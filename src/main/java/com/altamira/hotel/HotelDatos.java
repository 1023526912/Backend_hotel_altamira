package com.altamira.hotel;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class HotelDatos {
    private HotelDatos() {}

    public enum TipoHabitacion { ESTANDAR, PREMIER, SUITE }
    public enum EstadoHabitacion { DISPONIBLE, OCUPADA }

    public record AltaCliente(
            @NotBlank @Size(max = 40) String identificacion,
            @NotBlank @Size(max = 160) String nombreCompleto,
            @NotBlank @Email @Size(max = 254) String correoElectronico,
            @NotBlank @Size(max = 40) String telefono) {}

    public record Entrada(@NotNull @Positive Long clienteId, @NotNull TipoHabitacion tipoHabitacion) {}
    public record Salida(@NotNull @Positive Long clienteId) {}

    public record Cliente(long id, String identificacion, String nombreCompleto,
                          String correoElectronico, String telefono, LocalDateTime fechaCreacion) {}

    public record Habitacion(long id, int numeroHabitacion, int piso, TipoHabitacion tipoHabitacion,
                             BigDecimal valorNoche, EstadoHabitacion estado) {}

    public record RegistroEntrada(long hospedajeId, long clienteId, String cliente, int numeroHabitacion,
                                  TipoHabitacion tipoHabitacion, LocalDateTime fechaHoraIngreso, String mensaje) {}

    public record Factura(long facturaId, long clienteId, String cliente, int numeroHabitacion,
                          TipoHabitacion tipoHabitacion, LocalDateTime fechaHoraIngreso,
                          LocalDateTime fechaHoraSalida, int cantidadNoches, BigDecimal valorNoche,
                          BigDecimal valorTotal, LocalDateTime fechaGeneracion) {}
}
