# Tickets Backend

API REST para la gestión de tickets de soporte con clasificación automática, comentarios, actualización de estado y persistencia en PostgreSQL.

## Descripción general

Este proyecto es el backend de una solución para registrar solicitudes de clientes, analizar su contenido y asignar una categoría, prioridad y resumen inicial de forma automática. La aplicación está construida con Java 17 y Spring Boot y expone endpoints REST para operar con tickets.

## Características

- Registro, consulta y actualización de tickets
- Agregado de comentarios por ticket
- Cambio de estado y responsable asignado
- Clasificación automática por reglas de negocio
- Generación de resumen textual básico
- Validación de entrada y manejo centralizado de errores
- Soporte para ejecución con Docker y PostgreSQL

## Stack tecnológico

- Java 17
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- PostgreSQL 15
- Gradle
- Lombok
- Docker / Docker Compose

## Requisitos previos

- JDK 17 o superior
- Gradle (o usar el wrapper incluido en el proyecto)
- Docker y Docker Compose (opcional si quieres levantar la pila completa)
- PostgreSQL (si quieres correr la app en local sin contenedores)

## Estructura del proyecto

- `src/main/java/com/sysdatec/tickets/controller` — controladores REST
- `src/main/java/com/sysdatec/tickets/service` — lógica de negocio y clasificación
- `src/main/java/com/sysdatec/tickets/repository` — repositorios JPA
- `src/main/java/com/sysdatec/tickets/model` — entidades del dominio
- `src/main/java/com/sysdatec/tickets/dto` — objetos de entrada/salida
- `src/main/java/com/sysdatec/tickets/mapper` — conversión entre entidades y DTOs
- `src/main/resources/application.properties` — configuración de la app

## Configuración

La aplicación usa variables de entorno para conectarse a PostgreSQL y para la clave de la API de IA.

Variables principales:

```bash
DB_HOST=localhost
DB_USER=postgres
DB_PASSWORD=postgrespassword
LLM_API_KEY=tu_clave
LLM_API_BASE_URL=https://api.openai.com/v1
LLM_API_MODEL=gpt-4o-mini
```

Si ejecutas con Docker Compose, estas variables se inyectan automáticamente desde el archivo `docker-compose.yml`.

## Ejecución local

1. Clona el repositorio.
2. Asegúrate de tener PostgreSQL corriendo en `localhost:5432`.
3. Configura las variables de entorno.
4. Ejecuta la aplicación:

```bash
./gradlew bootRun
```

La API quedará disponible en:

```text
http://localhost:8080
```

## Ejecución con Docker Compose

Desde la raíz del proyecto:

```bash
docker compose up --build
```

Esto levanta:

- PostgreSQL: `localhost:5432`
- Backend: `http://localhost:8080`
- Frontend: `http://localhost` (si existe el proyecto frontend en el nivel superior del directorio)

## API REST

Base URL:

```text
/api/tickets
```

### 1) Listar tickets

```http
GET /api/tickets
```

### 2) Obtener un ticket por ID

```http
GET /api/tickets/{id}
```

### 3) Crear ticket

```http
POST /api/tickets
Content-Type: application/json
```

Ejemplo de body:

```json
{
  "customerName": "Ana García",
  "requestText": "Necesito ayuda porque me llegaron dos cobros por la misma factura y quiero saber qué está pasando.",
  "attachmentUrl": "https://example.com/adjunto.pdf"
}
```

Respuesta esperada:

```json
{
  "id": "...",
  "customerName": "Ana García",
  "requestText": "...",
  "category": "Facturación",
  "priority": "ALTA",
  "summary": "Solicitud analizada exitosamente (Simulador local). Resumen: ...",
  "status": "NUEVO",
  "assignedTo": null,
  "createdAt": "2026-09-18T10:00:00",
  "updatedAt": "2026-09-18T10:00:00",
  "comments": []
}
```

### 4) Agregar comentario a un ticket

```http
POST /api/tickets/{id}/comments
Content-Type: application/json
```

Ejemplo:

```json
{
  "author": "Soporte",
  "text": "Se revisó la factura y se confirma un error de cobro."
}
```

### 5) Actualizar estado

```http
PATCH /api/tickets/{id}/status
Content-Type: application/json
```

Ejemplo:

```json
"EN_PROCESO"
```

### 6) Actualizar responsable

```http
PATCH /api/tickets/{id}/assigned-to
Content-Type: application/json
```

Ejemplo:

```json
"Julio"
```

## Lógica de clasificación

La clasificación usa una API LLM compatible con OpenAI cuando `LLM_API_KEY` está configurada. El backend envía el texto del ticket al modelo con una instrucción para devolver un JSON con `category`, `priority` y `summary`.

Si no hay clave válida configurada, la aplicación hace un fallback a reglas locales para que no falle el flujo.

Reglas locales de respaldo:

- `factura`, `pago`, `cobro` -> `Facturación` / `ALTA`
- `urgente`, `caído`, `error` -> `Operaciones` / `ALTA`
- `ayuda`, `configurar` -> `Soporte Técnico` / `MEDIA`
- Default -> `Soporte General` / `BAJA`

## Variables por defecto

En `application.properties`, la app usa de forma predeterminada:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/tickets_db
spring.datasource.username=postgres
spring.datasource.password=postgrespassword
```

## Deteener la aplicación

```bash
Ctrl + C
```

O desde Docker:

```bash
docker compose down
```

## Notas

- El proyecto está pensado como backend para una solución de soporte con frontend en un repositorio hermano.
- La base de datos se crea y actualiza con Hibernate (`spring.jpa.hibernate.ddl-auto=update`).
- El contenido del archivo `.env` no se incluye en este README por seguridad.

## Licencia

Este proyecto no indica licencia específica en el repositorio. Consulta con el propietario del código antes de reutilizarlo en producción.
