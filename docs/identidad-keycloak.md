# Identidad con Keycloak en el PoC de Android (PV1-T11, INT-08)

> Cada afirmación es un **hecho comprobado**, o está marcada **«no verificado»**. Estado a 2026-10-14:
> **el código está escrito y nada de él se ha compilado ni ejecutado todavía** (ni los tests ni el
> login contra un realm). Las decisiones de este documento son de Andy; lo pendiente se marca como tal.
>
> **Actualización 2026-10-16:** el build del Codespace pasó con la identidad y Andy comunicó (2026-10-15) que **el login con
> Keycloak funcionó en su móvil** contra el Keycloak de pruebas (`docs/keycloak-de-pruebas.md`). Siguen sin verificar el
> step-up, la vinculación de Google (falta el cliente OAuth en Google Cloud) y las llamadas a la API.

## 1. Fuentes que mandan
- `ADR-AND-0008` (VenueNexa-android): AppAuth for Android, Keycloak como cliente público con
  Authorization Code + PKCE, Custom Tabs, tokens cifrados con Android Keystore, renovación
  serializada, step-up con `acr_values`/`max_age`, logout completo y Google por el *broker*.
- Guía de web, de la que esto toma lo que aplica: `ADR-WEB-0013` (sesión, step-up, logout),
  `ADR-WEB-0038` (pantallas de Keycloak con tema propio) y `ADR-VN-0007` (Keycloak formalizado).
- `docs/BITACORA.md` de android (Andy, 2026-10-14): el login con Keycloak se probará en este PoC
  cuando Diovi tenga disponible el endpoint de la API.

## 2. Qué cambia respecto a la web (no hay BFF)
| Web | Android |
|---|---|
| El BFF es cliente confidencial y guarda los tokens en una cookie cifrada | La app es cliente público con PKCE y guarda los tokens cifrados con una clave del Keystore |
| El servidor renueva y rota el refresh token | La app renueva y lo rota ella misma, con **una sola renovación en curso** |
| Logout RP-initiated desde el servidor | La app revoca el refresh token, limpia lo local y abre el cierre de sesión SSO de Keycloak en Custom Tabs |
| Límite de inactividad del usuario (ADR-WEB-0013) | **Fuera del PoC** (decisión de Andy, 2026-10-14): ADR-AND-0008 no la define para móvil |

## 3. Qué se escribió (decisiones de Andy, 2026-10-14)
Alcance: **base de ADR-AND-0008 + Google por el broker**. Prueba contra un realm **más tarde**.
- `:core:identity` (módulo nuevo): `IdentityManager` (lógica de sesión, sin AppAuth), `AppAuthBackend`
  (AppAuth 0.11.1: descubrimiento OIDC, intent de login con PKCE, canje, renovación, revocación y cierre SSO),
  `EncryptedTokenStore` + `KeystoreCipher` (AES-GCM no exportable, blob en DataStore) y su módulo Hilt.
- `:app`: `IdentityConfig` desde `BuildConfig`, esquema de redirección `com.venuenexa.pocmux`,
  `data_extraction_rules.xml` que excluye `files/datastore` de la copia en la nube y de la transferencia.
- `:feature:player-lab`: tarjeta «Cuenta» (iniciar sesión, Google con `kc_idp_hint=google`, renovar
  token, cerrar sesión), en la pantalla actual, EN/ES. **Sin captura aprobada:** la revisión visual
  está pendiente (no hay emulador ni herramienta de capturas decidida).
- Dependencia nueva: `net.openid:appauth:0.11.1` (la exige ADR-AND-0008; última publicada en Maven
  Central, consultado 2026-10-14). La API usada se comprobó contra su `sources.jar` publicado.

## 4. Fuera de esta pasada
Step-up (`acr`), llamadas a la API de identidad (no existe aún), vinculación de cuentas de Google,
registro por teléfono (Keycloak no lo trae; sin resolver igual que en ADR-WEB-0038), Play Integrity
(ADR-AND-0028), cancelar la subida en curso al cerrar sesión (el PoC no tiene subida) e inactividad.

