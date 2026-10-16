# PoC Android Mux Player con Mux Data solo con consentimiento

Prueba de concepto para ADR-AND-0032 (decisión #8): demostrar con evidencia de red que
Mux Data está apagado sin consentimiento de analítica (0 peticiones a `*.litix.io`).

## Estructura

- `app/` — `MainActivity`, bienvenida, navegación (Inicio, Detalle, Cuenta, Diagnóstico), config debug (cleartext a 10.0.2.2 + CA de usuario).
- `core:model`, `core:data` — modelo de venues y datos de ejemplo; guardados y preferencias del usuario.
- `core:designsystem` — tema de VenueNexa (Material 3), tipografía Outfit y componentes. `core:context` — contextos
  (cliente/propietario), capacidades **simuladas** y la hoja de cambio de perfil (ADR-AND-0026).
- `feature:home`, `feature:detail`, `feature:account`, `feature:owner` — pantallas de la app (diseño: `docs/design/design-brief.md`).
- `core:consent` — DataStore (denegado por defecto) + `ConsentGatingNetworkRequest`
  (puerta `INetworkRequest`: delega en `MuxNetwork` con consentimiento, descarta sin él,
  lectura en cada envío, sin reenvío de lo previo) + contadores por método y host.
- `core:common` — dispatchers inyectados. `core:testing` — fakes (sin mocking).
- `core:identity` — login con Keycloak (AppAuth, ADR-AND-0008): sesión, tokens cifrados con Keystore, refresh serializado, logout.
- `feature:player-lab` — pantalla Compose + ViewModel (UDF) + 3 modos
  (1 candidato con puerta, 2 control sin binding, 3 reserva Media3 propio).
- `tools/token-server.mjs` — JWT RS256 local, puerto 8787 (claves de Andy, fuera del repo; solo para el flujo firmado).
- `tools/keycloak-test/` — Keycloak de pruebas para el Codespace (realm, cliente de Android y scripts de arranque y parada).
- `docs/procedimiento-capturas.md` — lo que ejecuta Andy. `docs/informe.md` — resultados.

## Build y árbitros (Codespace o máquina con Android SDK)

```bash
node --env-file=<tu .env.local> tools/token-server.mjs
./gradlew build
./gradlew ktlintCheck detekt
./gradlew test
./gradlew lint
```

Ver `docs/procedimiento-capturas.md` para las capturas con mitmproxy (casos A–E).

## Probar la identidad (Keycloak) en el móvil

El APK se compila con el emisor y el cliente de Keycloak **al construirlo** (entran en `BuildConfig`): cambiar de
Keycloak es cambiar dos variables y **recompilar e instalar de nuevo**. Sin ellas la tarjeta «Cuenta» dice
«Keycloak no está configurado». Más detalle: `docs/identidad-keycloak.md` y `docs/keycloak-de-pruebas.md`.

### A. Con el Keycloak de pruebas del Codespace
1. `git pull` y, en la terminal del Codespace: `bash tools/keycloak-test/start.sh`. Arranca Keycloak con su propio
   JDK 25, importa el realm `venuenexa` con el cliente `venuenexa-android` y deja el puerto 8080 en público
   (si no puede, o el puerto no aparece en la pestaña PORTS: «Forward a Port» > 8080 y Port Visibility > Public;
   y Change Port Protocol > HTTP: la URL pública sigue siendo https; ver `docs/keycloak-de-pruebas.md`).
   Imprime el emisor y la línea de compilación.
2. Compila (sin ejecutar Gradle y Keycloak a la vez en un Codespace de 2 núcleos):
   ```bash
   KEYCLOAK_ISSUER=https://<codespace>-8080.app.github.dev/realms/venuenexa    KEYCLOAK_CLIENT_ID=venuenexa-android ./gradlew assembleDebug
   ```
3. Descarga `app/build/outputs/apk/debug/app-debug.apk` (explorador de VS Code > clic derecho > Download) e instálalo
   en el móvil (Android 14 o superior).
4. Tarjeta «Cuenta» > «Iniciar sesión»: entra con `demo` y la contraseña de `cat ~/keycloak-test/credentials.txt`.
5. Al terminar: `bash tools/keycloak-test/stop.sh` (para el servidor y vuelve el puerto a privado).
   Google es opcional: ver «Google» en `docs/keycloak-de-pruebas.md`.

### B. Con otro Keycloak (el de backend u otro de pruebas)
Cambia solo las variables y recompila; no hay que tocar código:
```bash
KEYCLOAK_ISSUER=https://<host>/realms/<realm> KEYCLOAK_CLIENT_ID=<id del cliente> KEYCLOAK_SCOPES="openid email profile" ./gradlew assembleDebug   # KEYCLOAK_SCOPES es opcional
```
(también valen `-PkeycloakIssuer=…`, `-PkeycloakClientId=…` y `-PkeycloakScopes=…`; la propiedad manda sobre la variable).
Ese Keycloak necesita un cliente **público** con PKCE S256, la redirección `com.venuenexa.pocmux:/oauth2redirect`, la de
cierre `com.venuenexa.pocmux:/logout`, rotación del refresh token y emisor por HTTPS (AppAuth no admite HTTP).
Si tiene los ámbitos `acr` y `tenant`, añádelos en `KEYCLOAK_SCOPES`. Lista completa: `docs/identidad-keycloak.md` §5.

## Reglas respetadas

Sin commits (solo `git init` si se pide), sin secretos en el repo, sin otros repos tocados,
firmas verificadas contra código público (detalles y «no verificado» en `docs/informe.md`).
