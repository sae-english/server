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

Данные фильмов (content.json и т.д.) должны лежать на сервере. Скопируй папку `storage` из проекта в тот же каталог, откуда запускается приложение (например рядом с `app.jar`), или оставь внутри `server` — загрузчик Liquibase ищет её относительно рабочей директории. Лучше положить в каталог приложения:

```bash
# Если storage уже в репозитории — ничего делать не нужно.
# Если нет — с твоего Mac:
scp -r storage root@IP_СЕРВЕРА:/opt/english-movies/server/
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
ExecStart=/usr/bin/java -Xmx256m -jar /opt/english-movies/server/app.jar --spring.profiles.active=prod
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
```

Если проект в `/opt/english-movies-server`, замени путь в `WorkingDirectory` и в `ExecStart` на `/opt/english-movies-server`.

```bash
systemctl daemon-reload
systemctl enable english-movies
```

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

Когда выкатил новый код (через `git pull` или залив файлов):

```bash
cd /opt/english-movies/server
git pull   # если используешь Git
./scripts/deploy.sh
```

Скрипт заново соберёт проект, подменит `app.jar` и перезапустит сервис.

**Сброс БД и пересоздание схемы:** перед перезапуском можно выполнить `./scripts/reset-schema.sh` (см. раздел «Сброс схемы» ниже).

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

Создай конфиг сайта:

```bash
nano /etc/nginx/sites-available/english-movies
```

Вставь (подставь свой домен и при необходимости порт):

```nginx
server {
    listen 80;
    server_name api.твой-домен.ru;   # или твой-домен.ru

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

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
