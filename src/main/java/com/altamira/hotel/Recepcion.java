package com.altamira.hotel;

import com.altamira.hotel.HotelDatos.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
class Recepcion {
    private static final RowMapper<Cliente> CLIENTE = (r, n) -> new Cliente(
            r.getLong("id"), r.getString("identificacion"), r.getString("nombre_completo"),
            r.getString("correo_electronico"), r.getString("telefono"),
            r.getObject("fecha_creacion", LocalDateTime.class));

    private static final RowMapper<Habitacion> HABITACION = (r, n) -> new Habitacion(
            r.getLong("id"), r.getInt("numero_habitacion"), r.getInt("piso"),
            TipoHabitacion.valueOf(r.getString("tipo_habitacion")), r.getBigDecimal("valor_noche"),
            EstadoHabitacion.valueOf(r.getString("estado")));

    private static final RowMapper<Factura> FACTURA = (r, n) -> new Factura(
            r.getLong("id"), r.getLong("cliente_id"), r.getString("nombre_completo"),
            r.getInt("numero_habitacion"), TipoHabitacion.valueOf(r.getString("tipo_habitacion")),
            r.getObject("fecha_hora_ingreso", LocalDateTime.class),
            r.getObject("fecha_hora_salida", LocalDateTime.class), r.getInt("cantidad_noches"),
            r.getBigDecimal("valor_noche"), r.getBigDecimal("valor_total"),
            r.getObject("fecha_generacion", LocalDateTime.class));

    private static final String CONSULTA_FACTURA = """
            SELECT f.*, h.cliente_id, h.fecha_hora_ingreso, h.fecha_hora_salida,
                   c.nombre_completo, r.numero_habitacion, r.tipo_habitacion
            FROM facturas f
            JOIN hospedajes h ON h.id = f.hospedaje_id
            JOIN clientes c ON c.id = h.cliente_id
            JOIN habitaciones r ON r.id = h.habitacion_id
            """;

    private record Estadia(long id, long habitacionId, LocalDateTime ingreso) {}

    private final JdbcClient db;
    private final ZoneId zona;

    Recepcion(JdbcClient db, @Value("${hotel.zona}") String zona) {
        this.db = db;
        this.zona = ZoneId.of(zona);
    }

    Cliente registrarCliente(AltaCliente datos) {
        try {
            long id = insertar(db.sql("""
                    INSERT INTO clientes (identificacion, nombre_completo, correo_electronico, telefono, fecha_creacion)
                    VALUES (?, ?, ?, ?, ?)
                    """).params(datos.identificacion().strip(), datos.nombreCompleto().strip(),
                    datos.correoElectronico(), datos.telefono().strip(), LocalDateTime.now(zona)));
            return cliente(id);
        } catch (DuplicateKeyException error) {
            throw new HotelError(HttpStatus.BAD_REQUEST, "La identificación ya está registrada");
        }
    }

    List<Cliente> clientes() {
        return db.sql("SELECT * FROM clientes ORDER BY id").query(CLIENTE).list();
    }

    Cliente cliente(long id) {
        return db.sql("SELECT * FROM clientes WHERE id = ?").param(id).query(CLIENTE)
                .optional().orElseThrow(() -> HotelError.noEncontrado("Cliente no encontrado"));
    }

    Cliente clientePorDocumento(String documento) {
        return db.sql("SELECT * FROM clientes WHERE identificacion = ?").param(documento).query(CLIENTE)
                .optional().orElseThrow(() -> HotelError.noEncontrado("Cliente no encontrado"));
    }

    List<Habitacion> habitaciones() {
        return db.sql("SELECT * FROM habitaciones ORDER BY numero_habitacion").query(HABITACION).list();
    }

    Habitacion habitacion(int numero) {
        return db.sql("SELECT * FROM habitaciones WHERE numero_habitacion = ?").param(numero)
                .query(HABITACION).optional().orElseThrow(() -> HotelError.noEncontrado("Habitación no encontrada"));
    }

    List<Habitacion> disponibles(TipoHabitacion tipo) {
        return db.sql("""
                SELECT * FROM habitaciones WHERE tipo_habitacion = ? AND estado = 'DISPONIBLE'
                ORDER BY numero_habitacion
                """).param(tipo.name()).query(HABITACION).list();
    }

