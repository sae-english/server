# Подключение Yandex Translate к приложению

Краткая инструкция: как получить доступ к [Yandex Cloud Translate API](https://cloud.yandex.ru/docs/translate/) и настроить приложение.

---

## 1. Регистрация и каталог в Yandex Cloud

1. Зайди на [console.yandex.cloud](https://console.yandex.cloud).
2. Войди по аккаунту Яндекса (или зарегистрируйся).
3. Если облако ещё не создано — нажми **Создать облако**, введи название и платёжный аккаунт (при первом использовании можно получить стартовый грант).
4. Создай **каталог** (Folder): в левом меню выбери **Каталоги** → **Создать каталог**. Запомни его **ID** (понадобится как `folder-id`).

---

## 2. Включение Translate API и создание API-ключа

1. В консоли открой **Перевод** (Translate) в меню или перейди в [Translate в Yandex Cloud](https://console.yandex.cloud/folders/<FOLDER_ID>/translate).
2. Убедись, что сервис **Перевод** доступен в выбранном каталоге (при необходимости включи его).
3. Создай **API-ключ** для доступа к API из кода:
   - В консоли: **Управление доступом** (IAM) → **Сервисные аккаунты** (или **API-ключи**).
   - Либо: [Документация — API-ключ](https://cloud.yandex.ru/docs/iam/operations/api-key/create) (создание ключа для сервисного аккаунта с ролью `ai.translate.user` или `editor`).
   - Скопируй созданный **API-ключ** — он понадобится в конфиге приложения.

Альтернатива API-ключу — [IAM-токен](https://cloud.yandex.ru/docs/iam/operations/iam-token/create) (срок действия около 12 часов, для продакшена обычно используют API-ключ или ключ сервисного аккаунта).

---

## 3. Настройка приложения

В конфиге задай переменные (или пропиши в `application.yaml` / `application-dev.yaml`):

| Переменная / свойство | Описание |
|------------------------|----------|
| `yandex.translate.api-key` или `YANDEX_TRANSLATE_API_KEY` | API-ключ из шага 2 |
| `yandex.translate.folder-id` или `YANDEX_TRANSLATE_FOLDER_ID` | ID каталога из шага 1 |

**Пример для dev** (`application-dev.yaml`):

```yaml
yandex:
  translate:
    api-key: ваш_api_ключ
    folder-id: b1gxxxxxxxxxxxxxxxxxx
```

**Пример для prod** (лучше через переменные окружения в systemd):

```ini
Environment="YANDEX_TRANSLATE_API_KEY=ваш_ключ"
Environment="YANDEX_TRANSLATE_FOLDER_ID=ваш_folder_id"
```

В `application.yaml` уже есть плейсхолдеры:

```yaml
yandex:
  translate:
    api-key: ${YANDEX_TRANSLATE_API_KEY:}
    folder-id: ${YANDEX_TRANSLATE_FOLDER_ID:}
```

Если оба значения заданы, сервис `YandexTranslateService` будет доступен и сможет вызывать API перевода.

---

## 4. Использование в коде

Сервис `YandexTranslateService`:

- **`isEnabled()`** — возвращает `true`, если заданы API-ключ и folder-id.
- **`translate(String text, String targetLanguageCode)`** — переводит текст на указанный язык (исходный язык определяется автоматически).
- **`translate(String text, String sourceLanguageCode, String targetLanguageCode)`** — переводит с указанного языка на целевой.

Пример:

```java
if (yandexTranslateService.isEnabled()) {
    String translated = yandexTranslateService.translate("Hello, world!", "ru");
    // "Привет, мир!"
}
```

Коды языков — в формате [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) (например, `en`, `ru`).

---

## 5. Документация и лимиты

- [Перевод — быстрый старт](https://yandex.cloud/ru/docs/translate/quickstart)
- [Справочник API перевода](https://yandex.cloud/ru/docs/translate/api-ref/Translation/translate)
- Тарифы и лимиты: [Yandex Cloud Translate — цены](https://cloud.yandex.ru/docs/translate/pricing). Есть бесплатный tier.

---

## Краткий чеклист

1. Зарегистрироваться в Yandex Cloud, создать каталог, запомнить **folder ID**.
2. Включить сервис «Перевод», создать **API-ключ** (или IAM-токен).
3. В приложении задать `yandex.translate.api-key` и `yandex.translate.folder-id` (или переменные окружения).
4. Использовать `YandexTranslateService.translate(...)` в коде или через REST-контроллер.
