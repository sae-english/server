# Выкладка приложения на сервер

Краткая инструкция: как выложить **english-movies-server** (Spring Boot + PostgreSQL) на арендованный сервер и привязать домен.

---

## Рекомендуемый вариант: БД на сервере + лёгкая пересборка

Ниже — один раз настраиваешь сервер, потом обновление = `git pull` + `./scripts/deploy.sh`.

### Один раз: подготовка сервера

Подключись по SSH, затем выполни по шагам.

**1. Обновление и установка Java 21 + PostgreSQL**

```bash
apt update && apt install -y openjdk-21-jdk postgresql postgresql-contrib git
```

**2. База данных**

```bash
sudo -u postgres psql -c "CREATE USER englishmovies WITH PASSWORD 'придумай_надёжный_пароль';"
sudo -u postgres psql -c "CREATE DATABASE englishmovies OWNER englishmovies;"
```

Пароль запомни — он понадобится для конфига приложения.

**3. Код проекта**

Если репозиторий уже есть в Git:

```bash
cd /opt
git clone https://github.com/ТВОЙ_ЮЗЕР/english-movies.git
cd english-movies/server
```

Если пока без Git — залей папку `server` с компьютера:

```bash
# На твоём Mac в папке с проектом:
scp -r server root@IP_СЕРВЕРА:/opt/english-movies-server
# На сервере:
cd /opt/english-movies-server
```

**4. Конфиг для production**

Создай файл конфигурации (подставь свой пароль БД):

```bash
cat > /opt/english-movies/server/src/main/resources/application-prod.yaml << 'EOF'
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/englishmovies?currentSchema=englishmovies
    username: englishmovies
    password: ТВОЙ_ПАРОЛЬ_БД
  liquibase:
    change-log: classpath:db/changelog/db.changelog-master.xml
    liquibase-schema: public
    default-schema: englishmovies
  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.PostgreSQLDialect
EOF
```

(замени `ТВОЙ_ПАРОЛЬ_БД` на пароль из шага 2)

**5. Папка storage на сервере**

Данные фильмов, сериалов и comedy (content.json и т.д.) должны лежать на сервере. Загрузчик Liquibase при каждом старте читает `storage/content-manifest.json` и по путям из ключей `movies`, `series_episode`, `comedy` заливает данные в БД. Папку `storage` положи в **тот же каталог, откуда запускается приложение** (WorkingDirectory в systemd), т.к. загрузчик ищет её относительно рабочей директории (`user.dir/storage`):

```bash
# Если деплой через Jenkins только копирует JAR — на сервере storage не обновится.
# Нужно либо копировать и storage (rsync/scp), либо собирать и запускать из клона репо на сервере (git pull + deploy.sh).
# Проверка на сервере:
ls -la /opt/english-movies/server/storage/content-manifest.json
# В manifest должен быть ключ "comedy" и путь comedy/george-carlin-you-are-all-diseased/content.json
grep comedy /opt/english-movies/server/storage/content-manifest.json
```

**6. Сервис systemd**

Создай юнит (путь к каталогу поправь, если проект лежит не в `/opt/english-movies/server`):

```bash
cat > /etc/systemd/system/english-movies.service << 'EOF'
[Unit]
Description=English Movies API
After=network.target postgresql.service

[Service]
Type=simple
WorkingDirectory=/opt/english-movies/server
# Секреты (Telegram, Yandex Translate и т.д.) — из файла, не храни в репозитории
EnvironmentFile=/opt/english-movies/server/.env
ExecStart=/usr/bin/java -Xmx256m -jar /opt/english-movies/server/app.jar --spring.profiles.active=prod
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
```

Если проект в `/opt/english-movies-server`, замени путь в `WorkingDirectory` и в `ExecStart` на `/opt/english-movies-server`. В `EnvironmentFile` укажи путь к `.env` в том же каталоге.

**6.1. Файл с секретами (обязательно на сервере)**

Все секреты хранятся в **одном файле** `.env` в рабочем каталоге приложения. Его не коммитят в git. Systemd подхватывает переменные через `EnvironmentFile`.

