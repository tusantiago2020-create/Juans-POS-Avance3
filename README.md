# Juan’S POS — Avance 3 (proyecto de curso)

Código fuente de la solución de ventas e inventario para el Minimarket Familiar **Juan'S**. Continúa la **Alternativa 2** escogida en el Avance 2: **Java 17, Spring Boot 3 y aplicación web con base de datos**.

## Qué necesitas en tu computadora
- **JDK 17** o superior (preferiblemente Java 17 o 21); verifica con `java -version` y `javac -version`.
- **Maven 3.9+** instalado y en el `PATH`, verifica con `mvn -version`.
- Internet en la primera ejecución para descargar las dependencias de Maven. Las siguientes ejecuciones usan la caché local.
- Navegador Chrome, Edge o Firefox.

## Cómo ejecutarlo en Windows (sin configurar MySQL)
1. Descomprime toda la carpeta.
2. Haz doble clic en **`scripts/EJECUTAR-WINDOWS.bat`**. Si hay errores, abre una terminal en la carpeta raíz y ejecuta `mvn spring-boot:run` para verlos.
3. Cuando se vea `Started JuansApplication`, abre **http://localhost:8080**.
4. Inicia sesión con uno de los usuarios **exclusivos para pruebas locales**:
   - **admin** / **Admin123!** (ADMIN)
   - **vendedor** / **Vendedor123!** (VENDEDOR)
5. La base de datos de prueba H2 se crea automáticamente en `datos/juans.mv.db`, y **conserva los datos entre reinicios**. Se cargan cinco productos y un cliente con fiado de prueba solo cuando están vacías las tablas. Para resetear la demo: apaga la aplicación y elimina la carpeta `datos` (esto borra tus datos locales).

### Compilación y pruebas
```bash
mvn clean test package
java -jar target/juans-pos-1.0.0.jar
```
**Si no tienes Maven o internet**, ejecuta `scripts/PROBAR-REGLAS.bat` para compilar y verificar con `javac` las reglas de venta, stock, fiado y abonos. **Esta prueba no arranca el servidor web**.

## Qué mostrar al profesor
1. Inicio de sesión y rol ADMIN / VENDEDOR.
2. Panel principal con métricas actuales.
3. Crear y editar un producto (ADMIN).
4. Consultar stock y reponer mercancía.
5. Registrar venta y comprobar que el stock se descuenta.
6. Registrar cliente habilitado para fiado, hacer venta a crédito y abonar.
7. Mostrar alertas de stock bajo y productos por vencer; marcar atendida.
8. Exportar reporte de ventas a **Excel** (ADMIN).
9. Ejecutar `mvn test` y mostrar commits locales `git log --oneline` y el repositorio subido a GitHub.

## Arquitectura y patrones
- **MVC**: Controladores HTTP (`controller/`), servicios de negocio (`service/`) y vistas HTML/CSS/JS (`resources/static/`).
- **DAO / Repository**: interfaces Spring Data JPA (`repository/`) separan acceso a datos y lógica.
- **SOLID**: responsabilidad separada por clases y dependencias por constructor hacia interfaces.
- **TDD / pruebas**: reglas puras verificadas por escenarios JUnit 5 (`src/test`); desarrollo a partir de casos positivos y negativos. No se declara cobertura porcentual sin medición externa.
- **Transacciones**: registro de ventas/abonos con `@Transactional` y bloqueo pesimista de producto/cliente para evitar concurrencia en stock y fiado.
- **Seguridad**: Spring Security con login en sesión, roles ADMIN/VENDEDOR, BCrypt, CSRF, validaciones Bean Validation, datos parametrizados por JPA, bajas lógicas y auditoría de acciones críticas. **No publica la demo en internet:** credenciales precargadas y H2 son para uso local. HTTPS, bloqueo de intentos, copias automáticas y correo/WhatsApp están pendientes de producción.

## Bibliotecas Java usadas (requisito de la rúbrica)
| Dependencia | Uso real |
|--|--|
| Google Guava | Lista inmutable de formas de pago permitidas (`NegocioService`) |
| Apache Commons Lang | Normalización y validación de cadenas (`NegocioService`) |
| Apache POI | Exportación real a Excel `.xlsx` (`ReporteController`) |
| Logback + MDC | Registros de actividad etiquetados por `usuario`, `requestId`, `operacion` y `evento` (`MdcRequestFilter`, `NegocioService`) |

