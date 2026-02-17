# Выкладка приложения на сервер

Краткая инструкция: как выложить **english-movies-server** (Spring Boot + PostgreSQL) на арендованный сервер и привязать домен.

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