В репозитории есть файл **`.env.example`** — список переменных без значений. На сервере можно скопировать его в `.env` и заполнить: `cp .env.example .env && nano .env`.

Либо создай вручную (подставь свои значения):

```bash
cat > /opt/english-movies/server/.env << 'ENVEOF'
# БД (логин можно оставить в application-prod.yaml, пароль — только здесь)
SPRING_DATASOURCE_PASSWORD=твой_пароль_postgres

# Telegram (бот для рассылки слов в личку)
TELEGRAM_BOT_TOKEN=токен_от_BotFather
TELEGRAM_CHAT_ID=твой_user_id_из_RawDataBot

# Yandex Translate (кнопка «Перевести» в UI)
YANDEX_TRANSLATE_API_KEY=твой_api_ключ
YANDEX_TRANSLATE_FOLDER_ID=b1g6mf6i36n8pi112ugk
ENVEOF
chmod 600 /opt/english-movies/server/.env
```

После любого изменения `.env`: `systemctl restart english-movies`.

**7. Первый запуск**

```bash
cd /opt/english-movies/server   # или /opt/english-movies-server
chmod +x scripts/deploy.sh
./scripts/deploy.sh
```

Скрипт соберёт JAR, скопирует его в `app.jar` и перезапустит сервис. При первом старте Liquibase создаст схему и таблицы и при необходимости загрузит данные из `storage`.

Проверка:

```bash
curl -s http://localhost:8080/api/movies/titles?limit=2
```

---

### Обновление (пересборка) приложения

Пошаговая инструкция: **[UPDATE.md](UPDATE.md)**.

Когда выкатил новый код (через `git pull` или залив файлов):

```bash
cd /opt/english-movies/server
git pull   # если используешь Git
./scripts/deploy.sh
```

Скрипт заново соберёт проект, подменит `app.jar` и перезапустит сервис.

**Сброс БД и пересоздание схемы:** перед перезапуском можно выполнить `./scripts/reset-schema.sh` (см. раздел «Сброс схемы» ниже).

### Деплой через Jenkins: почему не подтягивается storage

При сборке в Jenkins репозиторий клонируется в **workspace** — там есть и код, и папка `storage` из git. Но на сервер в шаге деплоя обычно копируют **только JAR** (из `target/`). Папка `storage` на сервере при этом не обновляется — там остаётся старая версия (или её вообще нет).

**Что сделать в Jenkins:** в шаге деплоя после копирования JAR **дополнительно копировать папку `storage`** на сервер в тот же каталог, откуда запускается приложение (рядом с `app.jar`).

Пример для «Execute shell» в Jenkins (сборка и деплой на тот же сервер, где крутится Jenkins-агент):

```bash
./mvnw package -DskipTests -q
sudo cp target/server-0.0.1-SNAPSHOT.jar /opt/server/app.jar
# Обновить storage (manifest, comedy, movies, series) — иначе раздел comedy и новые данные не появятся
sudo rsync -a --delete storage/ /opt/server/storage/
sudo systemctl restart english-movies
```

Если `rsync` на агенте нет: `sudo cp -r storage/* /opt/server/storage/` (предварительно создать `sudo mkdir -p /opt/server/storage`).

Альтернатива: не копировать JAR на сервер, а в Jenkins по SSH заходить на сервер и выполнять там `cd /opt/server && git pull && ./scripts/deploy.sh` — тогда и код, и `storage` будут из репо на сервере.

---

## 1. Подключение к серверу

Поставщик (Reg.ru, Timeweb, Selectel и т.п.) даёт тебе:
- **IP-адрес** сервера
- **Логин** (часто `root`) и **пароль** или **SSH-ключ**

Подключение с твоего компьютера:

```bash
ssh root@ТВОЙ_IP_АДРЕС
```

(или `ssh пользователь@IP`, если логин другой)

---

## 2. Установка на сервере Docker и Docker Compose

На сервере (после входа по SSH) выполни:

