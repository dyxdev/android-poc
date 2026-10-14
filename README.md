# PoC Android Mux Player con Mux Data solo con consentimiento

Prueba de concepto para ADR-AND-0032 (decisión #8): demostrar con evidencia de red que
Mux Data está apagado sin consentimiento de analítica (0 peticiones a `*.litix.io`).

## Estructura

- `app/` — pantalla única, `MainActivity`, config debug (cleartext a 10.0.2.2 + CA de usuario).
- `core:consent` — DataStore (denegado por defecto) + `ConsentGatingNetworkRequest`
  (puerta `INetworkRequest`: delega en `MuxNetwork` con consentimiento, descarta sin él,
  lectura en cada envío, sin reenvío de lo previo) + contadores por método y host.
- `core:common` — dispatchers inyectados. `core:testing` — fakes (sin mocking).
- `feature:player-lab` — pantalla Compose + ViewModel (UDF) + 3 modos
  (1 candidato con puerta, 2 control sin binding, 3 reserva Media3 propio).
- `tools/token-server.mjs` — JWT RS256 local, puerto 8787 (claves de Andy, fuera del repo).
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

## Reglas respetadas

Sin commits (solo `git init` si se pide), sin secretos en el repo, sin otros repos tocados,
firmas verificadas contra código público (detalles y «no verificado» en `docs/informe.md`).
