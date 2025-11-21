# Backend-ESI-MEDIA-G05

Este repositorio contiene la API REST desarrollada con **Spring Boot** y **MongoDB** para la plataforma ESI Media. Gestiona la lógica de negocio, autenticación, gestión de multimedia (audio/video) y administración de usuarios.

## Stack Tecnológico

* **Java:** JDK (Versión recomendada: 17+)
* **Framework:** Spring Boot
* **Containerización:** Docker

## Requisitos Previos

Asegúrate de tener instalado:
1.  **Java JDK** instalado y configurado en el PATH.
2.  **Maven** (opcional si usas el wrapper `mvnw` incluido).

## Configuración del Entorno

El archivo principal de configuración se encuentra en `esi_media/src/main/resources/application.properties`.

## Ejecución del proyecto

.\mvnw.cmd spring-boot:run

## Compilación del proyecto

.\mvnw.cmd verify


## Estructura del Proyecto

El proyecto sigue una arquitectura en capas clásica de Spring Boot:

```text
esi_media/
├── src/main/java/iso25/g05/esi_media/
│   ├── config/         # Configuración de CORS, MongoDB, Seguridad e Interceptores
│   ├── controller/     # Controladores REST (Endpoints de la API)
│   ├── dto/            # Data Transfer Objects (Objetos para petición/respuesta)
│   ├── exception/      # Manejador global de errores y excepciones personalizadas
│   ├── mapper/         # Utilidades para mapear entre Entidades y DTOs
│   ├── model/          # Entidades persistentes en MongoDB (Usuario, Video, etc.)
│   ├── repository/     # Interfaces que extienden MongoRepository
│   └── service/        # Lógica de negocio principal
└── src/main/resources/
    ├── email-templates/ # Plantillas HTML para correos (recuperación, 2FA)
    └── application.properties # Configuración principal


