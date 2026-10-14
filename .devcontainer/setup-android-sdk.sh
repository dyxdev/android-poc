#!/bin/bash
# Instala el Android SDK (solo cmdline-tools + lo necesario para compilar y testear)
# en un Codespace/Gitpod. Tarda 5-15 min la primera vez. Idempotente: si ya existe, no reinstala.
set -e

# onCreateCommand se ejecuta en la raíz del workspace: guardarla antes de cualquier cd.
WORKSPACE="$(pwd)"
SDK="$HOME/android-sdk"
CMDLINE_VERSION="11076708"

if [ -d "$SDK/platforms/android-36" ] && [ -f "$WORKSPACE/local.properties" ]; then
  echo "Android SDK ya instalado en $SDK. Nada que hacer."
  exit 0
fi

echo "Descargando Android cmdline-tools..."
mkdir -p "$SDK/cmdline-tools"
cd /tmp
curl -fsSL -o cmdtools.zip "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_VERSION}_latest.zip"
rm -rf "$SDK/cmdline-tools/latest"
unzip -q cmdtools.zip -d "$SDK/cmdline-tools"
mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
rm cmdtools.zip

export ANDROID_HOME="$SDK"
export ANDROID_SDK_ROOT="$SDK"
export PATH="$SDK/cmdline-tools/latest/bin:$SDK/platform-tools:$PATH"

echo "Aceptando licencias..."
yes | sdkmanager --licenses > /dev/null 2>&1 || true

echo "Instalando platform + build-tools..."
sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0" \
  || sdkmanager "platform-tools" "platforms;android-36" "build-tools;35.0.0"

# Apunta Gradle al SDK (local.properties va en la raíz del workspace, no en /tmp).
echo "sdk.dir=$SDK" > "$WORKSPACE/local.properties"

echo "SDK listo en $SDK. Comprueba con: ./gradlew --version"
