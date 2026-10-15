# VenueNexa Android — design brief (borrador)

> Estado: **borrador del 2026-10-15** (Inicio construido, sin compilar ni probar en el móvil). Las decisiones de la
> tabla son de Andy; lo marcado «pendiente» no está decidido. Proceso: `ui-design-direction` + `android-compose-guidelines` (ingenius-frontend-skills): acordar la
> dirección, una pantalla aprobada con capturas reales, y solo entonces el resto.

## Producto y público
Marketplace de venues (bodas, eventos, sesiones, reuniones): el invitado busca y reserva espacios, el propietario
los publica. App nativa Android (Kotlin, Compose, Material 3), ES (por defecto) y EN. El PoC pasa de laboratorio a
**app real**: el laboratorio de Mux Data y consentimiento sigue existiendo como sección **Diagnóstico** dentro de
la cuenta, porque es la evidencia de ADR-AND-0032.

## Decisiones (Andy, 2026-10-15)
| Pregunta | Respuesta |
|---|---|
| Identidad visual | **Reutilizar la de la web** (acordada el 2026-10-09, ver `test-mux/docs/design/design-brief.md`) |
| De laboratorio a… | App real, con tarjetas de venues, vídeos de ejemplo, animaciones, inicio y cierre de sesión y cuenta |
| Primera entrega | **Inicio con venues**, **Detalle con vídeo**, **Cuenta y sesión** (con Diagnóstico) |
| Datos y fotos | Venues de ejemplo locales y **Coil** con las fotos de la web (Unsplash, ya verificadas) |
| Movimiento | **Sobrio y con intención** (1–2 elementos por vista; respeta «reducir animaciones») |
| Referencias | Dos shots de Dribbble (ver abajo), vistas el 2026-10-15 |
| Inicio | **Híbrido**: cabecera (wordmark + acceso a Cuenta), búsqueda, chips de categoría, carrusel de destacados con tira de fotos (PhoneTab) y lista vertical con corazón (ETA) |
| Bienvenida «Explorar / Iniciar sesión» | Solo la primera vez (**pendiente de construir**, entrega de Cuenta) |
| Dependencias nuevas | Autorizadas («las cuatro»): Coil, navigation-compose, kotlinx-serialization (rutas tipadas) |
| Orden | **Solo Inicio primero**; capturas reales en el móvil de Andy y **aprobación explícita** antes de Detalle, Cuenta y bienvenida |

## Referencias (vistas el 2026-10-15; las imágenes no se guardan en el repo: son de terceros)
Dribbble bloqueaba la lectura automática; Andy pegó los enlaces directos de las imágenes y se leyeron desde su CDN.
- **ETA Venues Mobile Application** — https://dribbble.com/shots/11356992 — clara, blanca, acento coral, tarjetas planas.
- **PhoneTab: Crypto Wallet / Venue Payment App UI** — https://dribbble.com/shots/27475671 — oscura, degradado naranja, tipografía monoespaciada.

### Qué se toma de cada una (propuesta) y qué no
| | Se toma | No se toma (choca con la identidad de la web) |
|---|---|---|
| **ETA Venues** | Cabecera con búsqueda (qué · categoría) sobre foto; «Popular categories» como chips; **anatomía de la tarjeta** (miniatura redondeada, nombre, ★ valoración con nº de opiniones, nivel de precio, distancia, «Open now!», corazón para guardar); detalle con foto grande redondeada, contador de galería (6/20), filas de datos con icono (disponibilidad, confirmación, idiomas, cancelación gratuita) y compartir; pantalla de filtros con chips y orden | El coral/rojo como color de marca y las barras superiores a todo color; la maqueta girada |
| **PhoneTab** | **Bienvenida con «Explore» y «Login»**; cabecera con saludo y avatar; búsqueda en píldora; **carrusel horizontal de tarjetas** que asoman por el borde, con tira de 4 fotos y botón principal a ancho completo; estado «Open now» en verde; profundidad y brillo sutiles | El tema oscuro y el degradado naranja (la identidad es clara y morada), la tipografía monoespaciada (es Outfit), el contenido de cripto y wallet (el pago es checkout alojado, ADR-AND-0023) |

Las dos tiraban en direcciones distintas; Andy eligió el **híbrido** (ver decisiones). Ambas incluyen mapa (ADR-AND-0027, Google Maps, pide clave): fuera de la primera entrega.

## Identidad (de la web) traducida a Material 3 — propuesta, contrastes sin medir
| Token web | Hex | Rol de Material 3 |
|---|---|---|
| Fondo papel cálido | `#F4F3EF` | `background` |
| Tarjeta | `#FFFFFF` | `surface` (+ `surfaceContainer*`) |
| Texto | `#141217` | `onSurface`, `onBackground` |
| Texto atenuado | `#57535E` | `onSurfaceVariant` |
| Primario (marca, CTA, enlaces) | `#7C3AED` sobre `#FFFFFF` | `primary` / `onPrimary` |
| Acento tintado (seleccionado) | `#F3EFFD` sobre `#5B21B6` | `primaryContainer` / `onPrimaryContainer` |
| Superficie secundaria | `#EBE8E1` | `secondaryContainer`, `surfaceVariant` |
| Borde / input | `#E2DED6` / `#D3CEC4` | `outlineVariant` / `outline` |
| Precio, disponibilidad | `#15803D` | `tertiary` (solo dinero y disponibilidad) |
| Error | sin token en la web | el de Material 3, a confirmar |

