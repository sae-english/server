#!/usr/bin/env bash
#
# Сбрасывает схему englishmovies и историю Liquibase.
# После следующего запуска приложения Liquibase заново создаст схему и все таблицы.
#
# Использование:
#   На сервере (под пользователем с доступом к БД):
#     ./scripts/reset-schema.sh
#   Или с параметрами:
#     PGHOST=localhost PGPORT=5432 PGDATABASE=englishmovies PGUSER=englishmovies PGPASSWORD=secret ./scripts/reset-schema.sh
#
set -e

PGHOST="${PGHOST:-localhost}"
PGPORT="${PGPORT:-5432}"
PGDATABASE="${PGDATABASE:-englishmovies}"
PGUSER="${PGUSER:-englishmovies}"

export PGHOST PGPORT PGDATABASE PGUSER
# PGPASSWORD передаётся отдельно или в .pgpass

echo "Database: $PGUSER@$PGHOST:$PGPORT/$PGDATABASE"
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
