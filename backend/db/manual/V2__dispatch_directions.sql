-- Aplicar UNA SOLA VEZ tras respaldo de MySQL y verificación de esquema.
-- Sin cambios a contraseñas, usuarios, ni datos de rutas existentes.
ALTER TABLE driver_route_assignment ADD COLUMN tipo_recorrido VARCHAR(20) NOT NULL DEFAULT 'SOLO_IDA';
ALTER TABLE trip ADD COLUMN tipo_recorrido VARCHAR(20) NOT NULL DEFAULT 'SOLO_IDA';
ALTER TABLE trip ADD COLUMN sentido_actual VARCHAR(10) NOT NULL DEFAULT 'IDA';
CREATE TABLE route_return_stop (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 route_id BIGINT NOT NULL,
 stop_id BIGINT NOT NULL,
 orden INT NOT NULL,
 CONSTRAINT uq_return_order UNIQUE (route_id, orden),
 CONSTRAINT uq_return_stop UNIQUE (route_id, stop_id),
 CONSTRAINT fk_return_route FOREIGN KEY (route_id) REFERENCES route(id),
 CONSTRAINT fk_return_stop FOREIGN KEY (stop_id) REFERENCES stop(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
