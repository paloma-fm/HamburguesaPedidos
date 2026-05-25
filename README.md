# Hamburgueseria Pedidos - Microservicio

Microservicio REST para la gestión de pedidos de una hamburguesería. Desarrollado con **Spring Boot 3.5** y **Java 21**, con pipeline CI/CD completo implementado en **GitHub Actions** y orquestación de contenedores mediante **Docker Compose**.

---

## Tabla de Contenidos

1. [Descripción del Microservicio](#descripción-del-microservicio)
2. [Arquitectura](#arquitectura)
3. [Endpoints de la API](#endpoints-de-la-api)
4. [Cómo ejecutar localmente](#cómo-ejecutar-localmente)
5. [Docker Compose](#docker-compose)
6. [Pipeline CI/CD](#pipeline-cicd)
7. [Pruebas y Calidad](#pruebas-y-calidad)
8. [Seguridad](#seguridad)
9. [Trazabilidad](#trazabilidad)

---

## Descripción del Microservicio

Este microservicio expone una API REST para:
- Registrar pedidos de hamburguesas con los datos del cliente
- Consultar todos los pedidos registrados
- Notificar automáticamente al microservicio de notificaciones al crear un pedido

**Tecnologías principales:**
- Java 21 + Spring Boot 3.5
- Spring Data JPA + MySQL 8
- Spring Boot Actuator (health checks)
- Maven (gestión de dependencias y build)

---

## Arquitectura

```
┌─────────────────────────────────────────────────────────┐
│                   Docker Compose Stack                  │
│                                                         │
│  ┌──────────────────┐    ┌──────────────────────────┐  │
│  │  hamburgueseria  │───▶│         MySQL 8           │  │
│  │     -pedidos     │    │    (db_pedidosx:3306)     │  │
│  │   (port: 8081)   │    └──────────────────────────┘  │
│  │                  │                                   │
│  │                  │───▶┌──────────────────────────┐  │
│  └──────────────────┘    │  notificaciones (mock)    │  │
│                          │   MockServer (port:1080)  │  │
│  ┌──────────────────┐    └──────────────────────────┘  │
│  │    Adminer       │                                   │
│  │  (port: 8090)    │──▶ Administración BD             │
│  └──────────────────┘                                   │
└─────────────────────────────────────────────────────────┘
```

---

## Endpoints de la API

| Método | Endpoint       | Descripción                  |
|--------|----------------|------------------------------|
| POST   | /api/pedidos   | Crear un nuevo pedido        |
| GET    | /api/pedidos   | Listar todos los pedidos     |
| GET    | /actuator/health | Health check del servicio  |

### Ejemplo de creación de pedido

```bash
curl -X POST http://localhost:8081/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "cliente": "Juan Pérez",
    "hamburguesa": "Hamburguesa Clásica",
    "correoCliente": "juan@ejemplo.com"
  }'
```

**Respuesta:**
```json
{
  "id": 1,
  "cliente": "Juan Pérez",
  "hamburguesa": "Hamburguesa Clásica",
  "correoCliente": "juan@ejemplo.com"
}
```

---

## Cómo ejecutar localmente

### Requisitos previos
- Java 21+
- Maven 3.9+
- MySQL 8 corriendo en `localhost:3306` con base de datos `db_pedidosx`

### Ejecución sin Docker

```bash
# Clonar el repositorio
git clone <url-del-repositorio>
cd HamburguesaPedidos-main

# Ejecutar pruebas
mvn test

# Compilar y ejecutar
mvn spring-boot:run
```

El servicio estará disponible en `http://localhost:8081`.

---

## Docker Compose

El stack completo incluye: microservicio de pedidos, MySQL, MockServer (notificaciones) y Adminer.

### Pre-configuración

```bash
# 1. Copiar el archivo de variables de entorno
cp .env.example .env

# 2. (Opcional) Ajustar valores en .env según tu entorno
```

### Levantar el stack

```bash
docker compose up --build -d
```

### Verificar el estado

```bash
docker compose ps
```

### Puertos expuestos

| Servicio               | Puerto Host | Descripción                     |
|------------------------|-------------|---------------------------------|
| hamburgueseria-pedidos | 8081        | API REST del microservicio      |
| MySQL                  | 3306        | Base de datos                   |
| notificaciones (mock)  | 8082        | Mock del servicio de notif.     |
| Adminer                | 8090        | Interfaz web de administración  |

### Apagar el stack

```bash
docker compose down -v
```

### Componentes del Docker Compose

El archivo `docker-compose.yml` incluye todos los componentes exigidos:

| Componente | Implementación |
|------------|----------------|
| Definición de servicios | `hamburgueseria-pedidos`, `mysql`, `notificaciones`, `adminer` |
| Redes personalizadas | `hamburgueseria-network` (bridge) |
| Variables de entorno | Configuradas por servicio, sobreescribibles vía `.env` |
| Volúmenes persistentes | `hamburgueseria-mysql-data` para MySQL |
| Mapeo de puertos | Host:Contenedor para cada servicio |
| Dependencias entre servicios | `depends_on` con `condition: service_healthy` |
| Health checks | Configurados en todos los servicios principales |
| Estrategia de reinicio | `restart: unless-stopped` |
| Límites de recursos | `deploy.resources.limits` de memoria por servicio |
| Imágenes públicas y propias | `mysql:8.0`, `adminer`, `mockserver` + `build: .` |

---

## Pipeline CI/CD

El pipeline se implementa en GitHub Actions (`.github/workflows/ci-cd.yml`) y cubre el ciclo completo desde el código fuente hasta el despliegue simulado.

### Diagrama del Pipeline

```
push/PR
   │
   ▼
┌──────────┐
│  BUILD   │  mvn clean package -DskipTests
└────┬─────┘
     │
     ├──────────────────────────┐
     ▼                          ▼
┌──────────┐            ┌───────────┐
│   TEST   │            │    SCA    │
│ (JaCoCo) │            │  (OWASP)  │
│ ≥60% cov │            │ CVSS < 10 │
└────┬─────┘            └─────┬─────┘
     │                        │
     └──────────┬─────────────┘
                ▼
         ┌──────────┐
         │   SAST   │
         │SonarCloud│
         └────┬─────┘
              │
     ┌────────┴────────┐
     ▼                 ▼
┌──────────┐     ┌──────────┐
│  DOCKER  │     │   SAST   │
│  BUILD   │     │ (espera) │
│(hadolint)│     └──────────┘
└────┬─────┘
     │ (solo en main)
     ▼
┌──────────┐
│  DEPLOY  │
│  (Docker │
│  Compose)│
└──────────┘
```

### Jobs del pipeline

| Job | Herramienta | Propósito |
|-----|-------------|-----------|
| Build | Maven | Compila el proyecto |
| Test | JUnit 5 + JaCoCo | Ejecuta pruebas y verifica cobertura ≥ 60% |
| SCA | OWASP Dependency Check | Analiza vulnerabilidades en dependencias |
| SAST | SonarCloud o CodeQL | Análisis estático de calidad y seguridad |
| Docker Build | Docker Buildx + Hadolint | Construye y verifica la imagen Docker |
| Deploy | Docker Compose | Despliegue simulado con pruebas funcionales |

### Configurar SonarCloud (Esto es opcional)

1. Crear cuenta en [sonarcloud.io](https://sonarcloud.io)
2. Importar el repositorio de GitHub
3. Agregar los siguientes secretos/variables en el repositorio de GitHub:
   - **Secret:** `SONAR_TOKEN` → Token de autenticación de SonarCloud
   - **Variable:** `SONAR_ORGANIZATION` → Nombre de la organización en SonarCloud

> Si no se configura SonarCloud, el job SAST mostrará un mensaje informativo indicando los pasos para activarlo, y el pipeline continuará sin bloquearse.

### Configurar NVD API Key (opcional, acelera OWASP)

1. Obtener API key en [nvd.nist.gov](https://nvd.nist.gov/developers/request-an-api-key)
2. Agregar **Secret:** `NVD_API_KEY` en el repositorio de GitHub

---

## Pruebas y Calidad

### Ejecutar pruebas localmente

```bash
# Solo pruebas unitarias
mvn test

# Pruebas + reporte de cobertura + verificación umbral 60%
mvn verify

# Reporte de cobertura disponible en:
# target/site/jacoco/index.html
```

### Cobertura de pruebas

| Clase | Pruebas |
|-------|---------|
| `PedidoController` | `PedidoControllerTest` (4 tests con MockMvc) |
| `PedidoService` | `PedidoServiceTest` (4 tests con Mockito) |
| `PedidoRepository` | `PedidoRepositoryTest` (5 tests con @DataJpaTest + H2) |

**Umbral mínimo:** 60% de cobertura de líneas. El build falla si no se alcanza.

### Análisis de dependencias (SCA)

```bash
mvn dependency-check:check

# Reporte disponible en:
# target/dependency-check-report.html
```

---

## Seguridad

### Controles de seguridad en el pipeline

| Control | Herramienta | Acción |
|---------|-------------|--------|
| Cobertura mínima | JaCoCo | Bloquea build si < 60% |
| CVEs críticos | OWASP Dependency Check | Bloquea build si CVSS ≥ 10 |
| Calidad de código | SonarCloud / CodeQL | Reporta y puede bloquear |
| Calidad del Dockerfile | Hadolint | Bloquea en errores críticos |
| Actualizaciones automáticas | Dependabot | PRs automáticos semanales |

### Seguridad en contenedores

- La imagen Docker corre con un **usuario no-root** (`appuser`)
- Build multi-etapa: la imagen final no contiene Maven ni código fuente
- Variables sensibles gestionadas vía archivo `.env` (no comprometido en git)
- Health checks activos para detectar fallos de servicio

### .gitignore recomendado

Asegurarse de que `.env` esté en `.gitignore` para no exponer credenciales:

```
.env
target/
*.jar
```

---

## Trazabilidad

La trazabilidad del código desde desarrollo hasta producción se garantiza mediante:

1. **Control de versiones:** Cada commit en `main` o `develop` dispara el pipeline
2. **Build reproducible:** Maven genera artefactos JAR versionados (`0.0.1-SNAPSHOT`)
3. **Imagen etiquetada:** La imagen Docker se etiqueta con el SHA del commit (`hamburgueseria-pedidos:<sha>`)
4. **Artefactos del pipeline:** JAR, reporte JaCoCo, reporte OWASP guardados como artefactos de GitHub Actions
5. **Gate de calidad:** El despliegue solo ocurre si build + test + SCA + SAST pasan
6. **Logs de contenedores:** Disponibles vía `docker compose logs`
7. **Health checks:** El endpoint `/actuator/health` permite verificar el estado en cualquier momento
8. **Dependabot:** PRs automáticos para mantener dependencias actualizadas y seguras
