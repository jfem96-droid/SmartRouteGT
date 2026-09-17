-- ============================================================
-- SmartRoute GT
-- Datos de demostración
-- Base de datos: smartroute_gt
-- ============================================================

USE smartroute_gt;

-- ------------------------------------------------------------
-- RUTAS
-- ------------------------------------------------------------

INSERT INTO route (id, activa, codigo, nombre) VALUES
(101, b'1', 'DEMO-R001', 'Ruta Demo Centro');

-- ------------------------------------------------------------
-- PARADAS
-- ------------------------------------------------------------

INSERT INTO stop
(id, latitud, longitud, nombre, radio_permitido, activa)
VALUES
(101, 14.6349, -90.5069, 'Parque Central Demo', 50.0, b'1'),
(102, 14.6405, -90.5133, 'Plaza Barrios Demo', 40.0, b'1'),
(103, 14.6127, -90.5147, 'Avenida Reforma Demo', 40.0, b'1');

-- ------------------------------------------------------------
-- ORDEN DE PARADAS DE LA RUTA
-- ------------------------------------------------------------

INSERT INTO route_stop
(id, orden, route_id, stop_id)
VALUES
(101, 1, 101, 101),
(102, 2, 101, 102),
(103, 3, 101, 103);

-- ------------------------------------------------------------
-- BUSES
-- ------------------------------------------------------------

INSERT INTO bus
(id, activa, capacidad, codigo, placa)
VALUES
(101, b'1', 45, 'DEMO-BUS-001', 'DEM001'),
(102, b'1', 40, 'DEMO-BUS-002', 'DEM002');

-- ------------------------------------------------------------
-- PILOTOS
-- ------------------------------------------------------------

INSERT INTO driver
(id, activo, licencia, nombre, sanciones_confirmadas, telefono)
VALUES
(101, b'1', 'DEMO-LIC-001', 'Piloto Demo Uno', 1, '5555-0101'),
(102, b'1', 'DEMO-LIC-002', 'Piloto Demo Dos', 0, '5555-0102');

-- ------------------------------------------------------------
-- ASIGNACIONES
-- ------------------------------------------------------------

INSERT INTO driver_route_assignment
(id, activa, bus_id, driver_id, route_id)
VALUES
(101, b'1', 101, 101, 101),
(102, b'1', 102, 102, 101);

-- ------------------------------------------------------------
-- VIAJES
-- ------------------------------------------------------------

INSERT INTO trip
(id, activo, fecha_fin, fecha_inicio, bus_id, driver_id, route_id)
VALUES
(101, b'1', NULL, NOW(), 101, 101, 101),
(102, b'1', NULL, NOW(), 102, 102, 101);

-- ------------------------------------------------------------
-- UBICACIONES GPS
-- ------------------------------------------------------------

INSERT INTO bus_location
(id, fecha_hora, latitud, longitud, velocidad, bus_id, trip_id)
VALUES
(101, NOW(), 14.6349, -90.5069, 25.0, 101, 101),
(102, NOW(), 14.6405, -90.5133, 35.0, 102, 102);

-- ------------------------------------------------------------
-- VISITA DE PARADA DE DEMOSTRACIÓN
-- El viaje 101 ya cumplió la primera parada.
-- ------------------------------------------------------------

INSERT INTO stop_visit
(id, cumplida, distancia_minima, fecha_cumplimiento,
 fecha_entrada, stop_id, trip_id)
VALUES
(101, b'1', 5.0, NOW(), DATE_SUB(NOW(), INTERVAL 10 SECOND), 101, 101);

-- ------------------------------------------------------------
-- INFRACCIÓN PENDIENTE
-- ------------------------------------------------------------

INSERT INTO violation
(id, descripcion, estado, fecha_hora, limite_permitido,
 tipo, velocidad_registrada, bus_id, driver_id, trip_id)
VALUES
(101,
 'Exceso de velocidad de demostración',
 'PENDING',
 NOW(),
 60.0,
 'SPEEDING',
 70.0,
 101,
 101,
 101);

-- ------------------------------------------------------------
-- ALERTA DE PASAJERO
-- ------------------------------------------------------------

INSERT INTO passenger_alert
(id, descripcion, estado, fecha_hora, latitud, longitud, tipo)
VALUES
(101,
 'Alerta SOS de demostración',
 'PENDING',
 NOW(),
 14.6349,
 -90.5069,
 'SOS');

-- ------------------------------------------------------------
-- REPORTE DE PASAJERO
-- ------------------------------------------------------------

INSERT INTO passenger_report
(id, descripcion, estado, fecha_hora, resolucion,
 tipo, bus_id, driver_id, trip_id)
VALUES
(101,
 'Reporte de conducción imprudente de demostración',
 'PENDING',
 NOW(),
 NULL,
 'RECKLESS_DRIVING',
 101,
 101,
 101);

-- ------------------------------------------------------------
-- REGISTRO DE AUDITORÍA
-- ------------------------------------------------------------

INSERT INTO audit_log
(id, accion, actor, descripcion, entidad, entidad_id, fecha_hora)
VALUES
(101,
 'DEMO_DATA',
 'SYSTEM',
 'Carga de datos de demostración SmartRoute GT',
 'DATABASE',
 NULL,
 NOW());