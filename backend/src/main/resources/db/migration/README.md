# SmartRoute GT - Migraciones de base de datos

Este directorio contiene cambios de esquema necesarios para instalar o actualizar
la base de datos de SmartRoute GT.

## V1\_\_final_schema_updates.sql

Incluye:

- Estado CANCELLED para las infracciones.
- Tipo configurable de parada.
- Tiempo mínimo de parada.
- Tiempo objetivo de parada.
- Tiempo máximo de parada.
- Valores predeterminados para registros existentes.

## Importante

Estas migraciones no se ejecutan automáticamente en la configuración actual.

Antes de desplegar una nueva versión del backend sobre una base de datos existente,
se debe realizar una copia de seguridad y ejecutar las migraciones pendientes.

El backend utiliza variables de entorno para sus credenciales. Las contraseñas,
tokens y secretos nunca deben almacenarse en este directorio ni en Git.s
