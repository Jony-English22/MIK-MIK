# MIK-MIK

Base del proyecto para una API en Java 25 con Spring Boot y una aplicación móvil en Flutter.

## Estructura

- `backend/src/main/java/com/twdev/mikmik/`: módulos del backend.
- `backend/src/main/resources/db/migration/`: futuras migraciones de base de datos.
- `backend/src/test/java/com/twdev/mikmik/`: pruebas del backend.
- `mobile/lib/`: aplicación Flutter organizada por funcionalidades.
- `mobile/test/`: pruebas de Flutter.
- `docs/adr/`: decisiones de arquitectura.

## Archivos iniciales

- `backend/pom.xml` define Java 25 y Spring Boot para el backend.
- `backend/src/main/resources/application.yaml` define el nombre de la aplicación y el puerto HTTP.
- `mobile/pubspec.yaml` define el paquete Flutter y sus dependencias básicas.
- `.env.example` documenta los valores de entorno locales. `.env` es local y está excluido de Git.

El archivo `.env` no se carga automáticamente por Spring Boot ni por Flutter. Sus valores deben exportarse al entorno antes de iniciar un proceso que los necesite. No se han añadido aún clases de arranque, pantallas, dependencias de infraestructura ni proyectos nativos de Android/iOS; esta entrega prepara solamente la estructura y la configuración inicial.

## Versiones previstas

- JDK 25.
- Spring Boot 4.1.1.
- Flutter con Dart 3.

## Convenciones del repositorio

- El backend y la aplicación móvil se mantienen como áreas independientes.
- Los artefactos generados (`target/`, `.dart_tool/` y similares) no se versionan.
- Los secretos y valores locales se mantienen en `.env`; el repositorio solo conserva `.env.example`.
