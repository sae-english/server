#!/usr/bin/env bash
#
# Полный сброс схемы БД englishmovies + перезапуск сервиса english-movies.
# Использует scripts/reset-schema.sh (Liquibase создаст схему и таблицы заново при следующем старте).
#
# Использование (на сервере):
#   PGPASSWORD=твой_пароль ./scripts/reset-db-and-restart.sh
#

set -e

cd "$(dirname "$0")/.."

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-englishmovies}"
PGUSER="${PGUSER:-englishmovies}"

echo "Resetting schema englishmovies on $PGUSER@$PGHOST:$PGPORT/$PGDATABASE ..."

if [ -z "$PGPASSWORD" ]; then
  echo "PGPASSWORD is not set. Example:" >&2
  echo "  PGPASSWORD=твой_пароль ./scripts/reset-db-and-restart.sh" >&2
  exit 1
fi

PGHOST="$PGHOST" PGPORT="$PGPORT" PGDATABASE="$PGDATABASE" PGUSER="$PGUSER" \
  ./scripts/reset-schema.sh

echo "Restarting systemd service english-movies..."
systemctl restart english-movies

echo
echo "Current status:"
systemctl status english-movies --no-pager || true

echo
echo "Simple health check (HTTP 200 expected, if Liquibase уже отработал):"
curl -s -o /dev/null -w "HTTP %{'%'}{http_code}\n" "http://127.0.0.1:8080/api/movies/titles?limit=1" || true