    @Transactional
    public RegistroEntrada checkIn(Entrada datos) {
        Cliente cliente = bloquearCliente(datos.clienteId());
        if (estadiaActiva(cliente.id()).isPresent()) {
            throw HotelError.conflicto("El cliente ya tiene un hospedaje activo");
        }

        Habitacion habitacion = db.sql("""
                SELECT * FROM habitaciones WHERE tipo_habitacion = ? AND estado = 'DISPONIBLE'
                ORDER BY numero_habitacion LIMIT 1 FOR UPDATE
                """).param(datos.tipoHabitacion().name()).query(HABITACION).optional()
                .orElseThrow(() -> HotelError.conflicto("No hay habitaciones disponibles de ese tipo"));

        LocalDateTime ingreso = LocalDateTime.now(zona);
        long hospedaje = insertar(db.sql("""
                INSERT INTO hospedajes (cliente_id, habitacion_id, fecha_hora_ingreso, activo)
                VALUES (?, ?, ?, TRUE)
                """).params(cliente.id(), habitacion.id(), ingreso));
        db.sql("UPDATE habitaciones SET estado = 'OCUPADA' WHERE id = ?").param(habitacion.id()).update();

        return new RegistroEntrada(hospedaje, cliente.id(), cliente.nombreCompleto(), habitacion.numeroHabitacion(),
                habitacion.tipoHabitacion(), ingreso, "Habitación asignada: " + habitacion.numeroHabitacion());
    }

    @Transactional
    public Factura checkOut(Salida datos) {
        Cliente cliente = bloquearCliente(datos.clienteId());
        Estadia estadia = estadiaActiva(cliente.id())
                .orElseThrow(() -> HotelError.noEncontrado("El cliente no tiene un hospedaje activo"));

        BigDecimal tarifa = db.sql("SELECT valor_noche FROM habitaciones WHERE id = ? FOR UPDATE")
                .param(estadia.habitacionId()).query(BigDecimal.class).single();
        LocalDateTime salida = LocalDateTime.now(zona);
        int noches = nochesFacturadas(estadia.ingreso(), salida);

        db.sql("UPDATE hospedajes SET fecha_hora_salida = ?, activo = FALSE WHERE id = ?")
                .params(salida, estadia.id()).update();
        db.sql("UPDATE habitaciones SET estado = 'DISPONIBLE' WHERE id = ?")
                .param(estadia.habitacionId()).update();
        long factura = insertar(db.sql("""
                INSERT INTO facturas (hospedaje_id, cantidad_noches, valor_noche, valor_total, fecha_generacion)
                VALUES (?, ?, ?, ?, ?)
                """).params(estadia.id(), noches, tarifa, tarifa.multiply(BigDecimal.valueOf(noches)), salida));
        return factura(factura);
    }

    Factura factura(long id) {
        return db.sql(CONSULTA_FACTURA + "WHERE f.id = ?").param(id).query(FACTURA).optional()
                .orElseThrow(() -> HotelError.noEncontrado("Factura no encontrada"));
    }

    List<Factura> facturas() {
        return db.sql(CONSULTA_FACTURA + "ORDER BY f.id").query(FACTURA).list();
    }

    private Cliente bloquearCliente(long id) {
        return db.sql("SELECT * FROM clientes WHERE id = ? FOR UPDATE").param(id).query(CLIENTE).optional()
                .orElseThrow(() -> HotelError.noEncontrado("Cliente no encontrado"));
    }

    private java.util.Optional<Estadia> estadiaActiva(long clienteId) {
        return db.sql("""
                SELECT id, habitacion_id, fecha_hora_ingreso FROM hospedajes
                WHERE cliente_id = ? AND activo = TRUE
                """).param(clienteId).query((r, n) -> new Estadia(r.getLong("id"), r.getLong("habitacion_id"),
                r.getObject("fecha_hora_ingreso", LocalDateTime.class))).optional();
    }

    private static long insertar(JdbcClient.StatementSpec sentencia) {
        GeneratedKeyHolder claves = new GeneratedKeyHolder();
        sentencia.update(claves, "id");
        return Objects.requireNonNull(claves.getKey(), "La base de datos no devolvió el ID").longValue();
    }

    static int nochesFacturadas(LocalDateTime ingreso, LocalDateTime salida) {
        long dias = ChronoUnit.DAYS.between(ingreso.toLocalDate(), salida.toLocalDate());
        if (dias <= 0) {
            return 1;
        }
        return Math.toIntExact(dias + (salida.toLocalTime().isAfter(LocalTime.of(13, 0)) ? 1 : 0));
    }
}
