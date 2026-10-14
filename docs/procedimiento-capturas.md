# Procedimiento reproducible: captura de red con mitmproxy (lo ejecuta Andy)

PoC: `poc-android-mux`. Requisito: dispositivo o emulador **Android 14+** (API 34+, minSdk 34).

## 1. Vídeo: playback público (por defecto) o firmado

Por defecto el APK reproduce un **playback ID público** (`BuildConfig.PUBLIC_PLAYBACK_ID`, el de
`mux-1` del catálogo de test-mux): no hace falta token ni servidor. Otro ID público:
`./gradlew assembleDebug -PpublicPlaybackId=<id>`.

Flujo firmado (opcional): compila con `-PpublicPlaybackId=` (vacío) y `-PtokenUrl=<URL que alcance
el dispositivo>`, y arranca el token-server en tu máquina con tus claves:

```bash
node --env-file=<tu .env.local> tools/token-server.mjs
# -> token-server en http://127.0.0.1:8787
```

`.env.local` (fuera del repo) con `MUX_SIGNING_KEY_ID`, `MUX_SIGNING_PRIVATE_KEY`
y `MUX_SIGNED_PLAYBACK_ID`. El agente nunca ve estas claves. Comprueba:

```bash
curl http://127.0.0.1:8787
# {"playbackId":"...","token":"..."}
```

## 2. mitmproxy (emulador o dispositivo físico)

1. Arranca `mitmweb --listen-port 8080` (o `mitmdump -w captura-<caso>.flows`, formato nativo de
   mitmproxy) en tu máquina.
2. Instala la CA de mitmproxy como **CA de usuario** (`~/.mitmproxy/mitmproxy-ca-cert.cer`; la ruta
   del menú de Ajustes → Seguridad varía según el fabricante). El flavor **debug** confía en CAs
   de usuario (`app/src/debug/res/xml/network_security_config.xml`).
3. Proxy manual:
   - Emulador: `10.0.2.2:8080`.
   - Dispositivo físico: Wi-Fi → proxy manual → IP de tu máquina en la misma red y puerto `8080`.
     No verificado: aislamiento de clientes del router, VPN activas y que el cliente HTTP de
     Mux Data respete el proxy del sistema.
4. Instala el APK **debug** (`./gradlew installDebug` en un emulador; en un dispositivo, el APK de
   `app/build/outputs/apk/debug/`) y abre la pantalla del laboratorio.

## 3. Qué anotar en cada caso

- **Todos los hosts** a los que habla la app (no solo `litix.io`): filtra por
  `~d litix` para Mux Data y revisa además `stream.mux.com`, `image.mux.com` y otros de Mux.
- Contadores del panel: recibidas / reenviadas / descartadas, por método
  (`get`, `post`, `postWithCompletion`) y por host. Sin cuerpos ni tokens en notas ni logs.
- Pulsa «Reiniciar contadores» antes de cada caso para aislar la medida.

## 4. Casos (criterios en docs/informe.md)

| Caso | Pasos |
|---|---|
| A · denegado (modo 1) | Consentimiento OFF (por defecto). Cargar token, reproducir ≥30 s (vídeo 25,5 s: completo + repetir, pausa, seek). Criterio: **0 peticiones a `*.litix.io`**; todo descartado, nada reenviado. |
| B · concedido (modo 1) | Consentimiento ON, reproducir. Criterio: llegan envíos a Mux Data (captura; y panel de Mux Data si tienes acceso). |
| C · cambio en caliente | A mitad de reproducción: ON→OFF (corte inmediato) y OFF→ON (reinicio). Anotar qué pasa con la vista en curso. |
| D · control (modo 2) | Modo 2, consentimiento OFF, reproducir. Se espera que **sí** haya envíos (sin binding la puerta no actúa). |
| E · reserva (modo 3) | Modo 3, reproducir el mismo asset. Criterio: reproduce y **0 peticiones a Mux Data**. |
| Ciclo de vida | Con consentimiento OFF (modo 1): rotar, segundo plano y volver, cerrar y reabrir. Criterio: sigue 0 peticiones. |
| Límites | ¿Algo al inicializar el reproductor antes de aplicar el consentimiento? ¿Otros hosts de Mux además de `stream.mux.com`, `image.mux.com`, `litix.io`? |

## 5. Árbitros (en tu máquina con Android SDK)

```bash
./gradlew build
./gradlew ktlintCheck detekt
./gradlew test
./gradlew lint
```

Pega la salida real en `docs/informe.md`. Si algo falla: se arregla el código, no el test;
tras 3 intentos en rojo, se detiene y se marca para revisión (regla del prompt §4).
