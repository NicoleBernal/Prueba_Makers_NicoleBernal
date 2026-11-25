# Makers Bank — Sistema de gestión de préstamos

Aplicación full stack para solicitar, consultar y administrar préstamos bancarios, desarrollada para la prueba técnica de Makers (Spring + Angular). Consta de una API REST con **Spring Boot** y una SPA con **Angular**, con autenticación JWT, autorización por roles, caché, transacciones y pruebas automatizadas.

## Funcionalidades

**Usuario**
- Iniciar sesión con correo y contraseña.
- Solicitar un préstamo indicando monto y plazo.
- Consultar únicamente sus propios préstamos y su estado (pendiente, aprobado o rechazado).

**Administrador**
- Ver y filtrar todas las solicitudes (por estado y por correo, nombre o ID).
- Aprobar o rechazar solicitudes pendientes.
- Editar (monto y plazo) y eliminar préstamos.
- Crear, consultar, editar y eliminar usuarios.

## Tecnologías

### Backend

| Área | Tecnología |
|---|---|
| Lenguaje y framework | Java 17, Spring Boot 3.3.2 |
| Capa HTTP | Spring WebFlux (Netty), controladores con `Mono` / `Flux` |
| Persistencia | Spring Data JPA, Hibernate, H2 en memoria |
| Validación | Jakarta Bean Validation (Hibernate Validator) |
| Seguridad | Spring Security, JWT (jjwt 0.12.5), BCrypt |
| Caché | Spring Cache con EHCache 3 (JCache) |
| Pruebas | JUnit 5, Mockito, AssertJ, Reactor Test (`StepVerifier`), `WebTestClient`, Spring Boot Test, Spring Security Test |
| Construcción | Maven |

### Frontend

| Área | Tecnología |
|---|---|
| Framework | Angular 21 (componentes standalone, *signals*, nuevo control de flujo `@if` / `@for`) |
| Lenguaje | TypeScript 5.9, RxJS 7.8 |
| Estado | NgRx 21 (Store, Effects, Store Devtools) |
| Formularios | Reactive Forms |
| HTTP y seguridad | `HttpClient` con interceptor JWT, guards funcionales (`authGuard`, `adminGuard`, `guestGuard`) |
| Enrutamiento | Angular Router con carga diferida (*lazy loading*) y rutas hijas |
| Componentes | Atomic Design (`atoms`, `molecules`, `organisms`, `templates`) |
| Estilos | SCSS con variables CSS, diseño responsivo |
| Pruebas | Vitest 4 con jsdom, a través del CLI de Angular |

## Arquitectura

### Backend: arquitectura hexagonal (puertos y adaptadores)

El dominio y los casos de uso no dependen de Spring, JPA ni Reactor. La infraestructura implementa los puertos que el negocio declara, de modo que las dependencias solo apuntan hacia adentro.

```text
interfaces/rest ──> application/port/in ──> domain
                            │
                            └──> application/port/out <── infrastructure
```

| Paquete | Responsabilidad |
|---|---|
| `domain/model` | Entidades y enumeraciones del negocio (`Loan`, `User`, `LoanStatus`, `UserRole` con valores `USER` y `ADMIN`). Java puro |
| `application/port/in` | Casos de uso que ofrece la aplicación (`LoanUseCase`, `UserUseCase`, `AuthUseCase`) |
| `application/port/out` | Lo que el negocio necesita del exterior (`LoanRepositoryPort`, `UserRepositoryPort`, `PasswordPort`, `TokenPort`) |
| `application/service` | Reglas de negocio, en Java puro |
| `interfaces/rest` | Controladores, DTO, mappers y adaptadores reactivos |
| `infrastructure` | JPA, seguridad (JWT y BCrypt), caché, transacciones, configuración y manejo global de errores |

**Flujo de una petición** (por ejemplo, un administrador edita un préstamo con `PUT /api/admin/loans/{id}`):

1. `JwtAuthenticationFilter` valida el token y `SecurityConfig` exige el rol `ADMIN` en `/api/admin/**` (`401` sin token, `403` sin rol).
2. `LoanController` recibe el cuerpo y lo valida con `@Valid` (`400` si no cumple).
3. `LoanReactiveAdapter` ejecuta el caso de uso en `Schedulers.boundedElastic()`.
4. El caso de uso es una cadena de decoradores: `CachedLoanUseCase` → `TransactionalLoanUseCase` → `LoanServiceImpl`.
5. `LoanServiceImpl` aplica las reglas (rol de administrador, existencia del préstamo) y guarda mediante `LoanRepositoryPort`.
6. `JpaLoanRepositoryAdapter` convierte el dominio a entidad JPA y persiste.
7. La respuesta vuelve convertida a DTO; cualquier excepción la traduce `GlobalExceptionHandler`.

