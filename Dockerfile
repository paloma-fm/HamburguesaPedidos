# ============================================================
# Etapa 1: BUILD - Compilación del microservicio con Maven
# ============================================================
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar pom.xml primero para aprovechar el caché de capas de Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copiar código fuente y compilar omitiendo las pruebas (ya ejecutadas en CI)
COPY src ./src
RUN mvn clean package -DskipTests --no-transfer-progress

# ============================================================
# Etapa 2: RUNTIME - Imagen mínima de producción
# ============================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear usuario no-root para mayor seguridad (IE3: seguridad en contenedores)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar únicamente el JAR generado en la etapa de build
COPY --from=build /app/target/hamburgueseria-pedidos-*.jar app.jar

# Variables de entorno configurables (sobreescribibles en docker-compose)
ENV SERVER_PORT=8081
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/db_pedidosx
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=root
ENV NOTIFICACIONES_URL=http://notificaciones:1080

EXPOSE 8081

# Health check integrado en la imagen usando el endpoint de Actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
