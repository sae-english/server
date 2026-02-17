# Episode data scripts

## Конфигурация

Список директорий сезонов задаётся в `scripts/episode-sources.json`:

```json
[
  "src/main/resources/db/changelog/titles/friends/season-1",
  "src/main/resources/db/changelog/titles/friends/season-2"
]
```

При добавлении нового сезона — добавь строку в этот файл. При `mvn compile` / `./mvnw spring-boot:run` скрипт обработает все указанные папки.

## Структура данных

Данные эпизода хранятся в папке `episode-N` внутри сезона:

```
season-1/
├── episode-1/
│   ├── meta.json       # title_id, season, episode_number, episode_title, credits, note
│   ├── content.json    # массив blocks (scene, dialogue, action, transition, section)
│   ├── episode.sql     # генерируется скриптом
│   └── 001-episode.xml # Liquibase changeset
├── episode-2/
│   ├── meta.json
│   ├── content.json
│   ├── episode.sql
│   └── 001-episode.xml
└── ...
```

## Генерация SQL

Редактируй `meta.json` и `content.json`, затем запусти:

```bash
node scripts/generate-episode-sql.js
```

Скрипт автоматически запускается при сборке (фаза `generate-resources`).

## Добавление нового эпизода / сезона

**Новый эпизод в существующем сезоне:**
1. Создай папку `episode-N` в `season-X/`
2. Добавь `meta.json`, `content.json` и `001-episode.xml` (скопируй из другого эпизода)
3. Добавь include в changelog тайтла: `db/changelog/titles/.../season-X/episode-N/001-episode.xml`
4. Сборка сгенерирует `episode.sql` автоматически

**Новый сезон:**
1. Добавь путь в `scripts/episode-sources.json`, например:
   `"src/main/resources/db/changelog/titles/friends/season-2"`
2. Создай структуру `season-2/episode-1/` с meta.json, content.json, 001-episode.xml
3. Добавь include в changelog тайтла
