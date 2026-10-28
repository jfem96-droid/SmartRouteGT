-- Aplicar DESPUÉS de V2 y tras respaldo íntegro de MySQL. Aditivo, sin modificar viajes existentes.
CREATE TABLE IF NOT EXISTS trip_administrative_closure (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 trip_id BIGINT NOT NULL,
 operator_name VARCHAR(255) NOT NULL,
 reason VARCHAR(1000) NOT NULL,
 closed_at DATETIME(6) NOT NULL,
 INDEX idx_trip_admin_closure_trip (trip_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