El proyecto además usa Spring Boot, Spring Security, Spring Data JPA, JUnit 5 y H2/MySQL Connector/J.

## Conectar con MySQL 8 como en el diseño del Avance 2
1. En MySQL Workbench, crea una base de datos local para desarrollo llamada `juans_db`.
   ```sql
   CREATE DATABASE juans_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER 'juans_app'@'localhost' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE';
   GRANT SELECT,INSERT,UPDATE,DELETE,CREATE,ALTER,INDEX ON juans_db.* TO 'juans_app'@'localhost';
   ```
   Estos permisos amplios son **solo para desarrollo con JPA `ddl-auto=update`**. En producción usa migraciones y mínimo privilegio.
2. En PowerShell:
   ```powershell
   $env:DB_USER="juans_app"
   $env:DB_PASSWORD="TU_CLAVE"
   $env:DEMO_SEED="true"
   mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
   ```
3. Para ejecutar con datos reales establece `DEMO_SEED=false`, crea usuarios iniciales por un mecanismo seguro, usa HTTPS y despliega con migraciones. La modalidad MySQL no usa H2. La estructura física del prototipo usa nombres de categorías/proveedores directamente en producto en lugar de tablas normalizadas del diagrama de Avance 2: **requiere migración antes de declararse consistente con el esquema final**.

## Organización
```text
src/main/java/pe/juans/pos/
  model/            Entidades JPA
  repository/       Interfaces DAO
  service/          Reglas, transacciones y alertas programadas
  controller/       API REST; reportes Excel
  config/           Seguridad, manejo de errores y datos de prueba
src/main/resources/static/
  login.html        Vista 1
  index.html        Vistas 2 a 8 (SPA con pestañas)
  assets/           app.js y style.css
src/test/           Pruebas JUnit
scripts/             Inicio, pruebas y GitHub
pruebas/             Prueba Java simple sin Maven
```

## Limitaciones documentadas del prototipo
- **Interfaz implementada**: 8 vistas (login, panel, venta, inventario, edición de producto, alertas, clientes y reportes).
- **Sí implementado**: ventas, stocks, fiado/abonos, alerta diaria en base de datos, métricas, exportación Excel, roles, auditoría básica.
- **Pendiente para el 100% de todos los RF del Avance 2**: notificación por correo/WhatsApp, exportación PDF, gestión visual de usuarios/roles y proveedores como entidad aparte, mermas, bloqueo tras cinco intentos, respaldo automático, integración completa con el esquema de 12 tablas y pruebas de integración de carga/concurrencia en MySQL. Los reportes del prototipo son acumulados y no filtran por fecha.
- Es necesario que el equipo entienda y pruebe el código antes de declarar dominio y cobertura total.

## Subir evidencias reales a GitHub
```bash
git init
git add .
git commit -m "feat: primera implementación funcional JuanS POS"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/juans-pos.git
git push -u origin main
```
La **URL real de tu repositorio** y los commits **no se generan ni publican automáticamente** desde este paquete. Haz los siguientes cambios con commits `test: ...`, `fix: ...`, `docs: ...` y sube `git push` para mostrar historial. **Nunca subas contraseñas de producción**.

## Nota para la sustentación
Ejemplo: “Aplicamos MVC separando interfaz, controladores y servicio; DAO con Spring Data JPA; y SOLID separando responsabilidades. La venta corre en una transacción, bloquea el stock, valida el fiado y guarda historial. Spring Security usa roles y BCrypt. Con Apache POI exportamos el Excel, Commons valida textos, Guava define pagos y Logback registra eventos.”

## Mejora de Avance 3: MDC (Mapped Diagnostic Context)

Se añadió **MDC** con SLF4J/Logback para rastrear cada operación del backend por **usuario autenticado, identificador de petición y operación**, sin guardar contraseñas ni datos de formularios en los registros. Consulta `docs/MDC-SEGURIDAD.md` para la implementación, pruebas y evidencias. Tras actualizar, ejecuta `mvn test` y reinicia el servidor para cargar el código nuevo.

## Mejora de Avance 3: MDC (Mapped Diagnostic Context)

Se añadió **MDC** con SLF4J/Logback para rastrear cada operación del backend por **usuario autenticado, identificador de petición y operación**, sin guardar contraseñas ni datos de formularios en los registros. Consulta `docs/MDC-SEGURIDAD.md` para la implementación, pruebas y evidencias. Tras actualizar, ejecuta `mvn test` y reinicia el servidor para cargar el código nuevo.
