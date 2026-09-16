# Base de Datos - SmartRoute GT

## Motor de base de datos

SmartRoute GT utiliza MySQL como sistema gestor de base de datos.

Nombre de la base de datos:

smartroute_gt

## Creación de tablas

La estructura de las tablas es administrada automáticamente por Spring Boot mediante JPA/Hibernate.

La configuración utilizada en el backend es:

spring.jpa.hibernate.ddl-auto=update

Por esta razón, al iniciar el backend, Hibernate crea o actualiza automáticamente las tablas necesarias según las entidades Java del proyecto.

## Configuración

El backend se conecta a MySQL mediante las propiedades definidas en:

backend/src/main/resources/application.properties

La contraseña de MySQL no se almacena en GitHub.

Se proporciona mediante la variable de entorno:

DB_PASSWORD

Ejemplo:

export DB_PASSWORD='TU_CONTRASEÑA_DE_MYSQL'

Luego se puede iniciar el backend con:

cd backend
./mvnw spring-boot:run

## Datos de demostración

El archivo `sample-data.sql` contiene datos de ejemplo para realizar pruebas y demostrar las funciones principales de SmartRoute GT.

Incluye datos relacionados con:

- rutas
- paradas
- buses
- pilotos
- asignaciones
- viajes
- ubicaciones GPS
- visitas a paradas
- infracciones
- alertas y reportes

Estos datos son únicamente para pruebas y demostración del proyecto.

## Seguridad

Las credenciales reales de MySQL no deben almacenarse dentro del repositorio.

Cada integrante debe configurar su propia variable de entorno `DB_PASSWORD` antes de iniciar el backend.
