<div align="center">

# 🧠 superOscholar-recommendation-service

### *Motor de Recomendaciones Inteligente — A.IBERT ECI Planner*

> Analiza el perfil del estudiante, su historial de actividad y sus tareas pendientes para
> generar recomendaciones personalizadas y planes diarios mediante Inteligencia Artificial,
> reduciendo el estrés y mejorando el rendimiento académico.

---

### 🛠️ Stack Tecnológico

![Java](https://img.shields.io/badge/Java-21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-47A248?style=for-the-badge&logo=mongodb&logoColor=white)

### ☁️ Infraestructura & Calidad

![Azure](https://img.shields.io/badge/Azure-Cloud-0078D4?style=for-the-badge&logo=microsoft-azure&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Container-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-Build-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
![JaCoCo](https://img.shields.io/badge/JaCoCo-Coverage-D1322B?style=for-the-badge)
![SonarQube](https://img.shields.io/badge/SonarQube-Quality-4E9BCD?style=for-the-badge&logo=sonarqube&logoColor=white)

### 🏗️ Arquitectura

![Hexagonal](https://img.shields.io/badge/Architecture-Hexagonal-blueviolet?style=for-the-badge)
![Clean Architecture](https://img.shields.io/badge/Clean-Architecture-blue?style=for-the-badge)
![REST API](https://img.shields.io/badge/REST-API-009688?style=for-the-badge)
![Microservices](https://img.shields.io/badge/Microservices-Pattern-FF6B35?style=for-the-badge)

</div>

---

## 📑 Tabla de Contenidos

1. [👤 Integrantes](#1--integrantes)
2. [🎯 Descripción del Módulo](#2--descripción-del-módulo)
3. [⚙️ Tecnologías Utilizadas](#3--tecnologías-utilizadas)
4. [🏗️ Cómo Funciona el Módulo](#4--cómo-funciona-el-módulo)
   - [4.1 Módulos con los que se comunica](#41-módulos-con-los-que-se-comunica)
   - [4.2 Patrones utilizados](#42-patrones-utilizados)
   - [4.3 Estilo de arquitectura detallado](#43-estilo-de-arquitectura-detallado)
5. [📊 Diagramas](#5--diagramas)
   - [5.1 Diagrama de Clases](#51-diagrama-de-clases)
   - [5.2 Diagrama de Componentes](#52-diagrama-de-componentes)
   - [5.3 Diagrama de Secuencia](#53-diagrama-de-secuencia)
6. [⚡ Funcionalidades](#6--funcionalidades)
   - [6.1 R18 — Generación de Recomendaciones](#61-r18--generación-de-recomendaciones)
   - [6.2 R19 — Plan Diario de Tareas (AIB-29)](#62-r19--plan-diario-de-tareas)
   - [6.3 R20 — Reorganización Semanal del Estudio (AIB-30)](#63-r20--reorganización-semanal-del-estudio)
7. [🔌 Conexiones con Servicios Externos](#7--conexiones-con-servicios-externos)
8. [⚠️ Manejo de Errores](#8--manejo-de-errores)
9. [📋 Estrategia de Versionamiento y Branches](#9--estrategia-de-versionamiento-y-branches)
   - [9.1 Convenciones para crear ramas](#91-convenciones-para-crear-ramas)
   - [9.2 Convenciones para crear commits](#92-convenciones-para-crear-commits)
10. [🧪 Evidencia de Pruebas Unitarias](#10--evidencia-de-pruebas-unitarias)
11. [📈 Evidencia de Análisis de Cobertura](#11--evidencia-de-análisis-de-cobertura)
12. [🗂️ Código Organizado por Carpetas](#12--código-organizado-por-carpetas)
13. [🚀 Cómo Ejecutar el Proyecto](#13--cómo-ejecutar-el-proyecto)
14. [☁️ CI/CD y Despliegue en Azure](#14--cicd-y-despliegue-en-azure)
    - [14.1 Pipeline de Desarrollo (DEV)](#141-pipeline-de-desarrollo-dev)
    - [14.2 Pipeline de Producción (PROD)](#142-pipeline-de-producción-prod)
    - [14.3 Evidencia del Despliegue](#143-evidencia-del-despliegue)
    - [14.4 Link Swagger en Azure](#144-link-swagger-en-azure)
15. [🔐 Variables de Entorno](#15--variables-de-entorno)
16. [📚 Referencias](#16--referencias)

---

## 1. 👤 Integrantes

**Módulo 5 — Motor de Recomendaciones Inteligente**
**Proyecto:** A.IBERT — ECI Planner
**Institución:** Escuela Colombiana de Ingeniería Julio Garavito

<div align="center">

| 👨‍💻 Integrante | 🎓 Rol |
|--------------|--------|
| Equipo AIBERT (SuperOScholar) | Developer |

</div>

---

## 2. 🎯 Descripción del Módulo

El **Motor de Recomendaciones Inteligente** es el componente del sistema A.IBERT — ECI Planner encargado de personalizar la experiencia del estudiante mediante Inteligencia Artificial.

No se limita a listar tareas; analiza el contexto académico completo del estudiante para generar recomendaciones accionables y planes diarios optimizados.

<div align="center">

| ✅ **Qué hace** | ❌ **Problema que resuelve** |
|:---------------|:-----------------------------|
| Genera entre 1 y 5 recomendaciones personalizadas usando IA | Falta de orientación académica personalizada |
| Construye un plan diario priorizando tareas urgentes | Mala organización del tiempo de estudio |
| Detecta tareas reprogramables y sugiere reorganización | Sobrecarga en días específicos |
| Registra el historial de actividad estudiantil | Falta de contexto acumulado para mejorar consejos |
| Integra Circuit Breaker para resiliencia con IA externa | Interrupción del servicio por fallos del proveedor IA |

</div>

### Microservicios del Módulo

| Microservicio | Puerto | Responsabilidad |
|---|---|---|
| recommendation-service | 8086 | Motor de recomendaciones e IA |

---

## 3. ⚙️ Tecnologías Utilizadas

<div align="center">

| **Tecnología / Herramienta** | **Uso principal en el proyecto** |
|------------------------------|----------------------------------|
| **Java 21** | Lenguaje de programación base del microservicio backend. |
| **Spring Boot 3.4.3** | Framework principal para construir el microservicio, exponiendo APIs REST y gestionando configuración e inyección de dependencias. |
| **Spring Web** | Exposición de endpoints REST (controladores HTTP) dentro de la arquitectura hexagonal. |
| **Spring Security + JWT** | Autenticación y autorización mediante tokens JWT, asegurando el acceso a los endpoints. |
| **Spring Data MongoDB** | Integración con la base de datos NoSQL usando el patrón Repository. |
| **MongoDB Atlas** | Base de datos NoSQL en la nube para persistir recomendaciones y logs de actividad estudiantil. |
| **Spring Cloud OpenFeign** | Creación declarativa de clientes REST para consumir `profile-service` y `planning-service`. |
| **Resilience4j** | Circuit Breaker, Retry y TimeLimiter para tolerar fallos al llamar a Groq API y servicios internos. |
| **RabbitMQ (AMQP)** | Mensajería asíncrona para integración con otros microservicios del ecosistema. |
| **Apache Maven** | Gestión de dependencias, empaquetado del microservicio y automatización de builds en CI/CD. |
| **Lombok** | Reducción de código repetitivo con anotaciones como `@Getter`, `@Builder` y `@AllArgsConstructor`. |
| **MapStruct** | Mapeo automático entre entidades de dominio y DTOs. |
| **JUnit 5** | Framework de pruebas unitarias para validar la lógica de dominio y casos de uso. |
| **Mockito** | Simulación de dependencias (puertos, Feign clients, adaptadores) en pruebas unitarias. |
| **JaCoCo** | Generación de reportes de cobertura de código para evaluar la efectividad de las pruebas. |
| **SonarQube / SonarCloud** | Análisis estático del código e identificación de vulnerabilidades y code smells. |
| **Swagger (OpenAPI 3)** | Generación automática de documentación interactiva de los endpoints REST. |
| **Docker** | Contenerización del microservicio para despliegues aislados y consistentes. |
| **Azure App Service** | Entorno de ejecución en la nube donde se despliega el contenedor Docker. |
| **Azure Container Registry (ACR)** | Almacenamiento y versionado de las imágenes Docker generadas en CI/CD. |
| **GitHub Actions** | Pipelines de integración y despliegue continuo (CI/CD). |
| **Groq API** | Proveedor LLM principal (formato OpenAI-compatible, modelo `llama-3.3-70b-versatile`) para generación de recomendaciones. Fallback estático integrado ante fallos. |

</div>

> 🧠 **Stack seleccionado** para garantizar **escalabilidad**, **resiliencia**, **seguridad** y **mantenibilidad**, aplicando buenas prácticas de ingeniería de software.

---

## 4. 🏗️ Cómo Funciona el Módulo

### 4.1 Módulos con los que se comunica

El `recommendation-service` consume información de otros microservicios del ecosistema A.IBERT a través de HTTP REST (Feign Clients):

```
recommendation-service
 ├── profile-service     → Obtiene el perfil académico y disponibilidad del estudiante
 └── planning-service    → Obtiene las tareas pendientes y priorizadas del estudiante
```

<div align="center">

| 🌍 **Microservicio** | ⚙️ **Operación** | 📋 **Propósito** |
|:---------------------|:----------------|:-----------------|
| **profile-service** | GET perfil del estudiante | Obtener disponibilidad horaria, historial y carga académica |
| **planning-service** | GET tareas del estudiante | Obtener lista de tareas pendientes priorizadas para el plan diario |

</div>

Adicionalmente, el microservicio se conecta con el proveedor de IA vía HTTP REST:
- **Groq** — proveedor único de IA (Circuit Breaker + fallback estático integrado)

### 4.2 Patrones utilizados

<div align="center">

| 🎨 **Patrón** | 📋 **Descripción** |
|:-------------|:-------------------|
| **Ports & Adapters (Hexagonal)** | Separación entre lógica de negocio e infraestructura |
| **Repository Pattern** | Abstracción del acceso a datos con Spring Data MongoDB |
| **Circuit Breaker + Fallback** | Resiliencia ante fallos de Groq: si el Circuit Breaker abre o el Retry se agota, se retorna un fallback estático garantizando disponibilidad (Resilience4j) |
| **DTO Pattern** | Separación entre objetos de transferencia y entidades de dominio |
| **Feign Client** | Comunicación declarativa HTTP con otros microservicios |
| **Factory Method** | Creación centralizada y validada de entidades de dominio |

</div>

### 4.3 Estilo de arquitectura detallado

El microservicio implementa **Clean Architecture** con enfoque **Hexagonal (Ports & Adapters)**:

```
┌─────────────────────────────────────────────────┐
│                  ENTRYPOINTS                    │
│         (Controllers REST / Swagger)            │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│                  APPLICATION                    │
│         (Use Cases / Services / DTOs)           │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│                    DOMAIN                       │
│    (Entities / Domain Logic / Port Interfaces)  │
└──────────────────────┬──────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────┐
│               INFRASTRUCTURE                    │
│  (MongoDB / GroqAdapter / Feign Clients / JWT)    │
└─────────────────────────────────────────────────┘
```

**Flujo de dependencias:** `Entrypoints → Application → Domain ← Infrastructure`

> La capa de **Domain** no depende de ninguna otra capa; es el núcleo de la aplicación.

---

## 5. 📊 Diagramas

### 5.1 Diagrama de Clases

> 📌 *Inserta aquí el diagrama de clases del dominio.*

<div align="center">
<img src="docs/images/diagrama-clases.png" alt="Diagrama de Clases" width="600"/>
</div>

**Resumen del diseño de dominio:**

- **`Recommendation`** — Entidad central persistida en MongoDB. Contiene la lista de `RecommendationItem`, el `recommendationType`, el `confidenceScore` y el `motivationalMessage`.
- **`RecommendationItem`** — Objeto de valor embebido con `title`, `description`, `type` e `itemScore`.
- **`StudentActivityLog`** — Entidad que registra el historial de actividad del estudiante para enriquecer el contexto de la IA.
- **`TaskDTO`** — Objeto de valor que representa una tarea con `priorityLevel`, `deadline` y `estimatedDurationMinutes`.

---

### 5.2 Diagrama de Componentes

> 📌 *Inserta aquí el diagrama de componentes específico del microservicio.*

<div align="center">
<img src="docs/images/diagrama-componentes.png" alt="Diagrama de Componentes" width="600"/>
</div>

**Flujo principal:**

- **`RecommendationController`** → Recibe solicitudes HTTP y delega a los puertos `GenerateRecommendationUseCase` y `GenerateDailyPlanUseCase`.
- **`GenerateRecommendationUseCaseImpl`** → Orquesta: obtiene perfil y tareas, construye prompt, llama a Groq, persiste el resultado.
- **`GenerateDailyPlanUseCaseImpl`** → Clasifica tareas en `todayTasks` y `reschedulableTasks`, calcula alertas de urgencia.
- **`GroqAIClient`** → Adaptador que llama al endpoint `/chat/completions` de Groq API con Circuit Breaker.
- **`ProfileFeignClientAdapter`** / **`TaskFeignClientAdapter`** → Feign Clients para consultar servicios externos.
- **`MongoRecommendationRepositoryAdapter`** → Persiste y recupera recomendaciones desde MongoDB Atlas.

---

### 5.3 Diagrama de Secuencia

#### 🔁 `POST /api/v1/recommendations`

Flujo completo de generación de recomendaciones: validación, consulta de historial, enriquecimiento de contexto con IA y persistencia del resultado.

<div align="center">

![Sequence POST recommendations](docs/images/sequence_recommendations_post.png)

</div>

#### 🔁 `GET /api/v1/recommendations/daily/{studentId}`

Flujo de consulta del plan diario: obtención de tareas priorizadas, clasificación de sugeridas vs reprogramables, y cálculo de alertas de urgencia.

<div align="center">

![Sequence GET daily plan](docs/images/sequence_recommendations_daily_get.png)

</div>

---

## 6. ⚡ Funcionalidades

---

### 6.1 R18 — Generación de Recomendaciones

Genera entre 1 y 5 recomendaciones personalizadas llamando al proveedor de IA (Groq) con el contexto académico completo del estudiante: perfil, historial de actividad y tareas pendientes.

**Endpoint:**
`POST /api/v1/recommendations`

---

#### 📦 Información de Entrada (Request)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | ⚠️ Restricciones | 📝 Descripción |
|---|---|:---:|---|
| `studentId` | `String (UUID)` | Obligatorio | Identificador único del estudiante (formato UUID). |
| `requestType` | `String` | Opcional | `PRODUCTIVIDAD`, `CARGA`, `GENERAL`. Si es nulo o inválido, se usa `GENERAL`. |
| `Authorization` | `String` | Obligatorio (Header) | Token JWT Bearer. |

</div>

---

#### 📦 Información de Salida (Response)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| `studentId` | `String (UUID)` | Identificador único del estudiante. |
| `recommendations` | `List<RecommendationItemDTO>` | Lista de 1 a 5 recomendaciones accionables. |
| `recommendationType` | `String` | Tipo efectivo: `PRODUCTIVIDAD`, `CARGA`, `GENERAL`. |
| `confidenceScore` | `Double` | Score global combinado de confianza (0.0 – 1.0). |
| `motivationalMessage` | `String` | Mensaje motivacional generado por la IA. |
| `message` | `String` | Mensaje descriptivo de la respuesta. |
| `dateGenerated` | `LocalDate` | Fecha de generación de las recomendaciones. |

</div>

#### 📦 Estructura de `RecommendationItemDTO`

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| `title` | `String` | Título corto y accionable de la recomendación. |
| `recommendation` | `String` | Justificación detallada generada por la IA. |
| `type` | `String` | `PRODUCTIVIDAD`, `CARGA`, `GENERAL`. |
| `confidenceScore` | `Double` | Score individual de la recomendación (0.0 – 1.0). |

</div>

---

#### ✅ Happy Path (Ejemplo de Uso Exitoso)

**Request:**
```http
POST /api/v1/recommendations
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json

{
  "studentId": "550e8400-e29b-41d4-a716-446655440000",
  "requestType": "PRODUCTIVIDAD"
}
```

**Response `200 OK`:**
```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440000",
  "recommendations": [
    {
      "title": "Estudia Cálculo primero",
      "recommendation": "Tu parcial de Cálculo es en 2 días y aún no has revisado los temas clave.",
      "type": "PRODUCTIVIDAD",
      "confidenceScore": 0.92
    },
    {
      "title": "Toma un descanso de 15 minutos",
      "recommendation": "Llevas más de 2 horas de estudio continuo. Un descanso mejora la retención.",
      "type": "PRODUCTIVIDAD",
      "confidenceScore": 0.85
    }
  ],
  "recommendationType": "PRODUCTIVIDAD",
  "confidenceScore": 0.89,
  "motivationalMessage": "¡Vas muy bien! Mantén el ritmo y lograrás tus metas académicas.",
  "message": "Aquí tienes tus recomendaciones personalizadas",
  "dateGenerated": "2026-05-16"
}
```

---

#### 📊 Tipos de Errores Manejados

<div align="center">

| 🔢 **Código HTTP** | ⚠️ **Escenario** | 💬 **Mensaje de Error** |
|:------------------:|:----------------|:------------------------|
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `studentId` vacío o faltante | `"studentId: must not be blank"` |
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | JSON malformado | `"JSON inválido o malformado."` |
| ![401](https://img.shields.io/badge/401-Unauthorized-orange?style=flat) | Token inválido o ausente | `"Invalid or missing JWT token"` |
| ![422](https://img.shields.io/badge/422-Unprocessable-yellow?style=flat) | Historial insuficiente | `"Aún no hay suficientes datos para generar recomendaciones"` |
| ![500](https://img.shields.io/badge/500-Internal_Error-critical?style=flat) | Error inesperado | `"Unexpected error"` |
| ![503](https://img.shields.io/badge/503-Service_Unavailable-critical?style=flat) | Groq API no disponible | `"Error de acceso a datos o proveedor externo no disponible"` |

</div>

---

### 6.2 R19 — Plan Diario de Tareas (AIB-29)

Genera un resumen diario del plan de estudio del estudiante: determina cuáles tareas deben realizarse hoy (máx. 5, ordenadas por prioridad), cuáles pueden reprogramarse (deadline > 3 días y prioridad `LOW`/`MEDIUM`), e incluye sugerencias de reorganización cuando aplica.

**Endpoint:**
`GET /api/v1/recommendations/daily/{studentId}`

---

#### 📦 Información de Entrada (Request)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | ⚠️ Restricciones | 📝 Descripción |
|---|---|:---:|---|
| `studentId` | `String (UUID)` | Obligatorio (Path) | Identificador del estudiante (formato UUID). |
| `currentDate` | `LocalDate` | Opcional (Query, ISO: YYYY-MM-DD) | Fecha para generar el plan. Si se omite, se usa la fecha actual. |
| `Authorization` | `String` | Obligatorio (Header) | Token JWT Bearer. |

</div>

---

#### 📦 Información de Salida (Response)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| `studentId` | `String (UUID)` | Identificador del estudiante. |
| `planDate` | `LocalDate` (AIB-29) | Fecha del plan generado. |
| `todayTasks` | `List<TaskDTO>` | Máximo 5 tareas para hoy, ordenadas por prioridad descendente. |
| `reschedulableTasks` | `List<TaskDTO>` | Tareas con deadline > 3 días y prioridad `LOW` o `MEDIUM`. |
| `reorganization` | `List<ReorganizationSuggestionDTO>` | Sugerencias de reorganización semanal (máx. 5, solo cuando hay reprogramables). |
| `totalEstimatedMinutes` | `Integer` | Suma de minutos estimados de las tareas de hoy. |
| `urgentAlert` | `Boolean` | `true` si alguna tarea vence en menos de 24 horas. |
| `message` | `String` | Mensaje descriptivo del plan generado. |

</div>

#### 📦 Estructura de `TaskDTO`

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| `taskId` | `String` | Identificador de la tarea. |
| `title` | `String` | Título de la tarea. |
| `subjectId` | `String` | Identificador de la materia. |
| `priorityScore` | `Double` | Puntaje de prioridad calculado. |
| `priorityLevel` | `String` | `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`. |
| `deadline` | `LocalDateTime` | Fecha límite de entrega. |
| `estimatedDurationMinutes` | `Integer` | Duración estimada en minutos. |

</div>

#### 📦 Estructura de `ReorganizationSuggestionDTO`

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| `taskId` | `String` | Identificador de la tarea a reorganizar. |
| `title` | `String` | Título de la tarea para visualización. |
| `fromDay` | `LocalDate` | Día actual de la tarea en el plan (ISO 8601). |
| `toDay` | `LocalDate` | Día sugerido al que mover la tarea (ISO 8601). |
| `justification` | `String` | Motivo de la sugerencia (máx. 300 caracteres). |

</div>

---

#### ✅ Happy Path (Ejemplo de Uso Exitoso)

**Request:**
```http
GET /api/v1/recommendations/daily/550e8400-e29b-41d4-a716-446655440000?currentDate=2026-05-16
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response `200 OK`:**
```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440000",
  "planDate": "2026-05-16",
  "todayTasks": [
    {
      "taskId": "TASK-101",
      "title": "Parcial de Cálculo Diferencial",
      "subjectId": "CALC-201",
      "priorityScore": 0.95,
      "priorityLevel": "CRITICAL",
      "deadline": "2026-05-17T10:00:00",
      "estimatedDurationMinutes": 180
    },
    {
      "taskId": "TASK-205",
      "title": "Taller de Programación",
      "subjectId": "PROG-101",
      "priorityScore": 0.72,
      "priorityLevel": "HIGH",
      "deadline": "2026-05-18T23:59:00",
      "estimatedDurationMinutes": 120
    }
  ],
  "reschedulableTasks": [
    {
      "taskId": "TASK-310",
      "title": "Lectura de Ética",
      "subjectId": "ETH-101",
      "priorityScore": 0.30,
      "priorityLevel": "LOW",
      "deadline": "2026-05-25T23:59:00",
      "estimatedDurationMinutes": 60
    }
  ],
  "reorganization": [
    {
      "taskId": "TASK-310",
      "title": "Lectura de Ética",
      "fromDay": "2026-05-16",
      "toDay": "2026-05-20",
      "justification": "Esta tarea tiene baja urgencia y prioridad, puedes abordarla más adelante para reducir tu carga de hoy."
    }
  ],
  "totalEstimatedMinutes": 300,
  "urgentAlert": true,
  "message": "Tienes una tarea crítica que vence mañana. ¡Prioriza tu tiempo hoy!"
}
```

---

#### 📊 Tipos de Errores Manejados

<div align="center">

| 🔢 **Código HTTP** | ⚠️ **Escenario** | 💬 **Mensaje de Error** |
|:------------------:|:----------------|:------------------------|
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `studentId` inválido o `currentDate` con formato incorrecto | `"Parámetro inválido: currentDate"` |
| ![401](https://img.shields.io/badge/401-Unauthorized-orange?style=flat) | Token inválido o ausente | `"Invalid or missing JWT token"` |
| ![405](https://img.shields.io/badge/405-Method_Not_Allowed-orange?style=flat) | Método HTTP no permitido | `"Método HTTP no permitido."` |
| ![500](https://img.shields.io/badge/500-Internal_Error-critical?style=flat) | Error inesperado | `"Unexpected error"` |
| ![503](https://img.shields.io/badge/503-Service_Unavailable-critical?style=flat) | `planning-service` o `profile-service` no disponibles | `"Error de acceso a datos o proveedor externo no disponible"` |

</div>

---

### 6.3 R20 — Reorganización Semanal del Estudio (AIB-30)

Extiende las sugerencias de AIB-29 al ámbito semanal. Analiza el plan semanal activo (AIB-24), identifica días sobrecargados (>80 % de la disponibilidad diaria) y genera máximo 5 sugerencias concretas de movimiento de tareas entre días, priorizando los días de mayor carga.

**Endpoint:**
`GET /api/v1/recommendations/weekly/{studentId}`

---

#### 📦 Información de Entrada (Request)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | ⚠️ Restricciones | 📝 Descripción |
|---|---|:---:|---|
| `studentId` | `String (UUID)` | Obligatorio (Path) | Identificador del estudiante (formato UUID). |
| `currentDate` | `LocalDate` | Opcional (Query, ISO: YYYY-MM-DD) | Fecha de referencia de la semana. Default: hoy. |
| `Authorization` | `String` | Obligatorio (Header) | Token JWT Bearer. |

</div>

---

#### 📦 Información de Salida (Response — `WeeklyReorganizationDTO`)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| `studentId` | `String (UUID)` | Identificador del estudiante. |
| `weekStartDate` | `LocalDate` | Fecha de inicio de la semana analizada. |
| `reorganizationSuggestions` | `List<ReorganizationSuggestionDTO>` | Máximo 5 sugerencias de movimiento de tareas (RN-02). |
| `reschedulableTasks` | `List<TaskDTO>` | Tareas con deadline > 3 días y prioridad `LOW` o `MEDIUM` (RN-01). |
| `overloadedDays` | `List<LocalDate>` | Días con carga > 80 % de la disponibilidad diaria (RN-03). |
| `message` | `String` | `"Aquí tienes sugerencias para reorganizar tu semana"` o `"Tu semana está bien distribuida"`. |

</div>

---

#### ✅ Happy Path (Ejemplo de Uso Exitoso)

**Request:**
```http
GET /api/v1/recommendations/weekly/550e8400-e29b-41d4-a716-446655440000?currentDate=2026-05-16
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

**Response `200 OK`:**
```json
{
  "studentId": "550e8400-e29b-41d4-a716-446655440000",
  "weekStartDate": "2026-05-16",
  "reorganizationSuggestions": [
    {
      "taskId": "TASK-103",
      "title": "Laboratorio Física",
      "fromDay": "2026-05-16",
      "toDay": "2026-05-19",
      "justification": "La tarea puede moverse de 2026-05-16 a 2026-05-19 para reducir la sobrecarga del día de origen y optimizar tu disponibilidad semanal."
    }
  ],
  "reschedulableTasks": [
    {
      "taskId": "TASK-103",
      "title": "Laboratorio Física",
      "priorityLevel": "MEDIUM",
      "deadline": "2026-05-23T23:59:00",
      "estimatedDurationMinutes": 90
    }
  ],
  "overloadedDays": ["2026-05-16"],
  "message": "Aquí tienes sugerencias para reorganizar tu semana"
}
```

---

#### ⚠️ Flujos alternos

| Escenario | Mensaje retornado |
|:----------|:------------------|
| **FA-01** Sin tareas reprogramables | `"Tu semana está bien distribuida, no hay tareas para reorganizar."` |
| **FA-03** Sin plan semanal activo | `"Genera primero tu plan semanal en la sección de distribución automática."` |

---

#### 📊 Tipos de Errores Manejados

<div align="center">

| 🔢 **Código HTTP** | ⚠️ **Escenario** | 💬 **Mensaje de Error** |
|:------------------:|:----------------|:------------------------|
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `studentId` inválido o `currentDate` con formato incorrecto | `"Parámetro inválido: currentDate"` |
| ![401](https://img.shields.io/badge/401-Unauthorized-orange?style=flat) | Token inválido o ausente | `"Invalid or missing JWT token"` |
| ![500](https://img.shields.io/badge/500-Internal_Error-critical?style=flat) | Error inesperado | `"Unexpected error"` |
| ![503](https://img.shields.io/badge/503-Service_Unavailable-critical?style=flat) | `planning-service` no disponible (usa fallback) | Respuesta con datos de fallback |

</div>

---

## 7. 🔌 Conexiones con Servicios Externos

El microservicio se comunica con servicios externos a través de **Feign Clients** HTTP REST y la **API de Groq**:

<div align="center">

| 🌍 **Servicio Externo** | 🔗 **Tipo de Conexión** | ⚙️ **Operación** | 📋 **Propósito** |
|:------------------------|:------------------------|:----------------|:-----------------|
| **profile-service** | Feign Client HTTP | `GET /profiles/{studentId}` | Obtener perfil académico y disponibilidad horaria del estudiante |
| **planning-service** | Feign Client HTTP | `GET /planning/prioritization` + `GET /planning/distribution` | Obtener tareas priorizadas (AIB-22) y bloques del plan semanal activo (AIB-24/AIB-30) |
| **Groq API** | HTTP REST (Groq Cloud) | `POST /chat/completions` | Proveedor LLM único (`llama-3.3-70b-versatile`). Circuit Breaker activo; si falla, responde con fallback estático. |

</div>

### Configuración de Feign Clients

```java
@FeignClient(name = "profile-service", url = "${services.profile.url}")
public interface ProfileFeignClient {
    @GetMapping("/profiles/{studentId}")
    StudentProfileDTO getProfile(@PathVariable Long studentId,
                                  @RequestHeader("Authorization") String token);
}

@FeignClient(name = "planning-service", url = "${services.planning.url}")
public interface PlanningFeignClient {
    @GetMapping("/planning/prioritization")
    List<TaskDTO> getPrioritizedTasks(@RequestParam("studentId") String studentId);

    @GetMapping("/planning/distribution")
    List<WeeklyPlanBlock> getWeeklyPlan(
        @RequestParam("studentId") String studentId,
        @RequestParam("weekStart") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart);
}
```

### Circuit Breaker con Resilience4j

Todas las llamadas a los proveedores de IA están protegidas con **Circuit Breaker + Fallback en cadena**:

```
GroqAdapter
  └─ Circuit Breaker groqAI  →  fallbackRecommendation() [estático]
```

Configuración habilitada:

```yaml
spring:
  cloud:
    openfeign:
      circuitbreaker:
        enabled: true
```

> ⚠️ **Nota:** Todas las llamadas a servicios externos incluyen el token JWT en el header `Authorization` para mantener la seguridad de extremo a extremo.

---

## 8. ⚠️ Manejo de Errores

El microservicio implementa un **mecanismo centralizado de manejo de errores** mediante `GlobalExceptionHandler` (`@RestControllerAdvice`), garantizando respuestas uniformes y seguras.

### Estructura de Error Estandarizada

```json
{
  "status": 422,
  "message": "Aún no hay suficientes datos para generar recomendaciones",
  "timestamp": "2026-05-16T10:00:00",
  "errorId": "a3f2e1d0-..."
}
```

### Excepciones manejadas

<div align="center">

| 🔢 **Código HTTP** | ⚠️ **Excepción / Escenario** | 💬 **Mensaje** |
|:------------------:|:-----------------------------|:---------------|
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `MethodArgumentNotValidException` — campos inválidos | Detalle de cada campo fallido |
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `HttpMessageNotReadableException` — JSON malformado | `"JSON inválido o malformado."` |
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `MethodArgumentTypeMismatchException` — tipo inválido | `"Parámetro inválido: {nombre}"` |
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | `MissingServletRequestParameterException` — parámetro faltante | `"Falta el parámetro requerido: {nombre}"` |
| ![405](https://img.shields.io/badge/405-Method_Not_Allowed-orange?style=flat) | `HttpRequestMethodNotSupportedException` | `"Método HTTP no permitido."` |
| ![422](https://img.shields.io/badge/422-Unprocessable-yellow?style=flat) | `InsufficientHistoryException` — historial insuficiente | Mensaje personalizado de dominio |
| ![500](https://img.shields.io/badge/500-Internal_Error-critical?style=flat) | `Exception` — error inesperado | `"Unexpected error"` |
| ![503](https://img.shields.io/badge/503-Service_Unavailable-critical?style=flat) | `DataAccessException` — fallo de BD o proveedor externo | `"Error de acceso a datos o proveedor externo no disponible"` |

</div>

### Beneficios del Manejo Centralizado

<div align="center">

| 🎯 **Beneficio** | 📋 **Descripción** |
|:-----------------|:-------------------|
| **🎯 Uniformidad** | Todas las respuestas de error tienen el mismo formato JSON |
| **🔧 Mantenibilidad** | Agregar nuevas excepciones no requiere modificar cada controlador |
| **🔒 Seguridad** | Oculta detalles internos del servidor al cliente |
| **📍 Trazabilidad** | Cada error incluye un `errorId` único (UUID) y `timestamp` |
| **🤝 Integración** | Facilita la comunicación con frontend y herramientas como Postman/Swagger |

</div>

---

## 9. 📋 Estrategia de Versionamiento y Branches

El equipo utiliza **GitFlow** como modelo de ramificación para el control de versiones.

### Ramas y propósito

#### `main`
- **Propósito:** Rama **estable** con la versión final (lista para producción).
- **Reglas:** Solo recibe merges desde `release/*` y `hotfix/*`. Cada merge crea un **tag** SemVer (`vX.Y.Z`). Rama **protegida**: PR obligatorio con 1–2 aprobaciones y checks de CI en verde.

#### `develop`
- **Propósito:** Integración continua de trabajo; base de nuevas funcionalidades.
- **Reglas:** Recibe merges desde `feature/*` y `release/*`. Rama **protegida** similar a `main`.

#### `feature/*`
- **Propósito:** Desarrollo de una funcionalidad específica.
- **Base:** `develop`.
- **Cierre:** Merge a `develop` mediante PR.

#### `release/*`
- **Propósito:** Congelar cambios para estabilizar antes del deploy.
- **Base:** `develop`.
- **Cierre:** Merge a `main` (crear **tag**) y merge de vuelta a `develop`.
- **Ejemplo:** `release/1.0.0`

#### `hotfix/*`
- **Propósito:** Corregir bugs **críticos** detectados en `main`.
- **Base:** `main`.
- **Cierre:** Merge a `main` (crear **tag PATCH**) y merge a `develop`.
- **Ejemplo:** `hotfix/fix-resilience-bug`

---

### 9.1 Convenciones para crear ramas

#### `feature/*`
**Formato:**
```
feature/[nombre-funcionalidad]-AIBERT_[codigo-jira]
```
**Ejemplos:**
- `feature/add-groq-client-AIBERT-42`
- `feature/daily-plan-endpoint-AIBERT-51`

**Reglas:**
- Usar **kebab-case**
- Máximo 50 caracteres
- Código de Jira obligatorio para trazabilidad

#### `release/*`
```
release/[version]
```
**Ejemplo:** `release/1.0.0`

#### `hotfix/*`
```
hotfix/[descripcion-breve-del-fix]
```
**Ejemplo:** `hotfix/fix-circuit-breaker-timeout`

---

### 9.2 Convenciones para crear commits

**Formato:**
```
[codigo-jira] [tipo]: [descripción específica de la acción]
```

**Tipos de commit:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de errores
- `docs`: Cambios en documentación
- `test`: Adición o corrección de pruebas
- `refactor`: Refactorización de código

**Ejemplos:**
```
AIBERT-42 feat: implementar adaptador Groq con Circuit Breaker
AIBERT-51 feat: generar plan diario con tareas priorizadas y reprogramables
AIBERT-42 fix: corregir timeout en llamada a Groq API
```

---

## 10. 🧪 Evidencia de Pruebas Unitarias

El microservicio implementa una **estrategia integral de pruebas** con JUnit 5 y Mockito.

### Tipos de pruebas implementadas

<div align="center">

| 🧪 **Tipo de Prueba** | 📋 **Descripción** | 🛠️ **Herramientas** |
|:---------------------|:-------------------|:--------------------|
| **Pruebas Unitarias** | Validan el funcionamiento aislado de los casos de uso y adaptadores | ![JUnit](https://img.shields.io/badge/JUnit_5-25A162?style=flat&logo=junit5&logoColor=white) ![Mockito](https://img.shields.io/badge/Mockito-C5D928?style=flat) |
| **Pruebas de Integración** | Verifican la interacción entre capas y el controlador REST | ![Spring Test](https://img.shields.io/badge/Spring_Test-6DB33F?style=flat&logo=spring&logoColor=white) |
| **Cobertura de Código** | Mide el porcentaje de código cubierto por las pruebas | ![JaCoCo](https://img.shields.io/badge/JaCoCo-D1322B?style=flat) |

</div>

### Cómo ejecutar las pruebas

#### 1️⃣ Ejecutar todas las pruebas

```bash
mvn clean test
```

#### 2️⃣ Generar reporte de cobertura con JaCoCo

```bash
mvn clean test jacoco:report
```

El reporte HTML se generará en:
```
target/site/jacoco/index.html
```

#### 3️⃣ Ejecutar una prueba específica

```bash
mvn test -Dtest=GenerateRecommendationUseCaseImplTest
```

#### 4️⃣ Ejecutar pruebas desde IntelliJ IDEA

1. Click derecho sobre la carpeta `src/test/java`
2. Selecciona **"Run 'Tests in...'"**
3. Ver resultados en el panel inferior

---

### Ejemplo de prueba unitaria

```java
@ExtendWith(MockitoExtension.class)
class GenerateRecommendationUseCaseImplTest {

    @InjectMocks
    private GenerateRecommendationUseCaseImpl useCase;

    @Mock
    private GroqAIClient groqAIClient;

    @Mock
    private ProfileFeignClientAdapter profileAdapter;

    @Test
    @DisplayName("Should return recommendations when student has sufficient history")
    void execute_SufficientHistory_ShouldReturnRecommendations() {
        // Given
        String studentId = "550e8400-e29b-41d4-a716-446655440000";
        String requestType = "PRODUCTIVIDAD";

        // When
        DailyRecommendationDTO result = useCase.execute(studentId, requestType);

        // Then
        assertThat(result.getRecommendations()).isNotEmpty();
        assertThat(result.getRecommendations().size()).isBetween(1, 5);
        assertThat(result.getRecommendationType()).isEqualTo("PRODUCTIVIDAD");
    }
}
```

---

### Evidencias de ejecución

> 📌 *Inserta aquí las capturas de pantalla de las pruebas ejecutándose.*

**1. Consola mostrando pruebas ejecutadas exitosamente:**

<div align="center">
<img src="docs/images/console-tests.png" alt="Consola Pruebas" width="700"/>
</div>

**2. Vista del panel de pruebas en IntelliJ IDEA:**

<div align="center">
<img src="docs/images/intellij-tests.png" alt="IntelliJ Pruebas" width="700"/>
</div>

---

### Criterios de aceptación de pruebas

- ✅ **Cobertura mínima del 70%** en servicios y lógica de negocio
- ✅ **Todas las pruebas en estado PASSED** (sin fallos)
- ✅ **Cero errores de compilación** en el código de pruebas
- ✅ **Pruebas de casos felices y casos de error** implementadas

---

## 11. 📈 Evidencia de Análisis de Cobertura

El análisis de cobertura se realiza con **JaCoCo** y se integra con **SonarCloud** para el análisis estático de calidad.

### Reporte JaCoCo

> 📌 *Inserta aquí la captura del reporte JaCoCo generado.*

<div align="center">
<img src="docs/images/jacoco-report.png" alt="Reporte JaCoCo" width="700"/>
</div>

### Análisis SonarCloud

> 📌 *Inserta aquí la captura del análisis de SonarCloud.*

<div align="center">
<img src="docs/images/sonarcloud-analysis.png" alt="Análisis SonarCloud" width="700"/>
</div>

**Organización SonarCloud:** `ai-bert-backend`
**Project Key:** `AI-BERT-BACKEND_superOscholar-recommendation-servic`

<div align="center">

| 📊 **Métrica** | 🎯 **Objetivo** | ✅ **Resultado** |
|:--------------|:---------------|:----------------|
| Cobertura de líneas | ≥ 70% | *(Pendiente)* |
| Cobertura de ramas | ≥ 65% | *(Pendiente)* |
| Code Smells | 0 Blocker | *(Pendiente)* |
| Vulnerabilidades | 0 Critical | *(Pendiente)* |
| Duplicación de código | < 5% | *(Pendiente)* |

</div>

---

## 12. 🗂️ Código Organizado por Carpetas

El microservicio sigue **Clean Architecture** con enfoque **Hexagonal (Ports & Adapters)**:

### Estructura general del proyecto (Scaffolding)

```
superOscholar-recommendation-service/
│
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/aibert/dosw/
│   │   │   │
│   │   │   ├── 📁 application/                     # 🔵 CAPA DE APLICACIÓN
│   │   │   │   ├── 📁 dto/
│   │   │   │   │   └── 📁 request/
│   │   │   │   │       └── RecommendationRequest.java
│   │   │   │   ├── 📁 mapper/
│   │   │   │   │   └── RecommendationMapper.java
│   │   │   │   └── 📁 usecase/
│   │   │   │       ├── GenerateRecommendationUseCaseImpl.java
│   │   │   │       ├── GenerateDailyPlanUseCaseImpl.java
│   │   │   │       └── GenerateWeeklyReorganizationUseCaseImpl.java
│   │   │   │
│   │   │   ├── 📁 config/                          # ⚙️ CONFIGURACIONES
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── SecurityConfig.java
│   │   │   │
│   │   │   ├── 📁 domain/                          # 🟢 CAPA DE DOMINIO
│   │   │   │   ├── 📁 exception/
│   │   │   │   │   └── InsufficientHistoryException.java
│   │   │   │   ├── 📁 model/
│   │   │   │   │   ├── Recommendation.java
│   │   │   │   │   ├── StudentActivityLog.java
│   │   │   │   │   └── TaskDTO.java
│   │   │   │   └── 📁 port/
│   │   │   │       └── 📁 in/
│   │   │   │           ├── GenerateRecommendationUseCase.java
│   │   │   │           └── GenerateDailyPlanUseCase.java
│   │   │   │
│   │   │   └── 📁 infrastructure/                  # 🟠 CAPA DE INFRAESTRUCTURA
│   │   │       └── 📁 adapters/
│   │   │           ├── 📁 in/rest/                 # Controladores REST
│   │   │           │   ├── RecommendationController.java
│   │   │           │   ├── GlobalExceptionHandler.java
│   │   │           │   └── 📁 dto/
│   │   │           │       ├── DailyRecommendationDTO.java
│   │   │           │       ├── DailyPlanDTO.java
│   │   │           │       ├── RecommendationItemDTO.java
│   │   │           │       └── ReorganizationSuggestionDTO.java
│   │   │           └── 📁 out/
│   │   │               ├── 📁 api/                 # Adaptadores LLM
│   │   │               │   ├── 📁 groq/
│   │   │               │   │   ├── GroqAdapter.java           # Único proveedor de IA (Circuit Breaker → fallback estático)
│   │   │               │   │   └── 📁 dto/
│   │   │               ├── 📁 db/                  # Adaptadores MongoDB
│   │   │               └── 📁 feign/               # Clientes Feign
│   │   │                   ├── GroqAIClient.java
│   │   │                   ├── PlanningFeignClient.java
│   │   │                   ├── ProfileFeignClient.java
│   │   │                   ├── ProfileFeignClientAdapter.java
│   │   │                   └── TaskFeignClientAdapter.java
│   │   │
│   │   └── 📁 resources/
│   │       └── application.yml
│   │
│   └── 📁 test/                                    # 🧪 PRUEBAS
│       └── 📁 java/com/aibert/dosw/
│           └── 📁 infrastructure/adapters/out/api/
│
├── 📁 docs/
│   └── 📁 images/
├── 📁 output/                                      # Diagramas PlantUML generados
│   ├── dosw.puml
│   ├── sequence_recommendations_daily_get.puml
│   └── sequence_recommendations_post.puml
├── 📄 Dockerfile
├── 📄 docker-compose.yml
├── 📄 pom.xml
└── 📄 README.md
```

### Arquitectura Hexagonal Implementada

<div align="center">

| 🎨 **Capa** | 📋 **Responsabilidad** | 🔗 **Dependencias** |
|:-----------|:----------------------|:-------------------|
| **🟢 Domain** | Lógica de negocio pura, entidades y puertos (interfaces) | ❌ Ninguna (independiente) |
| **🔵 Application** | Casos de uso, mappers y DTOs de request | ✅ Solo `Domain` |
| **🟠 Infrastructure (in)** | Controladores REST y manejo global de errores | ✅ `Application` + `Domain` |
| **🟠 Infrastructure (out)** | Adaptadores MongoDB, Groq AI y Feign Clients | ✅ `Domain` |

</div>

**Flujo de dependencias:** `Entrypoints → Application → Domain ← Infrastructure`

---

## 13. 🚀 Cómo Ejecutar el Proyecto

### 📋 Prerrequisitos

- **Java 21**
- **Maven 3.8+**
- **Docker** y **Docker Compose** (opcional)
- **MongoDB Atlas** (o MongoDB local)
- **Clave de API de Groq** (`GROQ_API_KEY`)

### 🛠️ Opción 1: Ejecución Local (Maven)

```bash
# 1. Clonar el repositorio
git clone https://github.com/AI-BERT-BACKEND/superOscholar-recommendation-servic.git
cd superOscholar-recommendation-servic

# 2. Configurar variables de entorno (ver sección Variables de Entorno)
cp .env.example .env

# 3. Ejecutar la aplicación
mvn spring-boot:run
```

📍 **URL Local:** `http://localhost:8086`
📚 **Swagger UI:** `http://localhost:8086/swagger-ui.html`
📄 **API Docs:** `http://localhost:8086/v3/api-docs`

---

### 🐳 Opción 2: Ejecución con Docker Compose

```bash
# 1. Clonar el repositorio
git clone https://github.com/AI-BERT-BACKEND/superOscholar-recommendation-servic.git
cd superOscholar-recommendation-servic

# 2. Levantar los contenedores
docker-compose up --build -d

# 3. Ver logs
docker-compose logs -f

# 4. Detener los contenedores
docker-compose down
```

📍 **URL Docker:** `http://localhost:8086`

---

### 🐳 Opción 3: Ejecutar solo con Docker

```bash
# 1. Construir la imagen
docker build -t recommendation-service .

# 2. Ejecutar el contenedor
docker run -p 8086:8086 \
  -e MONGO_URI=mongodb+srv://user:pass@cluster.mongodb.net/recommendationdb \
  -e JWT_SECRET=your_jwt_secret \
  -e GROQ_API_KEY=your_groq_key \
  -e PLANNING_SERVICE_URL=http://planning-service:8087 \
  -e PROFILE_SERVICE_URL=http://profile-service:8081 \
  recommendation-service
```

---

## 14. ☁️ CI/CD y Despliegue en Azure

El proyecto implementa un **pipeline automatizado** con **GitHub Actions** para garantizar la calidad del código y el despliegue continuo en **Azure Cloud**.

---

### 14.1 Pipeline de Desarrollo (DEV)

Se ejecuta automáticamente en cada **Push** o **Pull Request** a la rama `develop`.

```yaml
# .github/workflows/cd_dev.yml
name: CI/CD — Development

on:
  push:
    branches: [ develop ]
  pull_request:
    branches: [ develop ]

jobs:
  build-and-test:
    name: 🧪 Build, Test & Quality
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: ☕ Set up Java 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: 🔨 Build + Test + Coverage
        run: mvn -B clean verify

      - name: 📊 SonarCloud Analysis
        run: mvn sonar:sonar
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

      - name: 🐳 Build Docker Image
        run: docker build -t recommendation-service-dev .

      - name: 📤 Push to ACR (Dev)
        run: |
          docker tag recommendation-service-dev $ACR_URL/recommendation-service:dev
          docker push $ACR_URL/recommendation-service:dev

      - name: 🚀 Deploy to Azure App Service (Dev)
        uses: azure/webapps-deploy@v2
        with:
          app-name: recommendation-service-dev
          images: ${{ secrets.ACR_URL }}/recommendation-service:dev
```

---

### 14.2 Pipeline de Producción (PROD)

Se ejecuta automáticamente en cada **Push** o **merge** a la rama `main`.

```yaml
# .github/workflows/cd_prod.yml
name: CI/CD — Production

on:
  push:
    branches: [ main ]

jobs:
  deploy-production:
    name: 🚀 Deploy to Production
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3

      - name: ☕ Set up Java 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: 🔨 Build + Test + Coverage
        run: mvn -B clean verify

      - name: 🐳 Build Docker Image (Production)
        run: docker build -t recommendation-service-prod .

      - name: 📤 Push to ACR (Production)
        run: |
          docker tag recommendation-service-prod $ACR_URL/recommendation-service:latest
          docker push $ACR_URL/recommendation-service:latest

      - name: 🚀 Deploy to Azure App Service (Production)
        uses: azure/webapps-deploy@v2
        with:
          app-name: recommendation-service-prod
          images: ${{ secrets.ACR_URL }}/recommendation-service:latest

      - name: 🏷️ Create Release Tag
        run: |
          git tag v${{ github.run_number }}
          git push origin v${{ github.run_number }}
```

---

### 14.3 Evidencia del Despliegue

> 📌 *Inserta aquí las capturas de pantalla de los despliegues en Azure.*

<div align="center">
  <img src="docs/images/azure-dev-deploy.png" alt="Azure Dev Deploy" width="45%" />
  <img src="docs/images/azure-prod-deploy.png" alt="Azure Prod Deploy" width="45%" />
</div>

### Infraestructura Azure

<div align="center">

| Componente | Servicio Azure | Propósito |
|:-----------|:---------------|:----------|
| **Compute** | ![App Service](https://img.shields.io/badge/App_Service-0078D4?logo=microsoft-azure&logoColor=white) | Ejecución del contenedor Docker del microservicio |
| **Registry** | ![ACR](https://img.shields.io/badge/ACR-0078D4?logo=docker&logoColor=white) | Almacenamiento privado de imágenes Docker |
| **Database** | ![MongoDB](https://img.shields.io/badge/MongoDB_Atlas-47A248?logo=mongodb&logoColor=white) | Persistencia de recomendaciones e historial estudiantil |
| **Monitoring** | ![Insights](https://img.shields.io/badge/App_Insights-5C2D91?logo=microsoft-azure&logoColor=white) | Logs, métricas y trazabilidad en tiempo real |

</div>

---

### 14.4 Link Swagger en Azure

<div align="center">

| 🌍 Ambiente | 🔗 URL Swagger | 📝 Estado |
|:-----------|:--------------|:---------|
| **🟢 Producción** | [recommendation-service-prod.azurewebsites.net/swagger-ui.html](#) | ![Pendiente](https://img.shields.io/badge/Status-Pending-yellow?style=flat) |
| **🟠 Desarrollo** | [recommendation-service-dev.azurewebsites.net/swagger-ui.html](#) | ![Pendiente](https://img.shields.io/badge/Status-Pending-yellow?style=flat) |

</div>

> 📌 *Reemplaza los links `#` por las URLs reales de Azure una vez desplegado el servicio.*

---

## 15. 🔐 Variables de Entorno

```bash
# Servidor
SERVER_PORT=8086

# Base de datos
MONGO_URI=mongodb+srv://user:password@cluster.mongodb.net/recommendationdb

# Seguridad
JWT_SECRET=your_jwt_secret_key_here

# Proveedor de IA primario (Groq)
GROQ_API_KEY=your_groq_api_key_here
GROQ_MODEL=llama-3.3-70b-versatile

# URLs de microservicios internos
PLANNING_SERVICE_URL=http://planning-service:8087
PROFILE_SERVICE_URL=http://profile-service:8081

# Spring Profiles
SPRING_PROFILES_ACTIVE=dev
```

### ⚙️ Propiedades principales (application.yml)

```yaml
server:
  port: ${SERVER_PORT:8086}

groq:
  api:
    base-url: "https://api.groq.com/openai/v1"
    key: "${GROQ_API_KEY:}"
    model: "${GROQ_MODEL:llama-3.3-70b-versatile}"

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 15000
      groqAiClient:
        connectTimeout: 5000
        readTimeout: 20000
```

> ⚠️ **Nunca subas el archivo `.env` al repositorio.** Usa `.env.example` como plantilla y agrega `.env` a tu `.gitignore`.

---

## 16. 📚 Referencias

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security + JWT](https://docs.spring.io/spring-security/reference/)
- [Spring Data MongoDB](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/)
- [OpenFeign Client](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/)
- [Resilience4j Documentation](https://resilience4j.readme.io/docs)
- [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- [SonarCloud](https://docs.sonarcloud.io/)
- [Docker Documentation](https://docs.docker.com/)
- [Azure App Service](https://docs.microsoft.com/en-us/azure/app-service/)
- [GitHub Actions](https://docs.github.com/en/actions)
- [Groq API Documentation](https://console.groq.com/docs)
- [Google Gemini API Documentation](https://ai.google.dev/api/generate-content)

---

<div align="center">

### 🏆 Microservicio de Recomendaciones — A.IBERT ECI Planner

![Module](https://img.shields.io/badge/Service-Recommendations-blueviolet?style=for-the-badge)
![Project](https://img.shields.io/badge/Project-A.IBERT_ECI_Planner-orange?style=for-the-badge)
![Year](https://img.shields.io/badge/Year-2026-blue?style=for-the-badge)

> 💡 **A.IBERT — ECI Planner** es un sistema académico inteligente diseñado para optimizar
> el rendimiento estudiantil mediante planificación automatizada e inteligencia artificial.

**🎓 Escuela Colombiana de Ingeniería Julio Garavito**

</div>

<div align="center">

<table>
  <thead>
    <tr>
      <th>💡 Funcionalidad</th>
      <th>Descripción</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td><strong>Generación de Recomendaciones</strong></td>
      <td>Procesa información de contexto del estudiante para enviar un prompt a IA (Groq) y generar entre 1 y 5 recomendaciones clave para el día.</td>
    </tr>
    <tr>
      <td><strong>Plan Diario de Tareas (AIB-29)</strong></td>
      <td>Genera un resumen diario con hasta 5 tareas prioritarias, identifica tareas reprogramables y sugiere reorganizaciones concretas (fromDay → toDay) con justificación.</td>
    </tr>
    <tr>
      <td><strong>Reorganización Semanal (AIB-30)</strong></td>
      <td>Analiza el plan semanal activo, detecta días sobrecargados (>80 % capacidad) y propone hasta 5 movimientos de tareas entre días para optimizar la semana. Las sugerencias nunca se aplican sin confirmación explícita (RN-04).</td>
    </tr>
    <tr>
      <td><strong>Integración IA (Groq)</strong></td>
      <td>Groq es el único proveedor de IA (Circuit Breaker + Retry). Si falla, se retorna una recomendación estática garantizando disponibilidad.</td>
    </tr>
    <tr>
      <td><strong>Registro de Actividad Estudiantil</strong></td>
      <td>Manejo de historiales (Student Activity Log) para tener contexto a largo plazo sobre los hábitos del estudiante.</td>
    </tr>
    <tr>
      <td><strong>Integración vía Feign</strong></td>
      <td>Comunicación robusta y síncrona con Profile Service y Task Service para agrupar todos los datos necesarios antes de solicitar recomendaciones a la IA.</td>
    </tr>
  </tbody>
</table>

</div>


## 4. 📋 Manejo de Estrategia de versionamiento y branches

### Estrategia de Ramas (Git Flow)

### Ramas y propósito
- Manejaremos GitFlow, el modelo de ramificación para el control de versiones de Git

#### `main`
- **Propósito:** rama **estable** con la versión final (lista para demo/producción).
- **Reglas:**
    - Solo recibe merges desde `release/*` y `hotfix/*`.
    - Cada merge a `main` debe crear un **tag** SemVer (`vX.Y.Z`).
    - Rama **protegida**: PR obligatorio, 1–2 aprobaciones, checks de CI en verde.

#### `develop`
- **Propósito:** integración continua de trabajo; base de nuevas funcionalidades.
- **Reglas:**
    - Recibe merges desde `feature/*` y también desde `release/*` al finalizar un release.
    - Rama **protegida** similar a `main`.

#### `feature/*`
- **Propósito:** desarrollo de una funcionalidad, refactor o spike.
- **Base:** `develop`.
- **Cierre:** se fusiona a `develop` mediante **PR**


#### `release/*`
- **Propósito:** congelar cambios para estabilizar pruebas, textos y versiones previas al deploy.
- **Base:** `develop`.
- **Cierre:** merge a `main` (crear **tag** `vX.Y.Z`) **y** merge de vuelta a `develop`.
- **Ejemplo de nombre:**  
  `release/1.3.0`

#### `hotfix/*`
- **Propósito:** corregir un bug **crítico** detectado en `main`.
- **Base:** `main`.
- **Cierre:** merge a `main` (crear **tag** de **PATCH**) **y** merge a `develop` para mantener paridad.
- **Ejemplos de nombre:**  
  `hotfix/fix-resilience-bug`

---

### 4.1 Convenciones para **crear ramas**

#### `feature/*`
**Formato:**
```
feature/[nombre-funcionalidad]-AIBERT_[codigo-jira]
```

**Ejemplos:**
- `feature/add-groq-client-AIBERT-42`

**Reglas de nomenclatura:**
- Usar **kebab-case** (palabras separadas por guiones)
- Máximo 50 caracteres en total
- Descripción clara y específica de la funcionalidad
- Código de Jira obligatorio para trazabilidad

#### `release/*`
**Formato:**
```
release/[version]
```
**Ejemplo:** `release/1.3.0`

#### `hotfix/*`
**Formato:**
```
hotfix/[descripcion-breve-del-fix]
```

---

### 4.2 Convenciones para **crear commits**

#### **Formato:**
```
[codigo-jira] [tipo]: [descripción específica de la acción]
```

#### **Tipos de commit:**
- `feat`: Nueva funcionalidad
- `fix`: Corrección de errores
- `docs`: Cambios en documentación
- `refactor`: Refactorización de código

## 5. ⚙️ Tecnologías Utilizadas

| **Tecnología / Herramienta** | **Uso principal en el proyecto** |
|------------------------------|----------------------------------|
| **Java OpenJDK 21** | Lenguaje de programación base, aprovechando las últimas características como Virtual Threads. |
| **Spring Boot 3.4.x** | Framework base, exponiendo APIs REST y gestionando inyección de dependencias. |
| **Spring Web** | Exposición de endpoints REST (controladores HTTP) en la arquitectura hexagonal. |
| **Spring Cloud OpenFeign** | Creación declarativa de clientes REST para consumir el Profile Service y Task Service. |
| **Resilience4j** | Implementación de Circuit Breaker para tolerar fallos al llamar APIs externas como Groq. |
| **Spring Security** | Configuración stateless; endpoints públicos (permitAll). |
| **Spring Data MongoDB** | Conexión a la base de datos NoSQL mediante el patrón Repository y adaptadores. |
| **MongoDB Atlas** | Base de datos NoSQL en la nube para persistir logs de actividad estudiantil e historial de recomendaciones. |
| **Apache Maven** | Gestión de dependencias y empaquetado del proyecto. |
| **MapStruct & Lombok** | Generación de código boilerplate (getters, setters) y mapeo automático entre Entidades y DTOs. |
| **JUnit 5 & Mockito** | Framework de pruebas unitarias simulando dependencias (puertos, feign clients). |
| **JaCoCo** | Generación de reportes de cobertura de código. |
| **Swagger (OpenAPI 3)** | Documentación interactiva de la API (springdoc-openapi). |

## 6. 🧩 Funcionalidad

---

### 1️⃣ Generar Recomendación Diaria

Permite generar recomendaciones personalizadas llamando a la IA con el contexto del estudiante.

**Endpoint principal:**  
`POST /api/v1/recommendations`

---

### 📦 Estructura de la Solicitud (Request)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | ⚠️ Restricciones | 📝 Descripción |
|---|---|:---:|---|
| studentId | Long | Obligatorio | Identificador único del estudiante. |
| requestType | String | Opcional | PRODUCTIVIDAD, CARGA, GENERAL. Si es nulo o inválido, se usa GENERAL. |

</div>

---

### 📦 Estructura de la Respuesta (Response)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| studentId | Long | Identificador único del estudiante. |
| recommendations | List<RecommendationItemDTO> | Lista de 1 a 5 recomendaciones accionables. |
| recommendationType | String | Tipo efectivo de recomendación. |
| confidenceScore | Double | Score global combinado (0.0 - 1.0). |
| motivationalMessage | String | Mensaje motivacional generado por la IA. |
| message | String | Mensaje descriptivo de la respuesta. |
| dateGenerated | LocalDate | Fecha de generación. |

</div>

---

### 📦 Estructura de `RecommendationItemDTO`

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| title | String | Título corto y accionable. |
| description | String | Justificación detallada. |
| recommendationType | String | PRODUCTIVIDAD, CARGA, GENERAL. |
| confidenceScore | Double | Score de la recomendación (0.0 - 1.0). |

</div>

---

### 2️⃣ Consultar Plan Diario

Genera y retorna un plan diario basado en el estado actual de las tareas del estudiante.

**Endpoint principal:**  
`GET /api/v1/recommendations/daily/{studentId}`

---

### 📦 Estructura de la Solicitud (Request)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | ⚠️ Restricciones | 📝 Descripción |
|---|---|:---:|---|
| studentId | Long | Obligatorio (Path) | Identificador del estudiante a consultar. |
| currentDate | LocalDate | Opcional (Query) | Fecha para generar el plan (formato ISO: YYYY-MM-DD). |

</div>

---

### 📦 Estructura de la Respuesta (Response)

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| studentId | Long | Identificador del estudiante. |
| planDate | LocalDate | Fecha del plan. |
| todayTasks | List<TaskDTO> | Máximo 5 tareas para hoy (priorizadas). |
| reschedulableTasks | List<TaskDTO> | Tareas reprogramables (deadline > 3 días y prioridad LOW/MEDIUM). |
| reorganizationSuggestions | List<ReorganizationSuggestionDTO> | Sugerencias de reorganización (si aplica). |
| totalEstimatedMinutes | Integer | Suma de minutos estimados del día. |
| urgentAlert | Boolean | True si alguna tarea vence en menos de 24h. |
| message | String | Mensaje descriptivo del plan. |

</div>

---

### 📦 Estructura de `TaskDTO`

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| taskId | String | Identificador de la tarea. |
| title | String | Título de la tarea. |
| subjectId | String | Identificador de la materia. |
| priorityScore | Double | Puntaje de prioridad. |
| priorityLevel | String | CRITICAL, HIGH, MEDIUM, LOW. |
| deadline | LocalDateTime | Fecha límite. |
| estimatedDurationMinutes | Integer | Duración estimada en minutos. |

</div>

---

### 📦 Estructura de `ReorganizationSuggestionDTO`

<div align="center">

| 🏷️ Campo | 🗃️ Tipo | 📝 Descripción |
|---|---|---|
| taskId | String | Identificador de la tarea. |
| taskTitle | String | Título de la tarea. |
| suggestedDay | String | Nuevo día sugerido (YYYY-MM-DD). |
| justification | String | Justificación de la sugerencia. |

</div>

---

## 7. 📊 Diagramas

Esta sección muestra los flujos de interacción entre componentes del microservicio mediante diagramas de secuencia.



### 🔁 Diagrama de Secuencia — `POST /api/v1/recommendations`

Flujo completo de generación de recomendaciones diarias para un estudiante: validación, consulta de historial, enriquecimiento de contexto con IA y persistencia del resultado.

<div align="center">

![Sequence POST recommendations](docs/images/sequence_recommendations_post.png)

</div>



### 🔁 Diagrama de Secuencia — `GET /api/v1/recommendations/daily/{studentId}`

Flujo de consulta del plan diario: obtención de tareas priorizadas, clasificación de sugeridas vs reprogramables, y cálculo de alertas de urgencia.

<div align="center">

![Sequence GET daily plan](docs/images/sequence_recommendations_daily_get.png)

</div>



### 🏗️ Arquitectura Hexagonal — Componentes clave

El microservicio de Recommendations separa sus responsabilidades:

- **Infraestructura In (REST Controllers):** `RecommendationController` recibe peticiones.
- **Aplicación (Use Cases):** `GenerateRecommendationUseCaseImpl`, `GenerateDailyPlanUseCaseImpl`.
- **Dominio:** `Recommendation`, `StudentActivityLog`.
- **Infraestructura Out (Adapters):**
  - `GroqAdapter` para la IA externa.
  - `MongoRecommendationRepositoryAdapter` para persistencia.
  - `ProfileFeignClientAdapter`, `TaskFeignClientAdapter` para servicios hermanos.

## 8. ⚠️ Manejo de Errores

El backend implementa un **mecanismo centralizado de manejo de errores** que garantiza uniformidad y seguridad.

A través de un `GlobalExceptionHandler` (`@ControllerAdvice`), se capturan las excepciones de validación o del dominio de negocio (por ejemplo, `InsufficientHistoryException` cuando no hay suficientes datos para recomendar).

### 📊 Tipos de errores manejados

<div align="center">

| 🔢 **Código HTTP** | ⚠️ **Escenario** |
|:------------------:|:----------------|
| ![400](https://img.shields.io/badge/400-Bad_Request-red?style=flat) | Datos inválidos en la petición o parámetros faltantes |
| ![405](https://img.shields.io/badge/405-Method_Not_Allowed-orange?style=flat) | Método HTTP no permitido |
| ![422](https://img.shields.io/badge/422-Unprocessable_Entity-orange?style=flat) | Historial insuficiente para generar recomendaciones |
| ![500](https://img.shields.io/badge/500-Internal_Server_Error-red?style=flat) | Error inesperado en el servidor |
| ![503](https://img.shields.io/badge/503-Service_Unavailable-critical?style=flat) | Error de acceso a datos o proveedor externo no disponible |

</div>

## 9. 🧪 Evidencia de Pruebas y Ejecución

El proyecto incluye pruebas unitarias integrales.

### 🚀 Cómo ejecutar las pruebas

#### **1️⃣ Ejecutar todas las pruebas**
```bash
mvn clean test
```

#### **2️⃣ Generar reporte de cobertura con JaCoCo**
```bash
mvn clean test jacoco:report
```
El reporte HTML se generará en `target/site/jacoco/index.html`.

## 10. 🗂️ Organización del Código (Scaffolding)

El microservicio sigue una **arquitectura hexagonal (puertos y adaptadores)**:

```
recommendation-service/
│
├── 📁 src/
│   ├── 📁 main/
│   │   ├── 📁 java/com/aibert/dosw/
│   │   │   ├── 📁 application/                     # 🔵 CAPA DE APLICACIÓN (Casos de Uso)
│   │   │   │   ├── 📁 dto/
│   │   │   │   └── 📁 usecase/
│   │   │   │
│   │   │   ├── 📁 config/                          # ⚙️ CONFIGURACIONES (Seguridad)
│   │   │   │
│   │   │   ├── 📁 domain/                          # 🟢 CAPA DE DOMINIO
│   │   │   │   ├── 📁 exception/
│   │   │   │   ├── 📁 model/                       # Entidades puras
│   │   │   │   └── 📁 port/                        # Puertos In/Out
│   │   │   │
│   │   │   └── 📁 infrastructure/                  # 🟠 CAPA DE INFRAESTRUCTURA
│   │   │       └── 📁 adapters/
│   │   │           ├── 📁 in/rest/                 # Controladores y Manejo de Errores
│   │   │           └── 📁 out/                     # Persistencia, Feign, AI Clients
│   │   │               ├── 📁 api/groq/            # Adaptador a LLM Groq
│   │   │               ├── 📁 db/                  # Adaptadores MongoDB
│   │   │               └── 📁 feign/               # Clientes Feign
│   │   │
│   │   └── 📁 resources/                           # 📄 application.yml
│   │
│   └── 📁 test/                                    # 🧪 PRUEBAS UNITARIAS
│
└── 📄 pom.xml                                      # Configuración Maven
```

## 11. 🚀 Ejecución del Proyecto

### 📋 Prerrequisitos
- **Java 21**
- **Maven 3.8+**
- **Docker** (Opcional)

### 🛠️ Opción 1: Ejecución Local (Maven)

```bash
mvn spring-boot:run
```
📍 **URL Local:** `http://localhost:8086` (o el puerto configurado)  
📚 **Documentación API (Swagger):** `http://localhost:8086/swagger-ui.html`

### 🐳 Opción 2: Ejecución con Docker (Si se incluye Dockerfile)

```bash
docker-compose up --build -d
```

## 12. ☁️ CI/CD y Despliegue en Azure

El proyecto tiene capacidad para desplegarse mediante GitHub Actions hacia Azure App Service o un entorno contenedorizado en la nube.
Se definen perfiles `local`, `qa` y `prod` en `application.yml` para gestionar la conexión a MongoDB. Variables clave:

- `SERVER_PORT`
- `MONGO_URI`
- `AI_PROVIDER`
- `GROQ_API_KEY`
- `GROQ_MODEL`
- `JWT_SECRET`
- `PLANNING_SERVICE_URL`
- `PROFILE_SERVICE_URL`

## 13. 🤝 Contribuciones

### Metodología
Se utiliza **Scrum** con iteraciones cortas, asegurando entregas continuas y mejora de valor. Las ramas principales son protegidas y todos los PRs deben cumplir validación estática (SonarQube) y ejecutar pipelines de CI.

<div align="center">

### 🏆 Proyecto AIBERT

![Course](https://img.shields.io/badge/Course-DOSW-orange?style=for-the-badge)
![Year](https://img.shields.io/badge/Year-2026-blue?style=for-the-badge)

</div>