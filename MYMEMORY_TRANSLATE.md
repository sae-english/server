# MyMemory — перевод по умолчанию

Приложение использует **MyMemory** (бесплатный HTTP API) для перевода текста. Ключи и регистрация не нужны.

- **Лимит запроса:** до 500 байт (UTF-8) на один вызов — примерно 1–2 короткие фразы.
- **Лимит в день:** 5000 символов без параметра; **50 000 символов**, если указать email в конфиге (см. ниже).

## Конфиг

В `application.yaml` или переменных окружения:

```yaml
translate:
  provider: mymemory   # по умолчанию, можно не указывать
  mymemory:
    email: your@email.com   # необязательно; с email лимит 50k символов/день
```

Переменные окружения: `TRANSLATE_PROVIDER`, `TRANSLATE_MYMEMORY_EMAIL`.

## Использование в коде

Внедри `TranslateService` — по умолчанию подставится MyMemory:

```java
private final TranslateService translateService;

String translated = translateService.translate("Hello, world!", "ru");
// "Привет, мир!"
```

## Вернуться на Yandex

Если нужен Yandex Cloud Translate:

1. Задай в конфиге: `translate.provider=yandex`.
2. Укажи `yandex.translate.api-key` и `yandex.translate.folder-id` (см. YANDEX_TRANSLATE.md).

Тогда будет использоваться Yandex вместо MyMemory.
