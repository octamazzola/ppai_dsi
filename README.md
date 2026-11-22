# PPAI DSI 2026 — Caso de Uso N° 36: "Consultar seguimiento de bolsines"

Implementación del **Proyecto Práctico de Aplicación Integrador (PPAI)** de la cátedra **Diseño de Sistemas de Información (DSI) — UTN Facultad Regional Córdoba**, Curso **3K3**, **Grupo 9 + 11**.

El proyecto implementa de punta a punta el **Caso de Uso N° 36: Consultar seguimiento de bolsines**, aplicando el enfoque orientado a objetos + API REST propuesto por la cátedra, persistencia en base de datos **H2 en memoria (Spring Data JPA)**, el patrón de diseño **Adapter de salida** consumiendo la **API real de GPS Tracker de la cátedra**, y un **frontend mínimo funcional en React + Vite**.

---

## Tabla de contenidos

- [Arquitectura General](#arquitectura-general)
- [Patrones y Decisiones de Diseño](#patrones-y-decisiones-de-diseño)
- [Estructura del Repositorio](#estructura-del-repositorio)
- [Requisitos Previos](#requisitos-previos)
- [Cómo Ejecutar el Proyecto (3 Servicios)](#cómo-ejecutar-el-proyecto-3-servicios)
- [Base de Datos H2 y Datos Precargados](#base-de-datos-h2-y-datos-precargados)
- [Endpoints REST del Backend](#endpoints-rest-del-backend)
- [Guía de Pruebas: Flujo Básico y Flujos Alternativos (A1–A5)](#guía-de-pruebas-flujo-básico-y-flujos-alternativos-a1a5)
- [Ejecutar Tests Automatizados](#ejecutar-tests-automatizados)

---

## Arquitectura General

El sistema está organizado en **3 aplicaciones independientes** que corren en paralelo en entorno local:

| Carpeta | Tecnología | Puerto | Rol |
| :--- | :--- | :---: | :--- |
| `gps-tracker-api/` | Java 17 + Spring Boot 3.3.5 | `8080` | **API oficial de la cátedra** ([LiberaeMentis/dsi-api-bolsines](https://github.com/LiberaeMentis/dsi-api-bolsines)) que simula los 3 modelos de GPS Tracker con endpoints, `X-API-Key` y formatos de respuesta incompatibles. |
| `backend/` | Java 17 + Spring Boot 3.3.5 + Spring Data JPA + H2 | `8081` | **Backend del CU 36**: capa Boundary (`@RestController`), Control (`@Service`), Entidades JPA con Patrón Experto, Patrón Adapter de salida contra `localhost:8080`, `IGoogleMaps` simulado e inclusión del CU 31. |
| `frontend/` | React 19 + Vite 6 | `5173` | **Frontend mínimo funcional** (sin diseño UX en esta primera instancia) para probar el flujo completo del CU 36, cambiar entre los 3 proveedores GPS y ejecutar todos los flujos alternativos. |

```text
[Frontend React :5173]
        │  HTTP REST / JSON (DTOs)
        ▼
[Backend PPAI :8081]  ──►  H2 en memoria (JPA + data.sql)
   ├── PantallaConsultarSeguimientoBolsines (@RestController <<boundary>>)
   ├── GestorConsultarSeguimientoBolsines   (@Service <<control>>)
   ├── Modelo de Dominio                    (<<entity>> con Patrón Experto)
   ├── IGoogleMaps ──► ServidorGoogleMaps   (<<boundary>> simulado)
   ├── GestorNotificarUbicacionBolsin       (<<include>> CU 31 simulado)
   └── InterfazGPSTracker ──► IAdaptadorGPSTracker (Patrón Adapter)
           ├── AdaptadorGPSTrackerXTR4500L     ──► ServicioGPSTrackerXTR4500L     ──► :8080/api/xtr-4500l/getBolsinLocation
           ├── AdaptadorGPSTrackerNavTrackQX7A ──► ServicioGPSTrackerNavTrackQX7A ──► :8080/api/navtrack-qx-7a/retrieveTrackingData
           └── AdaptadorGPSTrackerGeoPulseMTR900 ─► ServicioGPSTrackerGeoPulseMTR900 ► :8080/api/geopulse-mtr-900/fetchCargoPositions
```

---

## Patrones y Decisiones de Diseño

### 1. Mapeo BCE (Boundary–Control–Entity) a API REST
Siguiendo el ejemplo oficial de la cátedra (*Administración de Campos — Implementación de un Caso de Uso*):
- **`<<boundary>>`**: `PantallaConsultarSeguimientoBolsines` (`@RestController`) expone los endpoints REST `/api/seguimiento/*` y nunca recibe ni devuelve entidades de dominio directamente (utiliza **DTOs**).
- **`<<control>>`**: `GestorConsultarSeguimientoBolsines` (`@Service`) orquesta el flujo del CU 36 y conserva el estado temporal de la consulta activa.
- **`<<entity>>`**: Las clases de `model/` implementan la lógica de negocio aplicando el **Patrón Experto en Información (GRASP)** y se persisten con JPA en H2.
- **Eliminación de callbacks `Gestor → Pantalla`**: propios del modelo REST, los resultados se devuelven en el cuerpo JSON de cada respuesta HTTP.

### 2. Patrón Experto en Información (GRASP)
En lugar de concentrar condicionales en el Gestor inspeccionando getters ajenos, cada entidad responde por la información que conoce:
- `Sesion.esActual()` → verifica `fechaHoraFin == null`.
- `Empleado.esTuUsuario(Usuario)` → determina si corresponde al usuario logueado.
- `Empleado.getCM()`, `Empleado.esTuCM(ComisionMedica)` y `Empleado.sosGCM()` → delega en `Rol.esGCM()`.
- `Bolsin.esTuCMOrigen(ComisionMedica)` → comprueba coincidencia de CM origen.
- `Bolsin.sosEnviado()` → delega en `Bolsin.buscarCambioEstadoActual()` → `CambioEstadoBolsin.sosActual()` y `CambioEstadoBolsin.sosEnviado()` → `Estado.sosEnviado()` (validando `Estado.esAmbitoBolsin()`).
- `Bolsin.tienePrecinto(String)` y `Bolsin.tieneCMDestino(String)` → resuelven los filtros del Paso 6.

### 3. Patrón de Diseño GoF: Adapter de Salida (`IAdaptadorGPSTracker`)
Los 3 modelos de GPS Tracker de `dsi-api-bolsines` presentan firmas, parámetros, API keys y formatos incompatibles:

| Modelo GPS | Endpoint Real (`:8080`) | Parámetros | Formato Crudo de Respuesta | Adaptador Concreto |
| :--- | :--- | :--- | :--- | :--- |
| **XTR-4500L** | `/api/xtr-4500l/getBolsinLocation` | `numeroBolsin`, `codigoComisionMedicaOrigen` | JSON `{ "items": [ { ... } ] }` | `AdaptadorGPSTrackerXTR4500L` |
| **NavTrack QX-7A** | `/api/navtrack-qx-7a/retrieveTrackingData` | `numeroBolsin`, `codigoComisionMedicaDestino` | `text/plain` CSV (`101,-31.42,-64.18,...`) | `AdaptadorGPSTrackerNavTrackQX7A` |
| **GeoPulse MTR-900** | `/api/geopulse-mtr-900/fetchCargoPositions` | `numeroBolsin` | Matriz JSON `[[101, -31.42, -64.18, "..."]]` | `AdaptadorGPSTrackerGeoPulseMTR900` |

Todos implementan `IAdaptadorGPSTracker` (`recibirUbicacionBolsin()`, `solicitarUbicacionBolsin()`, `adaptarUbicacionBolsin()`), traduciendo cada respuesta al mismo formato matricial uniforme `String[][]`. Así, `InterfazGPSTracker` y `GestorConsultarSeguimientoBolsines` permanecen desacoplados de los proveedores externos.

### 4. Decisiones de Consistencia Acordadas
- **Unificación de nombres**: `PantallaConsultarSeguimientoBolsines` y `GestorConsultarSeguimientoBolsines` en todo el proyecto, y `CambioEstadoBolsin` según el Modelo de Dominio oficial.
- **Ampliación de firma en `InterfazGPSTracker`**: `obtenerUbicacionBolsin(int[] nroBolsines, String codCMOrigen, String[] codCMDestinos, String modeloGPS)` para soportar `NavTrack QX-7A` (que exige `codigoComisionMedicaDestino` por cada bolsín) y permitir alternar proveedores en vivo.
- **Destinatario de correo (CU 31)**: según la ERS oficial (pasos 8 y 10), la notificación se envía al **Gerente de la Comisión Médica destino** (`empleado.esTuCM(bolsin.obtenerCMDestino()) && empleado.sosGCM()`).
- **Mapa (`IGoogleMaps`)**: implementado como boundary simulado en el backend (`IGoogleMaps` → `ServidorGoogleMaps`) respetando el diagrama de secuencia sin requerir API Key paga.

---

## Estructura del Repositorio

```text
.
├── README.md
├── .gitignore
├── gps-tracker-api/                  # API GPS Tracker oficial de la cátedra (puerto 8080)
├── backend/                          # Backend CU 36: Spring Boot + JPA + H2 (puerto 8081)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── resources/
│       │   │   ├── application.properties
│       │   │   └── data.sql          # Datos semilla sincronizados con gps-tracker-api
│       │   └── java/ar/edu/utn/frc/dsi/bolsines/
│       │       ├── BolsinesApplication.java
│       │       ├── config/           # CorsConfig, GpsApiConfig
│       │       ├── controller/       # PantallaConsultarSeguimientoBolsines
│       │       ├── service/          # GestorConsultarSeguimientoBolsines, GestorNotificarUbicacionBolsin (CU 31)
│       │       ├── boundary/         # InterfazGPSTracker, IGoogleMaps
│       │       ├── adapter/          # IAdaptadorGPSTracker + 3 adaptadores concretos
│       │       ├── external/         # 3 clientes RestClient GPS + ServidorGoogleMaps
│       │       ├── model/            # Entidades JPA con Patrón Experto
│       │       ├── repository/       # Spring Data JPA Repositories
│       │       └── dto/              # Request / Response DTOs
│       └── test/
│           └── java/ar/edu/utn/frc/dsi/bolsines/BolsinesCu36Tests.java
└── frontend/                         # Frontend mínimo React + Vite (puerto 5173)
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── services/seguimientoService.js
```

---

## Requisitos Previos

- **Java 17 o superior** (probado en Java 25 LTS)
- **Maven 3.6+** (o utilizar `mvnw.cmd` en `gps-tracker-api`)
- **Node.js 18+** y **npm**
- Puertos libres: **`8080`**, **`8081`** y **`5173`**

---

## Cómo Ejecutar el Proyecto (3 Servicios)

Abrir **3 terminales** en la carpeta raíz del repositorio:

### Terminal 1 — API Oficial de GPS Tracker (puerto `8080`)
```powershell
cd gps-tracker-api
mvn spring-boot:run
```
> Verificar en: `http://localhost:8080/health` (debe responder `{"status":"ok"}`).

### Terminal 2 — Backend CU 36 con Spring Boot + H2 (puerto `8081`)
```powershell
cd backend
mvn spring-boot:run
```
> API disponible en: `http://localhost:8081/api/seguimiento`  
> Consola H2 disponible en: `http://localhost:8081/h2-console`

### Terminal 3 — Frontend mínimo en React + Vite (puerto `5173`)
```powershell
cd frontend
npm install
npm run dev
```
> Abrir en el navegador: `http://localhost:5173`

---

## Base de Datos H2 y Datos Precargados

El backend utiliza **H2 en memoria** con creación automática de esquema por JPA (`create-drop`) y precarga vía `backend/src/main/resources/data.sql`.

### Acceso a la Consola H2
- **URL**: `http://localhost:8081/h2-console`
- **JDBC URL**: `jdbc:h2:mem:bolsinesdb`
- **User Name**: `sa`
- **Password**: *(vacía)*

### Datos precargados (`data.sql`)
Los códigos de Comisiones Médicas y números de bolsín están **sincronizados con `gps-tracker-api`**:

| Bolsín | Precinto | CM Origen | CM Destino | Estado Actual | Propósito en la demo |
| :---: | :---: | :---: | :---: | :---: | :--- |
| `101` | `PRE-101` | `CM-CBA-01` | `CM-ROS-02` | `Enviado` | **Flujo Básico** (existe en los 3 modelos de `gps-tracker-api`) |
| `999` | `PRE-999` | `CM-CBA-01` | `CM-MDZ-03` | `Enviado` | **Flujo Alternativo A3** (no existe en `gps-tracker-api` → 404 controlado) |
| `120` | `PRE-120` | `CM-CBA-01` | `CM-BA-01` | `Cerrado` | Control de filtro `Bolsin.sosEnviado()` (no se muestra) |
| `121` | `PRE-121` | `CM-CBA-01` | `CM-ROS-02` | `Recibido` *(antes Enviado)* | Control de filtro `CambioEstadoBolsin.sosActual()` (no se muestra) |
| `102` | `PRE-102` | `CM-ROS-02` | `CM-MDZ-03` | `Enviado` | Control de `esTuCMOrigen()` (visible solo para `eb_ros`) |
| `103` | `PRE-103` | `CM-MDZ-03` | `CM-BA-01` | `Enviado` | Flujo limpio para sesión `eb_mdz` |
| `110` | `PRE-110` | `CM-CBA-02` | `CM-ROS-02` | `Enviado` | Control de otra CM Córdoba |

**Sesiones de Encargado de Bolsines (EB) disponibles desde el selector:**
- `eb_cba` (`CM-CBA-01`): Flujo básico (`101`) + A3 (`999`) + filtros de estado.
- `eb_ros` (`CM-ROS-02`): Flujo limpio (`102`) en los 3 modelos GPS.
- `eb_mdz` (`CM-MDZ-03`): Flujo limpio (`103`) en los 3 modelos GPS.
- `eb_juj` (`CM-JUJ-01`): Sin bolsines enviados → prueba directa de **Flujo Alternativo A1**.

### Cómo agregar o quitar datos precargados (`data.sql`)

Todos los datos iniciales se cargan automáticamente al iniciar el backend desde `backend/src/main/resources/data.sql` (al usar `spring.jpa.hibernate.ddl-auto=create-drop`, cada reinicio recrea las tablas en H2 y vuelve a ejecutar el script).

- **Orden de dependencias (claves foráneas)** a respetar:  
  `provincia` → `localidad` → `comision_medica` → `rol` → `usuario` → `empleado` → `sesion` → `estado` → `bolsin` → `cambio_estado_bolsin`.
- **Para agregar un bolsín nuevo**:
  1. Agregar un `INSERT INTO bolsin (...)` con un `numero_bolsin` único y códigos de `cm_origen_codigo` / `cm_destino_codigo` existentes en `comision_medica`.
  2. Agregar al menos un `INSERT INTO cambio_estado_bolsin (...)` con `fecha_hora_fin = NULL` y `estado_id = 3` (`Enviado`) para que sea considerado su estado actual.
  3. *(Opcional)* Para que el GPS Tracker devuelva coordenadas válidas (y no dispare A3), el `numero_bolsin` y los códigos de CM deben existir también en `gps-tracker-api/src/main/java/ar/edu/dsi/gpstracker/repository/InMemoryBolsinRepository.java`.
- **Para quitar un bolsín o empleado**:
  - Eliminar primero las filas hijas que lo referencian (por ejemplo, sus `INSERT INTO cambio_estado_bolsin` antes de borrar el `INSERT INTO bolsin`) para evitar errores de integridad referencial al levantar H2.
- **Aplicar cambios**: guardar `data.sql` y reiniciar el backend (`Ctrl + C` y `mvn spring-boot:run`). *(Si se modifican los datos de prueba por defecto, revisar las aserciones de `BolsinesCu36Tests.java`).*

---

## Endpoints REST del Backend

Base URL: `http://localhost:8081/api/seguimiento`

| Método | Endpoint | Descripción |
| :---: | :--- | :--- |
| `GET` | `/sesiones` | Lista las sesiones demo disponibles para alternar el EB logueado. |
| `POST` | `/iniciar` | Inicia el CU 36 (`{"modeloGPS":"XTR-4500L","nombreUsuario":"eb_cba"}`). Busca CM, bolsines enviados, consulta GPS vía Adapter y arma el mapa vía `IGoogleMaps`. |
| `POST` | `/filtrar` | Filtra por precinto y/o CM destino (`{"numeroPrecinto":"PRE-101","codigoCMDestino":""}`). Cubre Paso 6 y **A2**. |
| `POST` | `/seleccionar-bolsin` | Selecciona un bolsín (`{"numeroBolsin":101}`). Devuelve confirmación para notificar al GCM destino o aviso **A3**. |
| `POST` | `/confirmar-correo` | Confirma (`{"confirmar":true}`) ejecutando `<<include>>` **CU 31**, o rechaza (`{"confirmar":false}`) ejecutando **A5**. |
| `POST` | `/cancelar` | Ejecuta **Flujo Alternativo A4** y finaliza limpiando el estado del gestor. |

---

## Guía de Pruebas: Flujo Básico y Flujos Alternativos (A1–A5)

Con los 3 servicios levantados en `http://localhost:5173`:

1. **Flujo Básico + Polimorfismo del Patrón Adapter**:
   - Elegir `eb_cba` (`CM-CBA-01`), seleccionar **XTR-4500L** y presionar **Consultar seguimiento de bolsines**.
   - Observar en la columna *"Respuesta cruda API GPS"* el JSON de `XTR-4500L` normalizado a coordenadas `(-31.4201, -64.1888)` para el Bolsín `#101`.
   - Repetir seleccionando **NavTrack QX-7A** (recibe CSV texto plano `101,-31.4201,-64.1888,2026-03-07T12:30`) y **GeoPulse MTR-900** (recibe matriz JSON `[[101,...]]`): los 3 producen la misma salida uniforme.
   - Presionar **Seleccionar** en el Bolsín `#101` → en el diálogo modal presionar **Confirmar y enviar correo (CU 31)** → verificar el comprobante enviado a `gcm.rosario02@organismo.gob.ar` (GCM de la CM destino `CM-ROS-02`).
2. **Flujo Alternativo A1 (No se encuentran bolsines en estado Enviado)**:
   - Cambiar la sesión a `eb_juj` (`CM-JUJ-01`) y presionar **Consultar seguimiento de bolsines**.
3. **Flujo Alternativo A2 (No se encuentra bolsín con el número de precinto ingresado)**:
   - Con una consulta activa, escribir `PRE-000` en *Número de precinto* y presionar **Aplicar filtro**.
4. **Flujo Alternativo A3 (El GPS Tracker no pudo informar ubicación del bolsín)**:
   - Con sesión `eb_cba`, observar el aviso A3 sobre el Bolsín `#999` (404 desde `gps-tracker-api`) y presionar **Seleccionar** en `#999` para verificar el bloqueo controlado de la notificación.
5. **Flujo Alternativo A4 (El EB cancela la ejecución del caso de uso)**:
   - Con una consulta activa, presionar **Cancelar CU (A4)**.
6. **Flujo Alternativo A5 (El EB no envía correo electrónico)**:
   - Seleccionar el Bolsín `#101` y en el diálogo modal presionar **No enviar correo (A5)**.

---

## Ejecutar Tests Automatizados

```powershell
cd backend
mvn test
```

Los tests en `BolsinesCu36Tests` verifican:
- Carga del contexto Spring Boot + esquema JPA en H2 + `data.sql`.
- Filtrado por Patrón Experto (`esTuCMOrigen`, `sosEnviado`, `sosActual`, `esAmbitoBolsin`, `esTuCM`, `sosGCM`, búsqueda del GCM destino).
- Normalización uniforme de los 3 adaptadores (`AdaptadorGPSTrackerXTR4500L`, `AdaptadorGPSTrackerNavTrackQX7A`, `AdaptadorGPSTrackerGeoPulseMTR900`) y traducción de errores externos al mensaje de dominio A3.
