# Hotel Altamira

Backend de recepción hotelera en Java 21 y Spring Boot. Registra clientes, asigna habitaciones según disponibilidad, controla hospedajes y genera facturas de salida.

Referencia funcional del ejercicio: [hotel-Backend de Julian2008893](https://github.com/Julian2008893/hotel-Backend). Hotel Altamira implementa el mismo flujo con controladores REST, registros de Java para el intercambio de datos y SQL mediante JdbcClient.

## Ejecutar

Con JDK 21 y Maven 3.9 o superior, abre esta carpeta en IntelliJ IDEA o Visual Studio Code e importa el proyecto Maven. En la terminal ejecuta:

```bash
mvn spring-boot:run
```

Abre [Swagger UI](http://localhost:8080/swagger-ui/index.html) para probar la API. H2 guarda los datos en `datos/` y el primer arranque registra seis habitaciones de ejemplo, dos por categoría. Las tarifas de demostración están en pesos colombianos.

### MySQL

Crea una base vacía llamada `hotel_altamira` y configura las credenciales de un usuario con acceso a esa base. En Bash:

```bash
export HOTEL_DB_URL='jdbc:mysql://localhost:3306/hotel_altamira'
export HOTEL_DB_USER='usuario_local'
read -rsp 'Contraseña de MySQL: ' HOTEL_DB_PASSWORD
export HOTEL_DB_PASSWORD
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

El arranque crea las tablas y agrega las habitaciones de ejemplo que falten.

## Rutas

| Método | Ruta | Operación |
|---|---|---|
| POST | `/api/clientes` | Registrar cliente |
| GET | `/api/clientes` | Listar clientes |
| GET | `/api/clientes/{id}` | Consultar cliente por ID |
| GET | `/api/clientes/documento/{identificacion}` | Buscar por documento |
| GET | `/api/habitaciones` | Listar habitaciones |
| GET | `/api/habitaciones/{numero}` | Consultar habitación por número |
| GET | `/api/habitaciones/disponibles/{tipo}` | Filtrar por categoría y disponibilidad |
| POST | `/api/checkin` | Asignar habitación y abrir hospedaje |
| POST | `/api/checkout` | Cerrar hospedaje, liberar habitación y facturar |
| GET | `/api/facturas` | Listar facturas |
| GET | `/api/facturas/{id}` | Consultar una factura |

## Demostración en Swagger

1. Registra un cliente en `POST /api/clientes` con `{"identificacion":"ALT-001","nombreCompleto":"Laura Pérez","correoElectronico":"laura@example.com","telefono":"3001234567"}`. Copia el `id` de la respuesta.
2. Consulta `GET /api/habitaciones/disponibles/ESTANDAR`.
3. En `POST /api/checkin`, envía `{"clienteId":1,"tipoHabitacion":"ESTANDAR"}`. Usa el ID que te devolvió el primer paso. La API asigna la habitación disponible de menor número y la marca como ocupada.
4. Repite el check-in para ese cliente. La API debe responder `409` porque ya tiene un hospedaje activo.
5. En `POST /api/checkout`, envía `{"clienteId":1}` con el mismo ID. Verifica que el total sea la tarifa por noche multiplicada por las noches facturadas.
6. Consulta la factura por el `facturaId` de la respuesta y confirma que la habitación volvió a estar disponible.

El sistema registra las fechas en la zona horaria de Bogotá. Si el ingreso y la salida ocurren el mismo día, se cobra una noche. Si ocurren en días distintos, se cobra la diferencia en días y se suma una noche cuando la salida es después de las 13:00.

## Archivos principales

| Archivo | Función |
|---|---|
| `HotelAltamira.java` | Arranca Spring Boot y define el nombre de la API en Swagger |
| `HotelApi.java` | Conecta cada ruta HTTP con el servicio |
| `HotelDatos.java` | Define las solicitudes, validaciones, respuestas y categorías |
| `Recepcion.java` | Ejecuta las consultas y aplica las reglas de ingreso, salida y facturación |
| `ErroresApi.java` | Convierte problemas de validación y de negocio en respuestas HTTP |
| `schema.sql` | Define las tablas, claves foráneas y restricciones |
| `habitaciones-iniciales.sql` | Carga habitaciones de ejemplo |

El check-in y el check-out usan transacciones. El servicio bloquea la fila del cliente mientras verifica el hospedaje y bloquea la habitación antes de cambiar su disponibilidad. La salida, la liberación de la habitación y la factura se guardan juntas.
