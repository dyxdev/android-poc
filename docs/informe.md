# Informe PoC Mux Data con puerta de consentimiento (ADR-AND-0032, decisión #8)

> **Estado a 2026-10-16.** La PoC pasó de laboratorio a app (Inicio, Detalle con vídeo, Cuenta, bienvenida y cambio de
> contexto): ver §7. Lo que Andy comunicó y lo que está medido o sin verificar se separa en cada apartado. **Lo que sigue
> sin evidencia:** la captura de red de los casos A–E (§3), el resultado con consentimiento concedido y el corte en
> caliente, y la compilación de los últimos seis commits (§6).

> Cada afirmación es un **hecho comprobado**, una **inferencia** marcada como tal,
> o **«no verificado»**. La recomendación final es una **propuesta**: decide Andy.

## 1. Versiones exactas (rellenar con lo resuelto por Gradle al compilar)

| Pieza | Versión declarada | Versión resuelta |
|---|---|---|
| Mux Player (`com.mux.player:android`) | 1.6.0 | (pendiente `./gradlew build`) |
| Mux Data (`data-media3-at_1_11`, transitiva) | 1.13.2 | (pendiente) |
| Media3 | 1.11.0 | (pendiente) |
| AGP / Kotlin / Gradle | 8.13.2 / 2.1.20 / 8.13 | Gradle 8.13 (log del Codespace, 2026-10-14); resto (pendiente) |
| Compose BOM / Material3 | 2026.06.01 / 1.4.0 | (pendiente) |
| compileSdk / targetSdk / minSdk | 37 (`android-37.0`) / 36 / 34 | compila en Codespace (2026-10-14) |

Hecho comprobado (2026-10-13, docs públicas): Mux Player 1.6.0 declara Media3 1.11.0 +
Mux Data 1.13.2. Hecho comprobado (consultado 2026-10-14): 1.7.0 figura ya en
`maven-metadata.xml` de
https://muxinc.jfrog.io/artifactory/default-maven-release-local/com/mux/player/android/;
se mantiene 1.6.0 por decisión de Andy (2026-10-13).

Hecho comprobado (2026-10-14, `dl.google.com/dl/android/maven2`): la BOM `2026.09.00`
(compose-ui 1.12.1) y la `2026.08.00` (1.12.0) llevan `minCompileSdk=37` y
`minAndroidGradlePluginVersion=9.1.0` en el `aar-metadata.properties` de `ui-android`; el
proyecto usaba entonces compileSdk 36 y AGP 8.7.3. Se fija la BOM `2026.06.01` (compose-ui
1.11.4, material3 1.4.0): `ui-android`, `material3-android`, `foundation-layout-android` y
`foundation-android` declaran `minCompileSdk=35` y `minAndroidGradlePluginVersion=8.6.0`;
`runtime-android`, 34 / 8.1.1. La BOM `2026.09.00` sigue sin usarse porque exige AGP 9.1.

Hecho comprobado (2026-10-14, build real con AGP 8.7.3): el `aar-metadata.properties` de
`com.mux.player:android:1.6.0`, `data-media3-at_1_11:1.13.2` y `data-media3-custom-at_1_11:1.13.2`
declara `minCompileSdk=37`; también Mux Player 1.5.5 y 1.7.0, mientras que 1.5.3 y anteriores
no lo exigen. Por eso el proyecto compila con compileSdk 37 (targetSdk 36, minSdk 34).
AGP 8.7.3 no puede usar esa plataforma: Google la publica como `platforms;android-37.0` con
`api-level` 37.0 (repository2-3.xml) y AGP la busca por paquete. Con AGP 8.13.2 y Gradle 8.13, un
entero 37 pide el hash `android-37` y falla; `compileSdkVersion("android-37.0")` en las
convenciones de `build-logic` lo resuelve (comprobado en el Codespace, `assembleDebug` correcto).
AGP 8.13.2 se probó hasta compileSdk 36.1: `android.suppressUnsupportedCompileSdk=37,37.0`.

