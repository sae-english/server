# Локальный запуск с HTTPS

Два способа получить **https** при работе на своей машине.

---

## 1. Реальный адрес с HTTPS (туннель)

Сервер крутится на `localhost:8080`, а снаружи к нему ходят по **публичному https-URL**. Удобно для Telegram-бота, тестов с телефона, шаринга ссылки.

### ngrok — по шагам

**Шаг 1. Установка**

macOS (Homebrew):

```bash
brew install ngrok
```

Проверка: `ngrok version`

**Шаг 2. Регистрация и авторизация**

1. Зайди на [ngrok.com](https://ngrok.com) и зарегистрируйся (или войди).
2. В личном кабинете: **Your Authtoken** (или **Setup** → **Your Authtoken**).
3. Скопируй токен и выполни в терминале (подставь свой токен):

```bash
ngrok config add-authtoken ТВОЙ_ТОКЕН
```

Сообщение вроде `Authtoken saved to configuration file` — значит, всё ок.

**Шаг 3. Запуск сервера**

В первом терминале запусти приложение (порт по умолчанию — 8080):

```bash
cd /Users/alexander/Desktop/MyProjects/english-movies/server
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Дождись строки вида `Started EnglishMoviesServerApplication` и что сервер слушает порт 8080.

**Шаг 4. Запуск туннеля ngrok**

Во **втором** терминале:

```bash
ngrok http 8080
```

В консоли появится блок с адресами, например:

```
Forwarding   https://abc1-23-45-67.ngrok-free.app -> http://localhost:8080
```

**Шаг 5. Использование**

- Публичный адрес твоего API: **https://…ngrok-free.app** (из строки `Forwarding`).
- Открывай в браузере: `https://…ngrok-free.app/api/…` (подставь свой URL и путь, например `/api/movies/titles` или `/api/dictionary`).
- Для Telegram webhook или других сервисов укажи этот https-URL.

При первом заходе в браузере ngrok может показать предупреждающую страницу — нажми «Visit Site».

**Шаг 6. Остановка**

- Остановить туннель: в терминале с ngrok нажми **Ctrl+C**.
- Остановить сервер: в терминале с Spring Boot — **Ctrl+C**.

**Важно:** на бесплатном плане при каждом новом `ngrok http 8080` URL меняется. Чтобы сохранить один и тот же адрес, нужен платный план (или перезапускать ngrok и копировать новый URL при каждом запуске).

### Cloudflare Tunnel (cloudflared)

```bash
# Установка (macOS)
brew install cloudflared

# Быстрый временный туннель (без аккаунта)
cloudflared tunnel --url http://localhost:8080
```

Выдаст временный https-URL. Для постоянного домена нужен аккаунт Cloudflare и настройка именованного туннеля.

**Итог:** запускаешь приложение как обычно (`./mvnw spring-boot:run` или из IDE), в другом терминале — туннель. Внешний мир ходит по **https**, трафик приходит на твой localhost.

---

## 2. HTTPS только на localhost

Сервер сам слушает по **https** на `https://localhost:8443` (без туннеля, без выхода в интернет).

### Шаг 1: Создать самоподписанный сертификат (один раз)

В каталоге проекта (или в `src/main/resources`):

```bash
keytool -genkeypair -alias localhost -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 3650 \
  -storepass changeit -dname "CN=localhost, OU=Dev, O=Local, L=City, ST=State, C=US"
```

Пароль хранилища запомни (ниже используется `changeit`). Файл `keystore.p12` добавь в `.gitignore`, чтобы не коммитить.

### Шаг 2: Включить SSL в Spring Boot

Создай профиль, например `application-ssl.yaml` (и не коммить туда пароли/пути к файлам с секретами), или добавь в `application-dev.yaml`:

```yaml
server:
  port: 8443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12   # или file:./keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
```

Запуск с профилем `dev,ssl`:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev,ssl
```

Или в IDE: Active profiles = `dev,ssl`.

Открываешь в браузере: **https://localhost:8443**. Предупреждение о самоподписанном сертификате — нормально, для локальной разработки можно принять.

---

## Кратко

| Цель                         | Решение                    |
|-----------------------------|----------------------------|
| Публичный https-URL (бот, тесты) | Туннель: **ngrok** или **cloudflared** |
| Только https на своей машине    | **Spring Boot SSL** (keystore + `server.ssl`) |

Для «реального адреса с https» при локальном запуске — используй туннель (п. 1).
