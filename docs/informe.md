# Informe PoC Mux Data con puerta de consentimiento (ADR-AND-0032, decisión #8)

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