## 2. Firmas verificadas en código público (rama main, 2026-10-13)

- **Hecho comprobado:** `MuxPlayer.Builder` tiene `addExoPlayerBinding(ExoPlayerBinding)` y
  `addNetwork(INetworkRequest)`; sin binding, `MuxPlayer` crea `MuxStatsSdkMedia3` **sin**
  la red propia (la puerta no actúa). Con binding + red propia, la red se pasa al crear
  `MuxStatsSdkMedia3`. Fuente: `muxinc/mux-player-android`, `MuxPlayer.kt`.
- **Hecho comprobado:** `INetworkRequest` tiene `get(URL)`, `post(URL, JSONObject, Hashtable)` y
  `postWithCompletion(domain, envKey, body, headers, completion)` con **dos** variantes de
  callback (`IMuxNetworkRequestsCompletion` y `…2`); `MuxNetwork` es pública y cubre ambas.
  Fuente: `muxinc/stats-sdk-android`, `MuxNetwork.kt`; `mux-stats-sdk-media3`,
  `MockNetworkRequest.java`, `FakeNetwork.kt`.
- **Hecho comprobado:** playback firmado en Mux Player = `MediaItems.fromMuxPlaybackId(...,
  playbackToken = token)`. Fuente: `MediaItems.kt`. Reserva Media3 =
  `https://stream.mux.com/{playbackId}.m3u8?token={jwt}` (guía Mux).
- **Hecho comprobado:** integración en Compose = `AndroidView` + `PlayerView` de Media3.
  Fuente: ejemplo oficial `OfflinePlayerScreen.kt`.
- **Hecho comprobado (2026-10-14) contra los `sources.jar` publicados** (player 1.6.0,
  `data-media3-at_1_11` 1.13.2, `data-media3-custom-at_1_11` 1.13.2, android 1.7.2) y
  `stats.muxcore` 8.12.0: `INetworkRequest` vive en `muxcore` (no en `stats-sdk-android`);
  el overload con `IMuxNetworkRequestsCompletion2` es método `default`;
  `IMuxNetworkRequestsCompletion2.onComplete(boolean, Map<String, List<String>>)` coincide;
  `ExoPlayerBinding` es `open class` con constructor público sin argumentos; `MuxPlayer`
  sin binding no pasa la red propia (`MuxPlayer.kt` 1.6.0, líneas 128-136).

## 3. Resultados por caso (los rellena Andy con las capturas)

| Caso | Resultado | Cómo se comprobó | Evidencia (hosts + recuentos, sin tokens) |
|---|---|---|---|
| A · denegado, modo 1 | (pendiente) | captura §4 + contadores | 0 `*.litix.io` esperado |
| B · concedido, modo 1 | (pendiente) | captura (+ panel Mux Data si hay acceso) | envíos esperados |
| C · cambio en caliente | (pendiente) | ON→OFF y OFF→ON a mitad | corte/reinicio esperado |
| D · control, modo 2 | (pendiente) | modo 2, OFF | envíos esperados (puerta inútil sin binding) |
| E · reserva, modo 3 | (pendiente) | modo 3 | reproduce + 0 Mux Data esperado |
| Ciclo de vida | (pendiente) | rotar, fondo, reinicio con OFF | 0 peticiones esperado |
| Límites | (pendiente) | init + otros hosts Mux | anotar hosts |

## 4. Respuestas explícitas (propuesta tras las capturas; hoy: hipótesis de diseño)

- ¿Hacen falta `addExoPlayerBinding` **y** `addNetwork`? **Inferencia (del código): sí.**
  Sin binding, `MuxPlayer` ignora la red propia. El caso D lo confirma o refuta en red.
- ¿Algún envío escapa de `INetworkRequest`? **No verificado.** El núcleo de Mux Data no hace
  HTTP propio (**inferencia** del diseño: todo pasa por `INetworkRequest`), pero solo la
  captura (casos A y límites) lo demuestra. Si aparece `*.litix.io` en A, la vía no es fiable.
