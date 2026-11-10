# AgroGenética Digital

Tienda virtual Android nativa (Kotlin + XML) para comercializar genética bovina: pajillas, embriones y ejemplares vivos en exhibición. Proyecto de la asignatura **Desarrollo de Aplicaciones Móviles Nativas** – Fundación Universitaria U Compensar.

> **Alcance de esta versión (Actividad 3):** frontend completo de los tres roles con datos simulados en memoria. No hay backend todavía; la capa de repositorios permite conectarlo después sin tocar las pantallas.

## Roles y pantallas

| Rol | Pantallas |
|---|---|
| **Transversal** | Splash · Login (correo + huella) · Registro · Recuperar contraseña · Habilitar huella · Menú lateral (Drawer) + Bottom Navigation |
| **Comprador** | Inicio · Catálogo con búsqueda, filtros y orden · Detalle de pajilla/embrión · Detalle de ejemplar vivo (llamar / WhatsApp) · Carrito · Pasarela de pagos (MercadoPago, PSE, ePayco) con confirmación por huella · Pago exitoso · Mis pedidos · Detalle de pedido (cancelar) · Perfil · Editar perfil (cámara, GPS) |
| **Vendedor** | Panel (métricas, pedidos recientes) · Mis productos (CRUD: listar, buscar, filtrar, editar, eliminar con deshacer) · Nuevo/editar producto (cámara nativa, GPS, certificado ICA en PDF) · Pedidos recibidos · Detalle de pedido (cambiar estado) · Perfil · Editar perfil |
| **Administrador** | Dashboard (métricas, aprobaciones pendientes, accesos rápidos) · Gestión de usuarios (listar, filtrar, aprobar vendedores, crear, editar, suspender, eliminar) · Moderación de productos (pendientes, aprobados, reportados, revisión detallada) · Reporte de ventas (gráfico de barras, rankings, exportar CSV) · Perfil |

### Cuentas de demostración
En la pantalla de login hay tres botones (**Comprador**, **Vendedor**, **Admin**) que entran directamente a cada rol. También se puede ingresar escribiendo:

| Rol | Correo | Contraseña |
|---|---|---|
| Comprador | `comprador@agro.co` | `Agro2026*` |
| Vendedor | `vendedor@agro.co` | `Agro2026*` |
| Administrador | `admin@agro.co` | `Agro2026*` |

## Arquitectura

- **UI:** XML + Views (ConstraintLayout, MaterialCardView, RecyclerView, TextInputLayout, ChipGroup, BottomNavigationView, NavigationView) con Material Components 3 y **tema propio**.
- **Navegación:** Navigation Component. `SplashActivity` → `AuthActivity` (`nav_auth`) → una Activity por rol (`BuyerActivity`, `SellerActivity`, `AdminActivity`) que comparten `RoleHostActivity` (Toolbar + Drawer + Bottom Navigation) y cargan su grafo (`nav_buyer`, `nav_seller`, `nav_admin`). Cada pantalla es un Fragment.
- **Datos:** MVVM ligero. Los Fragments/ViewModels dependen de interfaces (`ProductRepository`, `OrderRepository`, `UserRepository`, `CartRepository`, `AuthRepository`); hoy las implementa `data/fake` en memoria y `di/ServiceLocator` decide cuál usar.
- **Hardware y APIs nativas:** `BiometricPrompt` (login y pago), cámara (`TakePicturePreview`), galería y selector de PDF (Activity Result API), ubicación (lectura única con `LocationManager`), Intents a llamada, WhatsApp y compartir.

```
app/src/main/java/com/jmariodev/agrodigital/
├── AgroApp.kt                 # inicializa el ServiceLocator
├── data/
│   ├── model/                 # Product, Order, User, CartItem, enums de estado
│   ├── repository/            # interfaces (contratos)
│   ├── fake/                  # datos de ejemplo + repositorios en memoria
│   └── session/               # SessionManager (rol, huella)
├── di/ServiceLocator.kt
└── ui/
    ├── splash/  auth/         # flujo transversal
    ├── common/                # RoleHostActivity, BindingFragment, extensiones, perfil, pedidos, detalle
    ├── buyer/                 # inicio, catálogo, detalle vivo, carrito, checkout
    ├── seller/                # panel, productos (CRUD)
    └── admin/                 # panel, usuarios, moderación, reportes
```

## Recursos y convenciones (XML)

| Carpeta | Contenido |
|---|---|
| `values/colors.xml` | Paleta de la guía de estilos con nombres semánticos (`agro_primary #2E7D32`, `agro_secondary #1976D2`, …) |
| `values/themes.xml` | `Theme.Agrodigital` (Material 3 claro) con colores, tipografía, formas y estilos por defecto de cada componente |
| `values/styles.xml` | Estilos reutilizables `Widget.Agro.*` (botones, campos, tarjetas, chips, badges, navegación, diálogos) |
| `values/type.xml` | Escala tipográfica `TextAppearance.Agro.*` con **Inter** (`res/font`) |
| `values/shapes.xml` | `ShapeAppearance.Agro.*` (esquinas 8/16/24 dp, píldora, círculo) |
| `values/dimens.xml`, `integers.xml`, `strings.xml`, `arrays.xml` | Espaciados, tamaños, textos (incluye plurales) y listas |
| `values-sw600dp/` | Ajustes para tablets (3 columnas en catálogo, márgenes mayores) |
| `drawable/` | `ic_*` íconos vectoriales propios (trazo de 2 dp), `bg_*` formas, `img_bull` ilustración, ícono del launcher |
| `color/` | Selectores de estado (`sel_*`): bottom nav, chips, botones segmentados, campos, tarjetas de pago |
| `layout/` | `activity_*`, `fragment_<rol>_*`, `item_*` (listas), `view_*` (componentes incluidos con `<include>`), `dialog_*`, `bottom_sheet_*` |
| `menu/`, `navigation/`, `anim/` | Menús por rol, grafos de navegación y animaciones del splash |

Reglas: ningún layout tiene colores hex, textos literales ni dimensiones repetidas; todo sale de `values`. IDs en camelCase con prefijo por tipo (`btn`, `tv`, `til`, `rv`, `iv`, `chip`…).

## Cómo ejecutar

1. Abrir la carpeta del proyecto en **Android Studio** (versión compatible con AGP 9.1).
2. Sincronizar Gradle y ejecutar la configuración `app` en un emulador o dispositivo con Android 8.0 (API 26) o superior.
3. Para probar la huella en el emulador: *Settings → Security → Fingerprint*, y usar el sensor virtual desde *Extended controls*.
4. APK: *Build → Generate App Bundles or APKs → Generate APKs*. Queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Normatividad reflejada en la interfaz

- **Ley 1581 de 2012:** aceptación de la política de datos en el registro, política visible en el perfil; la huella se valida en el dispositivo.
- **Ley 1480 de 2011:** términos y derecho de retracto antes de pagar; cancelación de pedidos.
- **Ley 527 de 1999:** comprobante digital del pedido (pantalla de pago exitoso e historial).
- **ICA:** certificado sanitario PDF obligatorio al publicar; el administrador lo revisa antes de aprobar.
- **PCI DSS:** la app no captura datos de tarjeta; el pago se delega a la pasarela.

## Autor

Jorge Mario González Quintero – Ingeniería de Software, Fundación Universitaria U Compensar.
