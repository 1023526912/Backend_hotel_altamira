CREATE TABLE IF NOT EXISTS clientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    identificacion VARCHAR(40) NOT NULL UNIQUE,
    nombre_completo VARCHAR(160) NOT NULL,
    correo_electronico VARCHAR(254) NOT NULL,
    telefono VARCHAR(40) NOT NULL,
    fecha_creacion TIMESTAMP(6) NOT NULL
);

CREATE TABLE IF NOT EXISTS habitaciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero_habitacion INT NOT NULL UNIQUE,
    piso INT NOT NULL,
    tipo_habitacion VARCHAR(20) NOT NULL,
    valor_noche DECIMAL(14,2) NOT NULL,
    estado VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS hospedajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    habitacion_id BIGINT NOT NULL,
    fecha_hora_ingreso TIMESTAMP(6) NOT NULL,
    fecha_hora_salida TIMESTAMP(6),
    activo BOOLEAN NOT NULL,
    CONSTRAINT fk_hospedaje_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_hospedaje_habitacion FOREIGN KEY (habitacion_id) REFERENCES habitaciones(id)
);

CREATE TABLE IF NOT EXISTS facturas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hospedaje_id BIGINT NOT NULL UNIQUE,
    cantidad_noches INT NOT NULL,
    valor_noche DECIMAL(14,2) NOT NULL,
    valor_total DECIMAL(16,2) NOT NULL,
    fecha_generacion TIMESTAMP(6) NOT NULL,
    CONSTRAINT fk_factura_hospedaje FOREIGN KEY (hospedaje_id) REFERENCES hospedajes(id)
);