**Decisiones técnicas**

- **WebFlux solo en el borde HTTP.** Atiende las peticiones sin bloquear los hilos de Netty. Como JPA es bloqueante, los adaptadores ejecutan las llamadas en `boundedElastic`. La persistencia no es reactiva de extremo a extremo.
- **Entidades JPA separadas del dominio.** El negocio no queda atado a anotaciones de persistencia; el costo es un mapper adicional.
- **Transacciones y caché como decoradores.** Los servicios quedan sin anotaciones de Spring y se pueden probar solo con Mockito. `ApplicationConfig` arma la cadena.
- **Concurrencia.** `@Version` aplica bloqueo optimista; un conflicto se devuelve como `409`.
- **Defensa en profundidad.** Las rutas administrativas se protegen en Spring Security y el rol se vuelve a validar en los casos de uso críticos.

### Frontend: SPA con NgRx y Atomic Design

```text
src/app
├── core/        Interceptor JWT, guards, sesión y servicios HTTP
├── store/       NgRx: auth, loans, users (acciones, reducers, efectos, selectores)
├── features/    Páginas contenedoras: login, dashboard, admin-loans, admin-users
└── shared/      Modelos y componentes reutilizables (atoms → molecules → organisms → templates)
```

**Rutas**

| Ruta | Acceso | Contenido |
|---|---|---|
| `/login` | Solo sin sesión | Inicio de sesión |
| `/dashboard` | Autenticado | Solicitar préstamo y ver el estado de los propios |
| `/admin/loans` | Administrador | Gestión de solicitudes (los filtros viven en la URL: `?status=PENDING&q=ana`) |
| `/admin/users` | Administrador | Gestión de usuarios |

**Decisiones técnicas**

- **NgRx** centraliza la sesión, los préstamos y los usuarios; las páginas leen con *signals* (`selectSignal`) y los efectos hablan con la API.
- **Interceptor JWT** añade el token a cada petición y cierra la sesión ante un `401`.
- **Guards funcionales** protegen las rutas por sesión y rol. Tras el login se vuelve a la página solicitada solo si es una ruta interna permitida para el rol.
- **Experiencia de usuario:** modales accesibles (foco atrapado, `Esc`), diálogo de confirmación propio, avisos flotantes, esqueletos de carga y tablas que pasan a tarjetas en móvil.
- **Compromiso conocido:** el token se guarda en `localStorage` (práctica habitual en esta clase de pruebas), con la exposición a XSS que eso implica. La sesión se descarta si el token está vencido o no coincide con el usuario guardado.

## Requisitos

- Java 17 o superior y Maven 3.8 o superior.
- Node.js `^20.19.0`, `^22.12.0` o `>=24.0.0` y npm 11.

## Ejecución local

Se necesitan dos terminales abiertas desde la raíz del repositorio.

**1. Backend** (API en `http://localhost:8080`)

```powershell
cd backend
mvn spring-boot:run
```

**2. Frontend** (interfaz en `http://localhost:3000`)

```powershell
cd frontend
npm.cmd ci
npm.cmd start
```

El frontend consume `http://localhost:8080/api` (constante `API_URL` en `frontend/src/app/core/services/api.services.ts`) y el backend solo acepta peticiones del origen `http://localhost:3000`.

### Usuarios de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Usuario | `usuario@test.com` | `123` |
| Administrador | `admin@test.com` | `123` |

Se cargan automáticamente, junto con tres préstamos de ejemplo (pendiente, aprobado y rechazado), cuando no está activo el perfil `prod`. Los datos viven en memoria: se reinician al detener el backend.

### Configuración

| Variable | Valor local por defecto | Descripción |
|---|---|---|
| `JWT_SECRET` | `makers-bank-development-secret-key-2026` | Clave de firma del JWT. Debe reemplazarse fuera del entorno local |
| `JWT_EXPIRATION_MS` | `3600000` | Vigencia del token (1 hora) |
| `DEMO_PASSWORD` | `123` | Contraseña de las cuentas de demostración |

```powershell
$env:JWT_SECRET="una-clave-privada-de-al-menos-32-bytes"
mvn spring-boot:run
```

## API REST

