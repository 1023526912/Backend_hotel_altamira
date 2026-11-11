INSERT INTO habitaciones (numero_habitacion, piso, tipo_habitacion, valor_noche, estado)
SELECT 101, 1, 'ESTANDAR', 120000, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM habitaciones WHERE numero_habitacion = 101);

INSERT INTO habitaciones (numero_habitacion, piso, tipo_habitacion, valor_noche, estado)
SELECT 102, 1, 'ESTANDAR', 120000, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM habitaciones WHERE numero_habitacion = 102);

INSERT INTO habitaciones (numero_habitacion, piso, tipo_habitacion, valor_noche, estado)
SELECT 201, 2, 'PREMIER', 185000, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM habitaciones WHERE numero_habitacion = 201);

INSERT INTO habitaciones (numero_habitacion, piso, tipo_habitacion, valor_noche, estado)
SELECT 202, 2, 'PREMIER', 185000, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM habitaciones WHERE numero_habitacion = 202);

INSERT INTO habitaciones (numero_habitacion, piso, tipo_habitacion, valor_noche, estado)
SELECT 301, 3, 'SUITE', 290000, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM habitaciones WHERE numero_habitacion = 301);

INSERT INTO habitaciones (numero_habitacion, piso, tipo_habitacion, valor_noche, estado)
SELECT 302, 3, 'SUITE', 290000, 'DISPONIBLE'
WHERE NOT EXISTS (SELECT 1 FROM habitaciones WHERE numero_habitacion = 302);
