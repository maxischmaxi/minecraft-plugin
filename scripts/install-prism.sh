#!/usr/bin/env bash
# builds max mod and copies it (plus fabric-api if missing) into the prism 26.3 instance
set -euo pipefail

PROJECT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INSTANCE="$HOME/.local/share/PrismLauncher/instances/26.3"
MODS="$INSTANCE/minecraft/mods"
FABRIC_API=0.163.0+26.3

if ! grep -q 'net.fabricmc.fabric-loader' "$INSTANCE/mmc-pack.json"; then
	echo "fabric loader is not installed in the prism instance 26.3."
	echo "prism: instance -> edit -> version -> install loader -> fabric"
	exit 1
fi

(cd "$PROJECT" && ./gradlew build -q)
mkdir -p "$MODS"

rm -f "$MODS"/xray-*.jar "$MODS"/maxmod-*.jar
cp "$PROJECT"/build/libs/maxmod-*[0-9].jar "$MODS/"
echo "copied $(ls "$MODS" | grep '^maxmod-')"

if ! ls "$MODS" | grep -q '^fabric-api'; then
	curl -sSfL -o "$MODS/fabric-api-$FABRIC_API.jar" \
		"https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/$FABRIC_API/fabric-api-$FABRIC_API.jar"
	echo "downloaded fabric-api $FABRIC_API"
fi