Todas las rutas, salvo el login, requieren `Authorization: Bearer <token>`.

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| `POST` | `/api/auth/login` | Público | Autenticación y emisión del JWT |
| `POST` | `/api/loans` | Autenticado | Crear una solicitud propia |
| `GET` | `/api/loans/my-loans` | Autenticado | Listar los préstamos propios |
| `GET` | `/api/loans/{id}` | Propietario | Consultar un préstamo propio |
| `GET` | `/api/admin/loans` | Administrador | Listar todos los préstamos |
| `GET` | `/api/admin/loans/pending` | Administrador | Listar solicitudes pendientes |
| `PUT` | `/api/admin/loans/{id}` | Administrador | Editar monto y plazo |
| `DELETE` | `/api/admin/loans/{id}` | Administrador | Eliminar un préstamo |
| `PATCH` | `/api/admin/loans/{id}/approve` | Administrador | Aprobar una solicitud pendiente |
| `PATCH` | `/api/admin/loans/{id}/reject` | Administrador | Rechazar una solicitud pendiente |
| `GET` | `/api/admin/users` | Administrador | Listar usuarios |
| `GET` | `/api/admin/users/{id}` | Administrador | Consultar un usuario |
| `POST` | `/api/admin/users` | Administrador | Crear un usuario |
| `PUT` | `/api/admin/users/{id}` | Administrador | Editar un usuario |
| `DELETE` | `/api/admin/users/{id}` | Administrador | Eliminar un usuario |

Cuerpo para solicitar o editar un préstamo:

```json
{
  "amount": 5000.00,
  "termMonths": 12
}
```

**Reglas de negocio y validaciones**

- Monto entre `100.00` y `1,000,000.00`, con máximo dos decimales.
- Plazo entre 3 y 60 meses.
- Solo una solicitud `PENDING` puede aprobarse o rechazarse.
- Un usuario solo puede consultar sus propios préstamos.
- No se puede repetir un correo ni eliminar el propio usuario administrador.
- La edición de un préstamo cambia monto y plazo, pero no su estado.

**Errores HTTP**

| Código | Cuándo |
|---|---|
| `400` | Validación fallida, JSON mal formado |
| `401` | Sin token, token inválido o credenciales incorrectas |
| `403` | Rol insuficiente o préstamo de otro usuario |
| `404` | Recurso o ruta inexistente |
| `409` | Conflicto de negocio (por ejemplo, préstamo ya resuelto) o de concurrencia |
| `500` | Error inesperado (se registra en el log) |

## Caché y transacciones

- **EHCache:** las consultas por ID se conservan 5 minutos y las listas por usuario 3 minutos.
- Crear, editar, aprobar, rechazar o eliminar invalida las entradas afectadas.
- Las operaciones de escritura se ejecutan dentro de transacciones.
- La base H2 usa `DB_CLOSE_DELAY=-1` para no perder los datos si el pool cierra sus conexiones mientras la aplicación sigue viva.

## Pruebas

```powershell
cd backend
mvn test
```

```powershell
cd frontend
npm.cmd test -- --watch=false
npm.cmd run build
```

**Backend: 41 pruebas**

| Tipo | Qué cubre |
|---|---|
| Unitarias | Reglas de aprobación, rechazo y edición; CRUD de usuarios; adaptadores reactivos con `StepVerifier` |
| Decoradores | Invalidación de caché y commit/rollback de transacciones |
| Seguridad | Filtro JWT, `401` y `403` |
| Persistencia | Consultas reales sobre H2 con `@DataJpaTest` |
| Integración | `@SpringBootTest` con `WebTestClient`: flujo completo con JWT, validaciones, errores HTTP y caché |

**Frontend: 23 pruebas** (Vitest): reducers de NgRx, sesión y JWT vencido, ruta de retorno segura, y componentes (formularios, tabla, modal, confirmación, filtros, avisos).

## Estructura del repositorio

```text
.
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/makers/loans/
│       │   ├── domain/
│       │   ├── application/
│       │   ├── interfaces/
│       │   └── infrastructure/
│       ├── main/resources/        (application.yml, ehcache.xml)
│       └── test/
├── frontend/
│   ├── package.json
│   └── src/app/
│       ├── core/
│       ├── features/
│       ├── shared/
│       └── store/
├── Hoja de respuestas Nicole Bernal.pdf   (parte teórica)
└── README.md
```

## Parte teórica

Las respuestas de la parte teórica están en [`Hoja de respuestas Nicole Bernal.pdf`](Hoja%20de%20respuestas%20Nicole%20Bernal.pdf).