```bash
# Ubuntu/Debian
apt update && apt install -y docker.io docker-compose-v2
systemctl enable docker && systemctl start docker
```

Проверка:

```bash
docker --version
docker compose version
```

---

## 3. Загрузка проекта на сервер

**Вариант А — через Git (удобно для обновлений):**

На сервере:

```bash
cd /opt
git clone https://github.com/ТВОЙ_ЮЗЕР/english-movies.git
cd english-movies/server
```

(замени URL на свой репозиторий; если репо приватный — настрой на сервере SSH-ключ или токен)

**Вариант Б — через SCP с твоего компьютера:**

На **твоём Mac** (в папке с проектом):

```bash
cd /Users/alexander/Desktop/MyProjects/english-movies
scp -r server root@ТВОЙ_IP_АДРЕС:/opt/english-movies-server
```

Потом на сервере:

```bash
cd /opt/english-movies-server
```

---

## 4. Запуск приложения в production

На сервере в каталоге с проектом (где лежат `Dockerfile` и `docker-compose.prod.yml`):

```bash
cd /opt/english-movies/server
# или cd /opt/english-movies-server
```

Создай файл `.env` с паролем БД (и при желании пользователем):

```bash
echo 'POSTGRES_USER=englishmovies' > .env
echo 'POSTGRES_PASSWORD=придумай_надёжный_пароль' >> .env
```

Запуск:

```bash
docker compose -f docker-compose.prod.yml up -d --build
```

Проверка:

```bash
docker compose -f docker-compose.prod.yml ps
curl http://localhost:8080/actuator/health
```

(если у тебя нет actuator — проверь любой свой API-эндпоинт, например `curl http://localhost:8080/...`)

Приложение слушает порт **8080**.

---

## 5. Nginx как обратный прокси и привязка домена

Чтобы ходить на сервер по домену (например `api.твой-домен.ru`) и при желании отдать статику или фронт с того же домена, на сервере ставят Nginx.

Установка Nginx:

```bash
apt install -y nginx
```

Создай конфиг сайта (можешь использовать заготовку из `nginx/sae-polyglot.conf`):

```bash
nano /etc/nginx/sites-available/english-movies
```

Вставь (подставь свой домен и при необходимости порт):

```bash
cp /opt/server/nginx/sae-polyglot.conf /etc/nginx/sites-available/sae-polyglot
```

При необходимости поправь `server_name` (если домен другой, не `sae-polyglot.ru`).

Включи сайт и перезапусти Nginx:

```bash
ln -s /etc/nginx/sites-available/english-movies /etc/nginx/sites-enabled/
nginx -t && systemctl reload nginx
```

В панели управления доменом (reg.ru) для этого сервера добавь **A-запись**:
- тип **A**, имя **api** (или `@` для корня домена), значение — **IP твоего сервера**.

Через 5–30 минут `http://api.твой-домен.ru` должен открывать твой API.

---

## 6. HTTPS (SSL) через Let's Encrypt

На сервере:

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d api.твой-домен.ru
```

Certbot сам поправит конфиг Nginx и будет продлевать сертификат. После этого API будет доступен по `https://api.твой-домен.ru`.

---

## 7. Полезные команды

| Действие | Команда |
|----------|--------|
| Логи приложения | `docker compose -f docker-compose.prod.yml logs -f app` |
| Остановить | `docker compose -f docker-compose.prod.yml down` |
| Запустить снова | `docker compose -f docker-compose.prod.yml up -d` |
| Пересобрать после изменений кода | `docker compose -f docker-compose.prod.yml up -d --build` |

---

## Краткий чеклист

1. Арендован сервер, куплен домен.
2. Подключился по SSH.
3. Установил Docker и Docker Compose.
4. Загрузил проект (git или scp).
5. Создал `.env` с `POSTGRES_PASSWORD` (и при желании `POSTGRES_USER`).
6. Запустил: `docker compose -f docker-compose.prod.yml up -d --build`.
7. Настроил Nginx и A-запись на IP сервера.
8. По желанию включил HTTPS через `certbot --nginx`.

