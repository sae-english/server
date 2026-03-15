#!/usr/bin/env bash
#
# Сбрасывает схему englishmovies и историю Liquibase.
#
# Режимы:
#   ./scripts/reset-schema.sh           — сброс с сохранением dictionary (бэкап → drop → после перезапуска вызвать restore-dictionary.sh)
#   ./scripts/reset-schema.sh full      — полный сброс, включая dictionary (без бэкапа)
#
# Использование:
#   PGHOST=... PGPASSWORD=... ./scripts/reset-schema.sh [full]
#
set -e

FULL_RESET=
[ "${1:-}" = "full" ] && FULL_RESET=1

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_FILE="${DICTIONARY_BACKUP_FILE:-$SCRIPT_DIR/dictionary_backup.sql}"

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-englishmovies}"
PGUSER="${PGUSER:-englishmovies}"

export PGHOST PGPORT PGDATABASE PGUSER
# PGPASSWORD передаётся отдельно или в .pgpass

echo "Database: $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"
if [ -n "$FULL_RESET" ]; then
  echo "Mode: full reset (dictionary will not be preserved)."
else
  echo "Mode: reset with dictionary preserved (backup → drop → restore after restart)."
fi

# Бэкап dictionary только если не full reset
if [ -z "$FULL_RESET" ]; then
  if psql -v ON_ERROR_STOP=1 -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema = 'englishmovies' AND table_name = 'dictionary'" 2>/dev/null | grep -q 1; then
    echo "Backing up englishmovies.dictionary to $BACKUP_FILE ..."
    pg_dump -h "$PGHOST" -p "$PGPORT" -U "$PGUSER" -d "$PGDATABASE" \
      --data-only --no-owner --no-acl \
      -t 'englishmovies.dictionary' \
      -f "$BACKUP_FILE" 2>/dev/null || true
    if [ -s "$BACKUP_FILE" ]; then
      echo "Dictionary backup saved ($(wc -l < "$BACKUP_FILE") lines)."
    else
      rm -f "$BACKUP_FILE"
    fi
  else
    echo "No dictionary table to backup (schema or table missing)."
  fi
fi

echo "Dropping schema englishmovies and Liquibase history..."

psql -v ON_ERROR_STOP=1 <<'SQL'
-- Удаляем схему со всеми таблицами и данными
DROP SCHEMA IF EXISTS englishmovies CASCADE;

-- Очищаем историю Liquibase (она в public), чтобы при старте приложения миграции выполнились заново
DO $$
BEGIN
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'databasechangelog') THEN
    TRUNCATE TABLE public.databasechangelog CASCADE;
  END IF;
  IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'databasechangeloglock') THEN
    TRUNCATE TABLE public.databasechangeloglock CASCADE;
  END IF;
END $$;
SQL

echo "Done. Restart the application to recreate schema and tables."
echo "  systemctl restart english-movies   # или как у тебя называется сервис"
if [ -z "$FULL_RESET" ]; then
  echo "Then restore dictionary (if backup was created):"
  echo "  ./scripts/restore-dictionary.sh"
fi
