# Informe PoC Mux Data con puerta de consentimiento (ADR-AND-0032, decisión #8)

> Cada afirmación es un **hecho comprobado**, una **inferencia** marcada como tal,
> o **«no verificado»**. La recomendación final es una **propuesta**: decide Andy.

## 1. Versiones exactas (rellenar con lo resuelto por Gradle al compilar)

| Pieza | Versión declarada | Versión resuelta |
|---|---|---|
| Mux Player (`com.mux.player:android`) | 1.6.0 | (pendiente `./gradlew build`) |
| Mux Data (`data-media3-at_1_11`, transitiva) | 1.13.2 | (pendiente) |
| Media3 | 1.11.0 | (pendiente) |
| AGP / Kotlin / Gradle | 8.7.3 / 2.1.20 / 8.12 | (pendiente) |
| Compose BOM / Material3 | 2026.06.01 / 1.4.0 | (pendiente) |
| compileSdk / targetSdk / minSdk | 36 / 36 / 34 | (pendiente) |

Hecho comprobado (2026-10-13, docs públicas): Mux Player 1.6.0 declara Media3 1.11.0 +
Mux Data 1.13.2. Hecho comprobado (consultado 2026-10-14): 1.7.0 figura ya en
`maven-metadata.xml` de
https://muxinc.jfrog.io/artifactory/default-maven-release-local/com/mux/player/android/;
se mantiene 1.6.0 por decisión de Andy (2026-10-13).

Hecho comprobado (2026-10-14, `dl.google.com/dl/android/maven2`): la BOM `2026.09.00`
(compose-ui 1.12.1) y la `2026.08.00` (1.12.0) llevan `minCompileSdk=37` y
`minAndroidGradlePluginVersion=9.1.0` en el `aar-metadata.properties` de `ui-android`; el
proyecto usa compileSdk 36 (Media3 1.11.0 exige 36) y AGP 8.7.3. Se fija la BOM
`2026.06.01` (compose-ui 1.11.4, material3 1.4.0): `ui-android`, `material3-android`,
`foundation-layout-android` y `foundation-android` declaran `minCompileSdk=35` y
`minAndroidGradlePluginVersion=8.6.0`; `runtime-android`, 34 / 8.1.1. No se subió
compileSdk, AGP ni Kotlin.

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
- ¿Qué no se pudo verificar aquí? Compilación y árbitros (esta máquina no tiene Android SDK;
  ver §6), ni ningún caso de red (requiere dispositivo/emulador + claves de Andy).

## 5. Recomendación para el ADR (**propuesta**, decide Andy)

- Si A cumple 0 `*.litix.io` y C corta en caliente: **adoptar opción e** (puerta propia),
  preguntar a Mux si es vía admitida y programar el caso A como test de red en CI.
- Si A muestra envíos o el corte falla: **volver a la reserva** (Media3 propio, ADR-AND-0030).

## 6. Salida de los árbitros (pegar la real; esta máquina no tiene SDK)

```
(pendiente: ./gradlew build | ./gradlew ktlintCheck detekt | ./gradlew test | ./gradlew lint)
```

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
Los puntos de riesgo de compilación que solo tu build puede confirmar siguen en §2.
