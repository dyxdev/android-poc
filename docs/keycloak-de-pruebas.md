# Keycloak de pruebas en el Codespace (para probar la identidad desde el móvil)

> Estado a 2026-10-15: **los scripts y el realm no se han ejecutado nunca** (se escribieron sin
> acceso al Codespace). Todo lo que sigue es lo previsto y se marca «no verificado» donde depende de ello.
> Es una instancia **solo de pruebas**: sin datos reales, en modo de desarrollo, no es la de VenueNexa.

## Para qué
La web del PoC usa el realm público de demostración de Refine (`refine` en `lemur-0.cloud-iam.com`),
donde no podemos registrar la redirección de la app. Para probar `ADR-AND-0008` en el móvil hace falta
un Keycloak con un **cliente público de Android** (PKCE S256) y una URL **HTTPS** alcanzable desde el
teléfono. Este Keycloak da ambas cosas hasta que backend entregue el realm real (PV1-T41).

## Qué crea
- Keycloak 26.8.1 con su propio JDK 25 (el de Gradle sigue siendo el 17), en `~/keycloak-test`, fuera del repo.
- Realm `venuenexa` (`tools/keycloak-test/realm-venuenexa.json`):
  - Cliente **`venuenexa-android`**: público, solo Authorization Code con PKCE S256, redirección
    `com.venuenexa.pocmux:/oauth2redirect` y cierre `com.venuenexa.pocmux:/logout`.
  - Rotación del refresh token activada y token de acceso de **180 s** (para ver renovar sin esperar).
  - Registro de usuarios y recuperación de contraseña permitidos; ES y EN.
  - Usuario **`demo`**. Las contraseñas de `demo` y de `admin` son aleatorias y quedan solo en
    `~/keycloak-test/credentials.txt` (permisos 600).
- El puerto 8080 en **público** (el móvil necesita llegar sin iniciar sesión en GitHub).

## Cómo usarlo
1. En el Codespace, tras `git pull`: `bash tools/keycloak-test/start.sh`. La primera vez descarga unos
   300 MB y tarda unos minutos; verifica las sumas de control.
2. El script imprime el **emisor** y la línea para compilar. Con tus valores sería:
   `KEYCLOAK_ISSUER=https://<codespace>-8080.app.github.dev/realms/venuenexa KEYCLOAK_CLIENT_ID=venuenexa-android ./gradlew assembleDebug`
3. Descarga e instala el APK en el móvil (ver cómo en la conversación; `app/build/outputs/apk/debug/app-debug.apk`).
4. En la app, tarjeta **Cuenta**: «Iniciar sesión» abre Keycloak en una pestaña del navegador; entra con
   `demo` y la contraseña de `credentials.txt` (`cat ~/keycloak-test/credentials.txt`).
5. Al terminar: `bash tools/keycloak-test/stop.sh` (para el servidor y vuelve a poner el puerto en privado).

Si cambias el emisor o el cliente hay que **recompilar e instalar de nuevo** (entran en `BuildConfig`).
No ejecutes Gradle y Keycloak a la vez en un Codespace de 2 núcleos: se pelean por la memoria.

## Google (opcional)
El botón «Entrar con Google» de la app envía `kc_idp_hint=google`: Keycloak lleva directo a Google si el realm
tiene ese proveedor. `start.sh` lo crea **solo si** existen los secretos `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET`
(el secreto va a un archivo temporal 600, no a la línea de comandos ni al repo). Hace falta una cuenta de Google
Cloud, y crear ese cliente es un recurso en un servicio externo, así que lo haces tú:
1. Google Cloud Console > APIs y servicios > **Pantalla de consentimiento de OAuth**: tipo externo, en *pruebas*,
   y añade tu cuenta de Google como usuario de prueba.
2. **Credenciales** > Crear ID de cliente de OAuth > **Aplicación web**. En «URI de redirección autorizados» pon la
   que imprime `start.sh`: `https://<codespace>-8080.app.github.dev/realms/venuenexa/broker/google/endpoint`.
3. En GitHub > Settings > Codespaces > Secrets: crea `GOOGLE_CLIENT_ID` y `GOOGLE_CLIENT_SECRET` y dales acceso a
   este repositorio. Un Codespace en marcha puede no verlos hasta reiniciarlo (no verificado).
4. Repite `bash tools/keycloak-test/start.sh` (con el servidor en marcha solo aplica Google).
Si creas otro Codespace, su URL cambia y hay que registrar la nueva URI en Google. La vinculación con una cuenta
existente usa el flujo por defecto de Keycloak (*first broker login*); **no** comprueba que se exija reautenticar
(RF-008 / ADR-API-0052 siguen pendientes). Todo esto está **sin verificar**.

## Qué se puede comprobar desde el móvil (condiciones de ADR-AND-0008, ver `docs/identidad-keycloak.md` §6)
| # | Cómo | 
|---|---|
| 1 | El login se ve en una pestaña del navegador (Custom Tab), no dentro de la app. |
| 4 | Pulsa «Renovar token» varias veces: sigue «Sesión iniciada» y la hora de caducidad avanza. |
| 5 | En la consola de administración (`/admin`): Sessions, cierra la sesión del usuario y pulsa «Renovar token»: debe decir «La sesión caducó o se revocó» y quedar sin sesión. |
| 6 | «Cerrar sesión» y volver a iniciar: Keycloak pide las credenciales otra vez (cerró el SSO). |
| 8 | Es la misma prueba que el 5 (sesión cerrada en otro sitio). |
No se pueden comprobar sin `adb`: tokens cifrados en disco (2) y exclusión de copias de seguridad (7).

## Seguridad
- **La consola `/admin` y el registro quedan accesibles a cualquiera con la URL mientras el puerto sea público.**
  Por eso la contraseña de administración es aleatoria, no hay datos reales y conviene parar el servidor al terminar.
  Keycloak no permite restringir la consola solo con su configuración: habría que hacerlo en un proxy.
- Nada de esto es la identidad de producción; no reutilices estas credenciales en ningún otro sitio.

## Límites (fuera del alcance de esta instancia)
Google solo si se configura (sección siguiente), sin `acr`/step-up ni ámbito `tenant`, sin el tema propio
(ADR-WEB-0038), sin registro por teléfono, base de datos de desarrollo (H2) y sin alta disponibilidad.

## No verificado
- Los scripts y el JSON de importación: nunca se ejecutaron.
- Que Keycloak 26.8.1 arranque con el JDK 25 de Temurin y acepte `--hostname` con la URL de Codespaces
  detrás de su proxy (`--proxy-headers=xforwarded`), según la documentación de Keycloak consultada el 2026-10-15.
- Que `kcadm.sh` pueda poner la contraseña de `demo` hablando con `localhost` cuando el hostname es el público
  (si falla, el script lo avisa y se pone a mano: Users > demo > Credentials).
- Que `gh codespace ports visibility` funcione con el token del Codespace (si no, a mano: pestaña PORTS > Port Visibility > Public).
- Que Codespaces no muestre una página intermedia de aviso en los puertos públicos; si aparece en la pestaña
  del navegador, hay que aceptarla una vez.
- Que el cliente sin secreto y con PKCE sea aceptado por AppAuth tal cual (condición 1).
- Que `kcadm.sh` cree el proveedor de Google con ese JSON y que Google acepte como redirección una URI de `app.github.dev`.