- ¿Funciona el corte en caliente? **Diseño: sí** (consentimiento leído en cada envío, sin
  cola: lo previo no se reenvía). El caso C lo demuestra.
- ¿Riesgos al actualizar Mux Player? **Sí**: la vía no está documentada como interruptor;
  un cambio interno (p. ej. crear `MuxStatsSdkMedia3` con red por defecto aunque haya
  binding, o nuevas llamadas fuera de `INetworkRequest`) rompería la puerta en silencio.
  Mitigación: este PoC + caso A en CI con emulador, y pregunta a Mux si es vía admitida.
- **Hecho comprobado (2026-10-14, bytecode de muxcore 8.12.0, `BeaconBatchTracker.onComplete`):**
  con `success=false` el núcleo re-encola el lote pendiente en memoria (hasta 3600 eventos;
  por encima los descarta) y lo reintenta. La puerta completa con `false` al descartar, así
  que lo recogido sin consentimiento podría enviarse después de conceder (caso C, OFF→ON).
  Contradice «sin cola» de arriba. **Decidido por Andy (2026-10-15): completar con `true`** al
  descartar: el núcleo da el envío por hecho y vacía el lote (rama de éxito de `onComplete`:
  `pendingEventsQueue.clear()`), así que nada se acumula ni se reintenta. Cambio aplicado en
  `ConsentGatingNetworkRequest` y en su test. **Sin comprobar en dispositivo** (caso C: conceder tras
  haber reproducido sin consentimiento y verificar con mitmproxy que no sale nada anterior).
- ¿Qué no se pudo verificar? Ningún caso de red (requiere dispositivo/emulador + claves de
  Andy). Compilación y árbitros: ver §6.

## 5. Recomendación para el ADR (**propuesta**, decide Andy)

- Si A cumple 0 `*.litix.io` y C corta en caliente: **adoptar opción e** (puerta propia),
  preguntar a Mux si es vía admitida y programar el caso A como test de red en CI.
- Si A muestra envíos o el corte falla: **volver a la reserva** (Media3 propio, ADR-AND-0030).

## 6. Salida de los árbitros

Última ejecución completa pegada por Andy (Codespace, 2026-10-14, 6 min 12 s,
`assembleDebug ktlintCheck detekt test lint --continue`): `assembleDebug` correcto (APK debug),
`detekt` y `lint` correctos en todos los módulos, tests de `feature:player-lab` correctos.
Fallaron tres tareas: `:core:consent:ktlintMainSourceSetCheck` (`GatingFactory.kt:14`) y
`:core:consent:testDebugUnitTest` / `testReleaseUnitTest`
(`granted_everythingReachesDelegate`, aserción `byHost["litix.io"]`). Corregidas después en el
repo (`max_line_length = 120` en `.editorconfig`; la expectativa correcta es 4 peticiones a
`litix.io`); **pendiente reejecutar** para confirmar. Salida real definitiva: (pendiente).

