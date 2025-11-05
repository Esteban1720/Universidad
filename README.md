# 🏥 Gestión de Citas Médicas

Aplicación web desarrollada en **Java (Spring Boot)** para la gestión de citas médicas. Permite registrar pacientes, médicos y clínicas, gestionar horarios y generar reportes de facturación e historial médico.

---

## 📋 Descripción del proyecto

**Gestión de Citas Médicas** es una aplicación web que facilita la administración de citas entre pacientes y médicos, incluyendo autenticación por roles, generación de informes y envío de notificaciones por correo.  
El objetivo es digitalizar los procesos clínicos básicos y ofrecer una plataforma moderna y segura para la gestión hospitalaria.

---

## ⚙️ Tecnologías utilizadas

| Categoría | Tecnología |
|------------|-------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3 |
| Construcción | Maven |
| Base de datos | MariaDB |
| Persistencia | Spring Data JPA |
| Plantillas | Thymeleaf |
| Seguridad | Spring Security |
| Validación | Spring Boot Validation |
| Reportes | JasperReports |
| Mapeo DTOs | MapStruct |
| Correos | Spring Mail (JavaMailSender) |
| Archivos Excel | Apache POI |

---

## 🧩 Estructura del proyecto

```
src/
 ├── main/java/com/co/gestiondecitasmedicas/     # Código fuente
 ├── main/resources/templates/                   # Vistas Thymeleaf
 │    ├── home.html
 │    ├── index.html
 │    ├── login.html
 │    ├── registro.html
 │    ├── seleccionar-rol.html
 │    ├── clinica/
 │    ├── medico/
 │    └── paciente/
 ├── main/resources/informes/                    # Reportes Jasper (.jrxml / .jasper)
 └── main/resources/application.properties       # Configuración
```

---

## 🚀 Ejecución del proyecto

### Requisitos previos
- Java 17 (JDK)
- Maven
- MariaDB

### Configuración
Edita `src/main/resources/application.properties` con los datos de tu base de datos:

```properties
spring.datasource.url=jdbc:mariadb://localhost:3306/gestiondecitasmedicas
spring.datasource.username=root
spring.datasource.password=tu_password
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

### Ejecutar el proyecto
```bash
.\mvnw spring-boot:run
```
O bien:
```bash
java -jar target/gestiondecitasmedicas.jar
```

---

## 🧠 Herramientas tecnológicas utilizadas

- IntelliJ IDEA / Eclipse  
- Miro (tablero de planificación)  
- Git / GitHub para control de versiones  
- JasperSoft Studio (diseño de reportes)  
- MariaDB Workbench  

📌 **Tablero Miro:** https://miro.com/welcomeonboard/WW80VURiczJXTi9pazh1WHRDaWJZUElEOHIxdDdUMXJJZ1A2cVhNaXFDR1V4Ti9qUXh1bVoyelIxK2RYUmpiOEpyRXFwYUFLZ3pyMkRjRmFtelhFbmRTdW50VVNFVUhMTTlMR2NrMFZ1RHNvZmlBd1VZVGRKVkptKzBJaHRWTVBNakdSWkpBejJWRjJhRnhhb1UwcS9BPT0hdjE=?share_link_id=928082782380

---

## 👥 Colaboradores

- Equipo de desarrollo: *Metodología_Scrum_Equipo 1*


## 🛡️ Buenas prácticas

- No subir contraseñas al repositorio.
- Usar variables de entorno para credenciales.
- Gestionar migraciones con Flyway o Liquibase.
- Mantener el código limpio y documentado.


### Resumen técnico

- Framework: Spring Boot 3 (starter parent)
- Lenguaje: Java 17
- Build: Maven (incluye `spring-boot-maven-plugin`)
- Persistencia: Spring Data JPA + MariaDB
- Vistas: Thymeleaf
- Validación: spring-boot-starter-validation
- Seguridad: Spring Security
- Informes: JasperReports (.jrxml / .jasper)
- MapStruct para mapeo DTO <-> entidad
- Email: Spring Boot Mail (JavaMailSender)
- Excel: Apache POI (poi-ooxml)

Dependencias relevantes están en `pom.xml` (por ejemplo JasperReports 7.0.3, MariaDB JDBC driver, MapStruct).

### Estructura del proyecto (resumen)

- `src/main/java/com/co/gestiondecitasmedicas/` - código fuente
- `src/main/resources/templates/` - plantillas Thymeleaf (p. ej. `home.html`, `index.html`, `login.html`, `registro.html`, `seleccionar-rol.html` y subcarpetas `clinica/`, `medico/`, `paciente/`)
- `src/main/resources/informes/` - archivos Jasper (`Facturacion.jasper`, `Facturacion.jrxml`, `HistorialMedico.jasper`, `HistorialMedico.jrxml`)
- `src/main/resources/application.properties` - configuración principal

### Requisitos previos

- Java 17 (JDK)
- Maven (se proveen scripts `mvnw` y `mvnw.cmd` en el repo)
- MariaDB (o MySQL compatible)
- Opcional: JasperSoft Studio para editar `.jrxml`

### Configuración

La configuración principal está en `src/main/resources/application.properties`. Valores importantes:

- `spring.datasource.url` - URL de la base de datos (por defecto: `jdbc:mariadb://localhost:3306/gestiondecitasmedicas`)
- `spring.datasource.username` / `spring.datasource.password` - credenciales de la BD
- `spring.jpa.hibernate.ddl-auto` - actualmente `validate` (si es la primera vez, puede cambiarse a `update` o `create` para crear tablas automáticamente durante desarrollo)
- `server.port` - puerto del servidor (por defecto `8080`)
- `spring.mail.*` - configuración SMTP para el envío de correos

