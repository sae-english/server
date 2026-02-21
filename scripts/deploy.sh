#!/usr/bin/env bash
#
# Пересборка и перезапуск приложения на сервере.
# Запускать из каталога server:  ./scripts/deploy.sh
#
set -e

cd "$(dirname "$0")/.."
SERVICE_NAME="${SERVICE_NAME:-english-movies}"

echo "Building..."
./mvnw package -DskipTests -q

JAR=$(ls -t target/server-*.jar 2>/dev/null | head -1)
if [ -z "$JAR" ]; then
  echo "No JAR found in target/"
  exit 1
fi

echo "Installing $JAR -> app.jar"
cp "$JAR" app.jar

echo "Restarting $SERVICE_NAME..."
systemctl restart "$SERVICE_NAME"

echo "Done. Check: systemctl status $SERVICE_NAME"
systemctl status "$SERVICE_NAME" --no-pager || true