- **Tipografía:** Outfit (display 800 con tracking −3 %, títulos 700, cuerpo 400, UI 500–600) sobre la escala de Material 3.
  Hay que empaquetar los `.ttf` (licencia OFL) en `res/font`; las fuentes descargables piden Google Play services.
- **Forma y densidad:** 8 dp en tarjetas, botones, campos y medios (12 dp solo para destacados); botones rectangulares,
  sin píldoras mezcladas con rectángulos; compacto (fotos 3:2, rejillas con huecos de 12–16 dp). Solo tema claro.
- **Evitar** (del brief de la web): fondos morados o rosas de página, decoración en bucle, degradados en tarjetas de
  contenido, iconos en cajas de color, emojis como iconos, movimiento en datos fuera de los destacados.

## Pantallas de la primera entrega (propuesta)
- **Inicio:** cabecera con el wordmark, categorías (6, las de la web) y tarjetas de venues destacados (foto 3:2, nombre,
  ciudad, capacidad, precio en verde, valoración) con entrada escalonada y estados de carga con brillo.
- **Detalle de venue:** transición compartida desde la tarjeta, galería, **vídeo tour con Mux Player y la puerta de
  consentimiento**, datos clave con cifras animadas, botón «Reservar» (lleva a iniciar sesión si no hay sesión).
- **Cuenta:** invitado o sesión (iniciales, caducidad), entrar con Keycloak o Google, **cerrar sesión**, privacidad
  (consentimiento de analítica) y **Diagnóstico** (el laboratorio actual).
- **Navegación:** barra inferior (Inicio, Cuenta) y el detalle apilado encima. Navegar como invitado; la sesión se
  pide al reservar. Pendiente de confirmar con las referencias.

## Datos de ejemplo
- Venues: los tres espacios de la web (jardín, loft, villa) y las seis categorías con sus fotos de Unsplash
  (`landing-content.ts`); nombres, precios y capacidades de ejemplo, ficticios y marcados como tales.
- Vídeos: los dos públicos de Mux del catálogo de la web (`mux-1`, 249,87 s; `mux-2`, 134,42 s).
- Coil pide la imagen al tamaño de pantalla (parámetros de Unsplash) y **vacía la caché de disco al cerrar sesión**
  (ADR-AND-0013).

## Restricciones de los ADR
Navegación con **rutas tipadas** (ADR-AND-0003), imágenes con Coil (ADR-AND-0013), analítica de Mux **apagada sin
consentimiento** (ADR-AND-0032), textos EN/ES (ADR-AND-0011), edge-to-edge y accesibilidad (toques ≥ 48 dp, escala de
fuente, nunca solo color), `core:designsystem` con el tema y los componentes; las pantallas solo consumen sus tokens.

## Dependencias nuevas (autorizadas por Andy el 2026-10-15)
- **Coil** `coil-compose` y `coil-network-okhttp` **3.3.0**. Se intentó la 3.6.3 (última en Maven Central, consultado
  2026-10-15) y **falló el build del Codespace**: arrastra Compose 1.12 (pide AGP 9.1; el proyecto usa 8.13.2) y
  kotlin-stdlib 2.4.10 (el compilador 2.1.20 no lo lee). Desde la 3.4.0 piden stdlib 2.3+; la 3.3.0 (stdlib 2.2.0, Compose
  1.8.2, `minCompileSdk` 1) es la última compatible. Subir Coil exige subir Kotlin y AGP (decisión aparte).
- **navigation-compose 2.9.8** y **kotlinx-serialization** (plugin `2.1.20` + librería 1.8.1) para rutas tipadas: se añaden
  con el Detalle. Se usa la 2.9.x porque la 2.10.x exige AGP 9.1 y el proyecto está en AGP 8.13.2 (comprobado 2026-10-15).
- `material-icons-core` (de la BOM de Compose) para los iconos básicos; el plugin `kotlin-jvm` para `core:model`.
- Fuente **Outfit** (SIL OFL) empaquetada en `res/font` (variable, un solo archivo; licencia en `OFL-Outfit.txt`).

## Estado de Inicio (2026-10-15) y siguiente paso
Escrito, **sin compilar ni verlo en un dispositivo**: `core:model`, `core:data` (9 venues de ejemplo, latencia simulada de
700 ms), `core:designsystem` (tema, tipografía, espaciado, wordmark, entrada escalonada, brillo de carga, foto con Coil,
botón de guardar con resorte, valoración, disponibilidad, buscador, chips) y `feature:home` (+ pruebas del ViewModel).
En `:app`: barra inferior Inicio/Cuenta (Cuenta = el laboratorio actual), borde a borde, tema claro de ventana.
Decisiones de detalle de esta entrega (revisables al ver las capturas): chips con nombres cortos (Bodas, Corporativo,
Sesiones, Cumpleaños, Reuniones, Pop-up) en lugar de los de la web; pulsar una tarjeta muestra un aviso «la ficha llega
en la siguiente entrega»; guardados solo en memoria; sin saludo con nombre (pide ampliar la sesión de identidad).
Siguiente: compilar en el Codespace, capturas reales en el móvil de Andy y **su aprobación explícita** antes del resto.
