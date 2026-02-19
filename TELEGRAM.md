# Отправка сообщений в Telegram-канал с сервера

Сервер умеет отправлять сообщения в твой личный Telegram-канал. Используй это для уведомлений, логов или любых сообщений из кода.

## 1. Создать бота

1. Открой в Telegram [@BotFather](https://t.me/BotFather).
2. Отправь `/newbot`, придумай имя и username бота.
3. Сохрани **токен** вида `123456789:AAH...` — это `TELEGRAM_BOT_TOKEN`.

## 2. Создать канал и добавить бота

1. Создай канал (Channel) в Telegram — можно приватный, только для себя.
2. Зайди в канал → Управление → Администраторы → Добавить администратора.
3. Найди своего бота по username и добавь его. Без прав админа бот не сможет писать в канал.

## 3. Узнать chat_id канала

Для канала chat_id выглядит как `-1001234567890`.

**Способ 1:** Добавь в канал бота [@userinfobot](https://t.me/userinfobot) или [@RawDataBot](https://t.me/RawDataBot), перешли любое сообщение из канала ему — он покажет chat id.

**Способ 2:** Для **публичного** канала можно использовать username: `@имя_канала` (например `@my_english_channel`). В конфиг тогда указываешь `telegram.chat-id=@имя_канала`.

## 4. Настроить сервер

Задай переменные окружения (или добавь в `application.yaml` / `application-dev.yaml`):

```bash
TELEGRAM_BOT_TOKEN=123456789:AAH...
TELEGRAM_CHAT_ID=-1001234567890
```

Или в конфиге:

```yaml
telegram:
  bot-token: "123456789:AAH..."
  chat-id: "-1001234567890"
```

**Важно:** токен и chat_id не коммить в репозиторий. На проде используй переменные окружения.

## 5. Как отправлять сообщения

**Через API (для теста или из фронта):**

```bash
curl -X POST http://localhost:8080/api/telegram/send \
  -H "Content-Type: application/json" \
  -d '{"text": "Привет из сервера!"}'
```

**Из кода Java:** внедри `TelegramService` и вызови:

```java
@Autowired
private TelegramService telegramService;

// ...
telegramService.sendMessage("Новое слово добавлено в словарь!");
```

**Проверить, что Telegram включён:**

```bash
curl http://localhost:8080/api/telegram/status
# {"enabled":true}
```

Если `enabled: false` — проверь, что заданы `TELEGRAM_BOT_TOKEN` и `TELEGRAM_CHAT_ID`.