## 5. Lo que hay que pedir a backend / Keycloak (PV1-T41)
1. Un cliente **público** para Android, PKCE S256 obligatorio. Su `clientId` entra con `-PkeycloakClientId`.
2. Redirect URI `com.venuenexa.pocmux:/oauth2redirect` y URI de cierre `com.venuenexa.pocmux:/logout`
   (esquema propio exclusivo; ADR-AND-0008 admite también App Link verificado).
3. Rotación del refresh token activada y el `revocation_endpoint` alcanzable por el cliente público.
4. Emisor por HTTPS (`https://<host>/realms/<realm>`, `-PkeycloakIssuer`). **No verificado:** el
   realm de VenueNexa. El de demostración de Refine que usó la web probablemente no sirve: no se puede registrar en un realm ajeno nuestra redirección (no verificado).
5. Para Google: el proveedor con alias `google` en el realm (`kc_idp_hint`). **No verificado.**
6. Los ámbitos `acr` y `tenant` (hoy el PoC pide solo `openid email profile`; `-PkeycloakScopes` los cambia).

Compilar con el realm, por propiedad de Gradle o por variable de entorno (la propiedad manda si están las dos):
- `./gradlew assembleDebug -PkeycloakIssuer=https://<host>/realms/<realm> -PkeycloakClientId=<id>`
- `KEYCLOAK_ISSUER=https://<host>/realms/<realm> KEYCLOAK_CLIENT_ID=<id> ./gradlew assembleDebug`
  (también `KEYCLOAK_SCOPES`). En un Codespace, los secretos de usuario se exponen como variables de entorno.

Los valores entran en el APK al compilar (`BuildConfig`): **cambiarlos exige recompilar e instalar de nuevo**.
No son secretos (cliente público con PKCE: emisor y `clientId`). Sin ellos la tarjeta muestra «Keycloak no está configurado».

## 6. Verificación (condiciones de ADR-AND-0008 que pasan o fallan)
| # | Condición | Cómo | Estado |
|---|---|---|---|
| 1 | Login con Custom Tabs y PKCE, nunca WebView | Probar en dispositivo contra el realm; inspeccionar la petición de autorización (`code_challenge_method=S256`) | pendiente |
| 2 | Ningún token en texto plano en disco | Inspeccionar `files/datastore/identity.preferences_pb` en un dispositivo de depuración | pendiente (el cifrado real no se prueba en JVM) |
| 3 | Blob no descifrable ⇒ se descarta la sesión | `EncryptedTokenStoreTest` (con un cifrado de pega) | escrito, sin ejecutar |
| 4 | Una sola renovación en curso y se guarda el refresh token rotado | `IdentityManagerTest` (5 llamadas concurrentes ⇒ 1 renovación) | escrito, sin ejecutar |
| 5 | Refresh token revocado ⇒ se cierra la sesión local; sin red ⇒ se conserva | `IdentityManagerTest` | escrito, sin ejecutar |
| 6 | Logout revoca, cierra la sesión SSO y limpia lo local | `IdentityManagerTest` (lógica) y prueba en dispositivo (SSO) | lógica escrita sin ejecutar; SSO pendiente |
| 7 | Los tokens no aparecen en copias de seguridad ni en la transferencia | Probar en dispositivo la copia en la nube (`bmgr`) y la transferencia entre dispositivos | pendiente |
| 8 | Sesión cerrada en otro dispositivo ⇒ la app la rechaza en la siguiente renovación | Cerrar la sesión en Keycloak y pulsar «Renovar token» | pendiente |
| 9 | Step-up con `acr_values`/`max_age` | — | fuera de esta pasada |

## 7. Riesgos no verificados
- **Compilación:** nada se compiló; ktlint puede pedir un `./gradlew ktlintFormat`.
- **AppAuth 0.11.1** es de 2022 (appcompat 1.3.0 y browser 1.3.0 como dependencias): comprobar
  compatibilidad con Android 14+ (`minSdk` 34) en el dispositivo.
- Si el emisor no publica `end_session_endpoint`, el cierre SSO se omite y solo se limpia lo local.
- Un emisor por HTTP no funciona (AppAuth y la configuración de red lo rechazan); el cleartext de depuración es solo para `10.0.2.2`.
