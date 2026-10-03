ALTER TABLE violation
MODIFY COLUMN estado
ENUM('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED')
NOT NULL DEFAULT 'PENDING';

ALTER TABLE route_stop
ADD COLUMN IF NOT EXISTS tipo_parada
ENUM('NORMAL', 'ALTA_DEMANDA', 'REGULADORA')
NULL;

ALTER TABLE route_stop
ADD COLUMN IF NOT EXISTS tiempo_minimo_segundos INT NOT NULL DEFAULT 15;

ALTER TABLE route_stop
ADD COLUMN IF NOT EXISTS tiempo_objetivo_segundos INT NOT NULL DEFAULT 20;

ALTER TABLE route_stop
ADD COLUMN IF NOT EXISTS tiempo_maximo_segundos INT NOT NULL DEFAULT 30;

UPDATE route_stop
SET tipo_parada = 'NORMAL'
WHERE tipo_parada IS NULL;

UPDATE route_stop
SET tiempo_minimo_segundos = 15
WHERE tiempo_minimo_segundos <= 0;

UPDATE route_stop
SET tiempo_objetivo_segundos = 20
WHERE tiempo_objetivo_segundos <= 0;

UPDATE route_stop
SET tiempo_maximo_segundos = 30
WHERE tiempo_maximo_segundos <= 0;

ALTER TABLE route_stop
MODIFY COLUMN tipo_parada
ENUM('NORMAL', 'ALTA_DEMANDA', 'REGULADORA')
NOT NULL DEFAULT 'NORMAL';

ALTER TABLE route_stop
MODIFY COLUMN tiempo_minimo_segundos INT NOT NULL DEFAULT 15;

ALTER TABLE route_stop
MODIFY COLUMN tiempo_objetivo_segundos INT NOT NULL DEFAULT 20;

ALTER TABLE route_stop
MODIFY COLUMN tiempo_maximo_segundos INT NOT NULL DEFAULT 30;
