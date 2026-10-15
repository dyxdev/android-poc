#!/bin/bash
# Para el Keycloak de pruebas y vuelve a poner el puerto en privado.
set -euo pipefail

PORT=8080
WORK="${KEYCLOAK_TEST_HOME:-$HOME/keycloak-test}"
PIDFILE="$WORK/keycloak.pid"

if [ -f "$PIDFILE" ] && kill -0 "$(cat "$PIDFILE")" 2>/dev/null; then
  kill "$(cat "$PIDFILE")"
  echo "Keycloak parado."
else
  echo "No había un Keycloak de pruebas en marcha."
fi
rm -f "$PIDFILE"

if [ -n "${CODESPACE_NAME:-}" ] && command -v gh > /dev/null 2>&1 &&
  gh codespace ports visibility "$PORT:private" -c "$CODESPACE_NAME" > /dev/null 2>&1; then
  echo "Puerto $PORT vuelto a privado."
else
  echo "Comprueba en la pestaña PORTS que el puerto $PORT esté en privado." >&2
fi