Nota: el código se generó sin compilar (acordado con Andy el 2026-10-13 ante la falta de
Android SDK en esta máquina). Revisión estática propia del 2026-10-13: 11 hallazgos,
todos corregidos en el acto (ver bitácora de la sesión): `NetworkCounters.record`
no compilaba (parámetro `forwarded: Boolean` ocultaba la propiedad `forwarded: Int`);
faltaba el binding Hilt de `@IoDispatcher` (nuevo `core:common/DispatchersModule`);
faltaban plugin kapt, plugin Hilt en `:app`, `namespace` en librerías,
`foundation-layout`, `ui-tooling-preview` y `hilt-navigation-compose`;
`includeBuild("build-logic")` duplicado; clave `processors` inválida en `detekt.yml`;
`PlayerLabScreen` muerta eliminada; `Timber` solo en debug; test del ViewModel con
`Dispatchers.setMain`; `gradlew`/`gradlew.bat` + wrapper jar añadidos (jar tomado del
repo oficial gradle/gradle en GitHub: regenerar con `gradle wrapper --gradle-version 8.12`
en tu máquina para dejarlo canónico).
Actualización 2026-10-14: `gradlew`, `gradlew.bat` y el jar eran una versión reducida a mano (el
`exec` pasaba `"-Xmx64m"` con comillas a Java) y el jar no coincidía con el publicado; se
sustituyeron por los oficiales de Gradle 8.13.0 (sha256 del jar verificado contra
https://services.gradle.org/distributions/gradle-8.13-wrapper.jar.sha256).
Los puntos de riesgo de compilación que solo tu build puede confirmar siguen en §2.

**Actualización 2026-10-15 (builds pegados por Andy tras añadir la app):** el primer build falló por Coil 3.6.3 (Compose 1.12 y
stdlib 2.4.10, §7.3) y por `detekt` (`MatchingDeclarationName` en `feature:home`); `ktlintCheck` pasó en todos los módulos y
los tests de `core:identity`, `core:consent` y `feature:player-lab` pasaron. El segundo build, con Coil 3.3.0, falló solo en
`:core:designsystem:compileDebugKotlin` (`ExperimentalTextApi`, `Type.kt:12`), corregido en `e632f1d`; `detekt` y `ktlintCheck`
pasaron en todos los módulos, incluido `feature:home`. **Desde entonces no hay build pegado:** no consta que compilen
`feature:home`, `feature:detail`, `feature:account`, `feature:owner`, `core:context` ni `app`, ni que pasen sus pruebas.

## 7. La PoC como app (2026-10-15 y 2026-10-16)

Todas las decisiones de esta sección son de Andy y están fechadas. El código de la app **no se ha compilado ni probado
desde el commit `e632f1d`** (solo se leyó y se revisó; ver §6); lo que Andy probó en el móvil se anota como comunicado.

### 7.1 Qué hay
- **Identidad (ADR-AND-0008):** login con Keycloak (AppAuth), Google por el *broker*, tokens cifrados con Keystore,
  cierre de sesión con revocación y cierre SSO. **Comunicado por Andy (2026-10-15): el login funcionó en su móvil** contra el
  Keycloak de pruebas del Codespace (`tools/keycloak-test/`). Corrige el aviso de `docs/identidad-keycloak.md` («nada se ha
  compilado ni ejecutado»), que quedó atrás.
- **Verificación manual en el móvil: comunicado por Andy (2026-10-16)** que probó la APK de forma manual. No detalló qué flujos ni con qué
  versión del código, así que aquí **no** se da por probado ningún flujo concreto más allá del login (arriba); pendiente de que Andy
  lo concrete (Inicio, Detalle y tour, Cuenta, bienvenida, cambio de contexto, consentimiento) para registrarlo.
- **App (módulos nuevos):** `core:model`, `core:data` (venues de ejemplo, guardados, preferencias en DataStore),
  `core:designsystem` (tema, tipografía Outfit, componentes), `core:context` (contextos y su hoja de cambio),
  `feature:home`, `feature:detail`, `feature:account`, `feature:owner` y `app` con navegación tipada. El laboratorio de Mux
  sigue en `feature:player-lab`, ahora dentro de Cuenta → Diagnóstico.
- **Vídeo del tour (ADR-AND-0030 y 0032):** el detalle reproduce con el mismo reproductor candidato del laboratorio (Mux Player
  con `ExoPlayerBinding` y la puerta de consentimiento), configurado en **un solo sitio** (`GatedMuxPlayer`, `core:consent`).
- **Diseño:** identidad de la web (Outfit, sin color dinámico); **solo se hizo y se probó el tema claro, por simplicidad**: el modo oscuro debe soportarse (decisión de Andy, 2026-10-16) y falta. Brief en `docs/design/design-brief.md`.

### 7.2 Decisiones de Andy
| Fecha | Decisión |
|---|---|
| 2026-10-15 | Reutilizar la identidad visual de la web; app real con Inicio (híbrido: destacados + lista), Detalle con vídeo y Cuenta; en la PoC solo se probó el tema claro, por simplicidad (el oscuro se soporta, 2026-10-16) |
| 2026-10-15 | Dependencias nuevas autorizadas: Coil, navigation-compose y kotlinx-serialization (rutas tipadas) |
| 2026-10-15 | La puerta de consentimiento descarta completando con éxito (`true`); ver §4 |
| 2026-10-15 | Cambio de contexto: capacidades **simuladas** en local; conmutador híbrido de una hoja con tres entradas (etiqueta de perfil, avatar, bloque en Cuenta), sin apertura automática; aviso de primera vez (globo y punto), una vez por cada contexto nuevo |
| 2026-10-15 | La bienvenida «Explorar / Iniciar sesión» solo sale la primera vez |
| 2026-10-16 | El consentimiento de analítica es **de la persona**: se retira al cerrar sesión (también por caducidad) |

### 7.3 Hechos medidos que condicionan los ADR
- **Coil:** 3.4.0 y posteriores piden `kotlin-stdlib` 2.3+ y la 3.6.3 arrastra Compose 1.12 (pide AGP 9.1). Comprobado en los
  `.module` de Maven Central y en el fallo real del build del Codespace (2026-10-15). Con Kotlin 2.1.20 y AGP 8.13.2 la última
  compatible es **3.3.0**.
- **Navigation Compose:** 2.10.x exige AGP 9.1; se usa **2.9.8** (metadatos de Maven, 2026-10-15).
- **Compose BOM:** 2026.08.00 y posteriores (Compose 1.12) exigen AGP 9.1; se usa 2026.06.01 (§1).
- **Mux Player 1.6.0** exige `compileSdk` 37 (§1).
- **Compose 1.11.4:** `Font(resId, …, variationSettings)` sigue marcada `@ExperimentalTextApi` (fuentes publicadas y error real
  del build); la transición compartida (`SharedTransitionLayout`) ya no requiere opt-in.
- **Todas estas versiones son del proyecto, no de los ADR:** suben juntas el día que el proyecto suba a AGP 9.1.

### 7.4 Limpieza al cerrar sesión (ADR-AND-0008, 0013 y 0026)
Un solo punto (`SessionCleanup`) actúa cuando la sesión pasa de iniciada a cerrada, sea cual sea la causa: guardados, contexto
recordado y avisos vistos, cachés de imágenes y consentimiento de analítica. Tiene pruebas de JVM (**sin ejecutar**, §6).

### 7.5 Lo que sigue abierto
- Captura de red de los casos A–E (§3), con mitmproxy (`sudo pacman -S mitmproxy`, aún sin instalar en la máquina de Andy).
- Medir la lentitud que Andy percibió en el APK de debug (compilar un release firmado con la clave de debug y comparar).
- Capacidades reales: dependen del endpoint de capacidades de la API (ADR-AND-0026); hoy son simuladas y **solo en debug**.
- El panel de propietario es de ejemplo y conserva su estado al cambiar de contexto (los datos reales deberán vaciarse).

## 8. Cruce con los ADR de Android (2026-10-16)

Revisión de ADR-AND-0001 a 0032 contra esta PoC y entre sí (un revisor independiente de solo lectura, con los hallazgos
comprobados después contra los textos; el código de la PoC se leyó, no se ejecutó). Resultado:

- **Registrado como hecho** (Follow-up de 13 ADR, sin cambiar ninguna decisión): 0001, 0003, 0004, 0006, 0007, 0008, 0012,
  0015, 0018, 0020, 0026, 0030, 0032 (y 0013, a la espera de que se integre el trabajo en curso de ese archivo). Cada nota empieza por
  «Comprobación en la PoC (2026-10-16)» y separa lo medido de lo pendiente.
- **Coherencia corregida:** 0030 se marca como parcialmente sustituido por 0032 (queda como reserva); título de 0032 y fila del README
  sin el «si el proveedor es Mux» ya resuelto; 0008 recoge que Keycloak está en ADR-VN-0007.
- **Citas verificadas:** todos los `ADR-AND`, `ADR-WEB` e `ADR-IOS` citados existen, y los RF y RNF citados también.
- **Decisiones que necesitan a Andy** (el agente no las toma; cada una cita los ADR afectados):

| # | Decisión | ADR | Opciones |
|---|---|---|---|
| D1 | Estado de la #9 y de `minSdk` 34 (los ADR dicen «pendiente» y otros «establecido») | 0001, 0009, 0015, 0022, 0032, CLAUDE.md | **Decidido (Andy, 2026-10-16): `minSdk` 34, cerrado por Danay** |
| D2 | El consentimiento de analítica es de la persona: dónde queda escrito (ámbito, defecto, un interruptor o varios, invitado, Crashlytics, paridad) | 0014, 0029, 0032 | ADR propio, o enmienda de 0032 y 0029 |
| D3 | Limpieza al cerrar sesión en un solo punto (disparador, qué es de la persona, política de fallos) | 0007, 0008, 0013, 0026 | ADR corto, o enmiendas |
| D4 | La app es solo de tema claro (como la web) frente al «soporte de modo oscuro» | 0012 | **Decidido por Andy (2026-10-16): el modo oscuro se soporta**; la PoC solo probó el claro por simplicidad |
| D5 | Política de versiones (matriz AGP, Kotlin, compileSdk) y APIs experimentales aprobadas | 0001, 0015, 0032 | **Decidido: matriz de compatibilidad y excepciones aprobadas; `compileSdk` 37 aceptado** |
| D6 | Pruebas por riesgo en vez de «todo ViewModel y repositorio» | 0018 | Enmendar como ADR-WEB-0022, o escribir las que faltan |
| D7 | Módulos nuevos (`identity`, `consent`, `context`) y datos de ejemplo dentro de `core:data` | 0020 | **Decidido: el reparto de la PoC es la referencia** |
| D8 | `kapt` en vez de KSP para Hilt | 0004 | **Decidido: KSP para más adelante** |
| D9 | Contextos: una barra por contexto y simulador solo en debug por bandera | 0026 | **Decidido: un grafo con barra por contexto y simulador solo en debug** |
| D10 | Límite de inactividad en móvil | 0008 | **Decidido: paridad con la web** |
| D11 | Flags del invitado y qué se vacía al cerrar sesión | 0021, 0008 | **Decidido: no se vacían** |
| D12 | Vídeos públicos sin token y `enableSmartCache(false)` | 0030, 0032 | **Decidido: lo que dicen los ADR (token siempre; caché apagada)** |
| D13 | Mover cifras, versiones y nombres de API de los ADR a la bitácora (regla de Andy) | 0004, 0009, 0011, 0028, 0031, 0032 | **Decidido: en bloque, datos copiados a la bitácora** |
| D14 | Excepción de red solo en debug (texto en claro a `10.0.2.2` y CA de usuario) | 0005 | **Decidido: registrar la excepción de debug** |

**Decididas por Andy el 2026-10-16 (las recomendadas):** D2 → ADR-AND-0033 (consentimiento de analítica de la persona), D3 → ADR-AND-0034 (limpieza al cerrar sesión), D4 → ADR-AND-0012 (corregida por Andy el mismo día: el modo oscuro **se soporta** en toda la app; la PoC solo probó el claro por simplicidad), D6 → ADR-AND-0018 enmendado a mínimos por riesgo. D1, D5 y D7 a D14 las decidió después (tabla de arriba y entrada de las 13:30 de la bitácora del lote). Queda por confirmar la fecha y el medio en que Danay cerró la #9, y su vía de distribución.

- **Pendientes de código** (no cambian decisiones): módulos que repiten el bloque de Compose y Hilt, dependencias fuera del
  catálogo, ktlint y detekt desde la raíz, fakes repetidos en vez de `core:testing`, `Dispatchers.IO` en `core:designsystem`, la
  versión de Mux escrita a mano en `ConsentModule`, y la ruta del laboratorio que sigue dentro del APK de release.