Nota importante de seguridad: en este repositorio aparece una contraseña en `application.properties`. No deje credenciales en el control de versiones. Use variables de entorno o un archivo de configuración externo `application-local.properties` no versionado.

Ejemplo de variables de entorno (PowerShell) para no almacenar credenciales directamente:

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mariadb://localhost:3306/gestiondecitasmedicas'
$env:SPRING_DATASOURCE_USERNAME = 'root'
$env:SPRING_DATASOURCE_PASSWORD = 'tu_password_segura'
$env:SPRING_MAIL_USERNAME = 'tu_correo@example.com'
$env:SPRING_MAIL_PASSWORD = 'tu_password_smtp'
```

O puede pasar propiedades al ejecutar Maven/Java:

```powershell
# Ejecutar con el wrapper de Windows (ejemplo temporal)
# $env:SPRING_DATASOURCE_PASSWORD='miPass'; .\mvnw.cmd spring-boot:run
```

### Inicialización de la base de datos

Actualmente `spring.jpa.hibernate.ddl-auto=validate`, lo que significa que las tablas deben existir y el esquema debe coincidir con las entidades. Para desarrollo rápido:

- Cambie a `spring.jpa.hibernate.ddl-auto=update` o `create` para que Hibernate cree/actualice las tablas automáticamente.
- O bien importe un script SQL con la estructura de la base de datos si lo tiene.

Recomendación: en producción usar `validate` y migraciones gestionadas con Flyway o Liquibase.

### Ejecutar la aplicación

En Windows (PowerShell), desde la raíz del proyecto:

```powershell
# Ejecutar con el wrapper de Windows
.\mvnw.cmd spring-boot:run

# Compilar y crear jar
.\mvnw.cmd -DskipTests package

# Ejecutar el jar producido
java -jar target\*.jar
```

Si prefiere Maven instalado globalmente:

```powershell
mvn spring-boot:run
mvn -DskipTests package
```

### Pruebas

Ejecutar tests con:

```powershell
.\mvnw.cmd test
```

### Endpoints / UI

La aplicación mezcla endpoints REST/servicios y controladores MVC que devuelven vistas Thymeleaf. En `src/main/resources/templates` están las páginas disponibles:

- Páginas públicas: `index.html`, `login.html`, `registro.html`, `seleccionar-rol.html`, `home.html`
- Roles y dashboards (carpeta `clinica`, `medico`, `paciente`): páginas para ver/editar citas, historial, facturación, etc.

Rutas exactas dependen de los controladores. Para localizar una URL concreta, buscar en el código las anotaciones `@Controller` / `@RestController` y los `@RequestMapping`, `@GetMapping`, `@PostMapping`.

### Informes (JasperReports)

Los `.jrxml` y `.jasper` están en `src/main/resources/informes/`. Para personalizarlos:

- Abra el `.jrxml` en JasperSoft Studio, edite el diseño y compile nuevamente a `.jasper` si lo modifica.
- Desde Java la aplicación puede cargar el `.jasper` y rellenarlo con parámetros y una conexión a la BD para generar PDF/HTML/Excel.

Si hay problemas con dependencias de PDF, ver `pom.xml` (itext 2.1.7 incluído para compatibilidad con jasperreports-pdf 7.0.3).

### Correo electrónico

La app usa Spring Mail. Configure `spring.mail.username` y `spring.mail.password` con credenciales SMTP válidas. Para Gmail es posible que necesite una contraseña de aplicación (app password) o ajustar la configuración de seguridad de la cuenta.

### MapStruct

MapStruct está configurado en `pom.xml` y facilita el mapeo entre entidades y DTOs. El procesador de anotaciones está incluido en la configuración del `maven-compiler-plugin`.

### Desarrollo y debugging

- Spring Boot DevTools está incluido para facilitar reload automático en desarrollo.
- Active logging SQL con `spring.jpa.show-sql=true` para ver las consultas Hibernate en consola.

### Buenas prácticas / seguridad

- No subir contraseñas ni secretos al repositorio.
- Usar HTTPS y proteger endpoints sensibles con Spring Security.
- Añadir gestión de migraciones (Flyway/Liquibase) para controlar cambios de esquema.

### Posibles mejoras (sugeridas)

- Añadir scripts SQL de inicialización (roles, usuarios de ejemplo).
- Externalizar configuración sensible usando `spring.config.import=optional:configserver:` o variables de entorno.
- Integrar Flyway/Liquibase para migraciones.
- Añadir pruebas de integración y cobertura mínima.

### ¿Dónde buscar código importante?

- Seguridad y login: buscar clases en el paquete `security` o archivos que extiendan `WebSecurityConfigurerAdapter` / `SecurityFilterChain`.
- Controladores MVC: buscar `@Controller` y plantillas en `templates/`.
- Repositorios: interfaces que extienden `JpaRepository`.

### Soporte y contacto

Si necesitas que documente endpoints concretos, añadir scripts SQL, o generar ejemplos de .env / `application-local.properties`, dime qué prefieres y lo agrego.

---

Licencia: revisa el repo para ver si hay un archivo LICENSE; si no hay, añade la que prefieras.
