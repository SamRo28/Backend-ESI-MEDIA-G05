# -----------------------------------------------------------------
# eti_media/Dockerfile
# -----------------------------------------------------------------
# Este Dockerfile es para LINUX, porque Render ejecuta Linux.
# Tu agente de Windows puede construir esto si Docker Desktop
# está instalado y configurado para "Linux Containers" (opción por defecto).
# -----------------------------------------------------------------

# --- Etapa 1: Compilación (Builder) ---
# Usamos una imagen que contiene Maven y JDK 17 (basada en Linux Jammy)
FROM eclipse-temurin:17-jdk-jammy as builder

# Establecemos el directorio de trabajo
WORKDIR /app

# Copiamos los archivos de Maven y descargamos dependencias
# Esto aprovecha la caché de Docker si no cambias el pom.xml
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline

# Copiamos el resto del código fuente y compilamos
COPY src ./src
RUN ./mvnw clean package -DskipTests

# --- Etapa 2: Ejecución (Runtime) ---
# Usamos una imagen ligera que solo contiene Java 17 JRE (Linux)
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copiamos el JAR compilado desde la etapa 'builder'
COPY --from=builder /app/target/esi_media-*.jar app.jar

# Exponemos el puerto 8080 (el que usa tu app Spring Boot)
EXPOSE 8080

# Comando para arrancar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]