#!/usr/bin/env bash
#
# Восстанавливает данные таблицы dictionary из бэкапа, созданного reset-schema.sh.
# Запускать после перезапуска приложения (когда Liquibase уже пересоздал схему и таблицы).
#
# Использование (из корня проекта или из scripts/):
#   PGPASSWORD=secret ./scripts/restore-dictionary.sh
#   Или: DICTIONARY_BACKUP_FILE=/path/to/backup.sql ./scripts/restore-dictionary.sh
#
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKUP_FILE="${DICTIONARY_BACKUP_FILE:-$SCRIPT_DIR/dictionary_backup.sql}"

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-englishmovies}"
PGUSER="${PGUSER:-englishmovies}"

export PGHOST PGPORT PGDATABASE PGUSER

if [ ! -f "$BACKUP_FILE" ] || [ ! -s "$BACKUP_FILE" ]; then
  echo "No backup file found or empty: $BACKUP_FILE"
  echo "Run reset-schema.sh first (with existing dictionary data) to create a backup."
  exit 0
fi

echo "Database: $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"

# Дождаться, пока Liquibase создаст схему и таблицу (приложение только что перезапустилось)
WAIT_TIMEOUT="${RESTORE_WAIT_TIMEOUT:-120}"
echo "Waiting for schema englishmovies and table dictionary (up to ${WAIT_TIMEOUT}s)..."
for i in $(seq 1 "$WAIT_TIMEOUT"); do
  if psql -v ON_ERROR_STOP=1 -tAc "SELECT 1 FROM information_schema.tables WHERE table_schema = 'englishmovies' AND table_name = 'dictionary'" 2>/dev/null | grep -q 1; then
    echo "Schema and table ready after ${i}s."
    break
  fi
  if [ "$i" -eq "$WAIT_TIMEOUT" ]; then
    echo "Timeout: schema englishmovies or table dictionary not found after ${WAIT_TIMEOUT}s. Is the app running?"
    exit 1
  fi
  sleep 1
done

echo "Restoring dictionary from $BACKUP_FILE ..."
psql -v ON_ERROR_STOP=1 -d "$PGDATABASE" -f "$BACKUP_FILE"

# Подправить последовательность id, чтобы новые записи получали корректный id
psql -v ON_ERROR_STOP=1 -d "$PGDATABASE" -tAc "
  SELECT setval(
    pg_get_serial_sequence('englishmovies.dictionary', 'id'),
    COALESCE((SELECT MAX(id) FROM englishmovies.dictionary), 1)
  );
" >/dev/null

echo "Dictionary restored. Sequence updated."