Если напишешь, какой у тебя ОС на сервере и как именно хочешь отдавать приложение (только API или ещё фронт), можно сузить шаги под твой случай.

---

## Вариант: БД на сервере (без Docker)

Если хочешь, чтобы PostgreSQL работал как обычная служба на сервере, а не в контейнере:

**1. Установка PostgreSQL:**

```bash
apt update && apt install -y postgresql postgresql-contrib
```

**2. Создание БД и пользователя:**

```bash
sudo -u postgres psql -c "CREATE USER englishmovies WITH PASSWORD 'твой_надёжный_пароль';"
sudo -u postgres psql -c "CREATE DATABASE englishmovies OWNER englishmovies;"
```

**3. Приложение** подключается к `localhost:5432`. В `application-prod.yaml` (или через переменные окружения) укажи:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/englishmovies?currentSchema=englishmovies`
- `spring.datasource.username=englishmovies`
- `spring.datasource.password=твой_надёжный_пароль`

При первом запуске Liquibase создаст схему `englishmovies` и все таблицы.

---

## Сброс схемы (пересоздание при следующем запуске)

Чтобы удалить схему и все данные и дать приложению заново создать всё при старте:

**1. Запусти скрипт** (на сервере, из каталога проекта):

```bash
cd /opt/english-movies/server   # или где у тебя лежит проект
chmod +x scripts/reset-schema.sh
PGPASSWORD=твой_пароль ./scripts/reset-schema.sh
```

Скрипт удаляет схему `englishmovies` и очищает таблицы Liquibase в `public` (`databasechangelog`, `databasechangeloglock`). После этого при следующем запуске приложения Liquibase заново выполнит все миграции и создаст схему и таблицы.

**2. Перезапусти приложение:**

```bash
systemctl restart english-movies
# или: docker compose restart app  (если только приложение в Docker, а БД на хосте)
```

Данные из `storage/` (фильмы, контент) загрузятся снова, если у тебя в Liquibase включён загрузчик (custom change), который читает JSON при старте.

**Режимы сброса:**

- **С сохранением dictionary** (по умолчанию): `./scripts/reset-schema.sh` — перед DROP создаётся бэкап `scripts/dictionary_backup.sql`, после перезапуска приложения вызови `./scripts/restore-dictionary.sh`.
- **Полный сброс (включая dictionary):** `./scripts/reset-schema.sh full` — бэкап не создаётся, словарь не восстанавливается.

Файл бэкапа в репозиторий не коммитится (см. `.gitignore`).

**Jenkins (два параметра сборки):**

- `RESET_DB=true` — сброс схемы с сохранением dictionary (бэкап → drop → после перезапуска вызвать `restore-dictionary.sh`).
- `RESET_FULL_DB=true` — полный сброс, включая dictionary (без бэкапа и без restore).
- Оба `false` — БД не трогаем.

Пример шага «Execute shell» в Jenkins:

```bash
# Сброс БД (один из двух режимов)
if [ "$RESET_FULL_DB" = "true" ]; then
  echo "RESET_FULL_DB=true: полный сброс схемы (включая dictionary)..."
  PGPASSWORD=dro11gba /opt/server/scripts/reset-schema.sh full
  echo "Схема БД сброшена."
elif [ "$RESET_DB" = "true" ]; then
  echo "RESET_DB=true: сбрасываем схему, dictionary сохраняем..."
  PGPASSWORD=dro11gba /opt/server/scripts/reset-schema.sh
  echo "Схема БД сброшена."
fi

# сборка и деплой
./mvnw package -DskipTests -q
sudo cp target/server-0.0.1-SNAPSHOT.jar /opt/server/app.jar
sudo rsync -a --delete storage/ /opt/server/storage/
sudo systemctl restart english-movies

# Восстановить dictionary только после обычного сброса (не full)
if [ "$RESET_DB" = "true" ] && [ "$RESET_FULL_DB" != "true" ]; then
  echo "Восстанавливаем словарь..."
  sleep 10
  PGPASSWORD=dro11gba /opt/server/scripts/restore-dictionary.sh
fi
```
