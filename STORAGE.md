# Добавление новой сущности (фильм / сериал)

Данные хранятся в папке **`storage/`**. При старте приложения Liquibase читает **`storage/content-manifest.json`** и по путям заливает данные в БД: для фильмов — work + movies (в т.ч. content), для сериалов — work + series + эпизоды в таблице episode.

---

## Общая структура storage

```
storage/
├── content-manifest.json   ← пути: movies и series_episode
├── movies/
│   └── <slug>/
│       └── content.json   ← work + film + content, credits, note (контент в таблице movies)
├── series_episode/
│   └── <slug>/            ← одна папка на сериал
│       ├── work.json      ← work + film сериала
│       ├── episode-1/
│       │   └── content.json
│       ├── episode-2/
│       │   └── content.json
│       └── ...
├── book/                  ← пока не используется
└── music/                 ← пока не используется
```

### Стабильный ключ контента (contentKey)

В **work** (в `content.json` фильма и в `work.json` сериала) укажи **`contentKey`** — уникальную строку, не меняющуюся при перезаливке. По ней из словаря (dictionary) ссылаются на фильм или эпизод.

- **Фильм**: в `work` добавить `"contentKey": "interstellar"`.
- **Сериал**: в `work` в `work.json` добавить `"contentKey": "friends"`.
- **Эпизод**: можно задать `contentKey` в `content.json` или не задавать — тогда загрузчик сгенерирует `{contentKey сериала}-s{season}e{episode_number}` (например `friends-s1e1`).

В БД в **work** и **episode** есть колонка **content_key**, в **dictionary** — **content_key** для привязки к контенту.

---

## Как добавить новый фильм (movie)

### 1. Создать папку и один файл

- Папка: **`storage/movies/<slug>/`**  
  Пример: `storage/movies/inception/`
- Внутри один файл: **`content.json`**

### 2. Заполнить `content.json`

Файл должен содержать **work**, **film**, **content**, **credits**, **note**. Контент сохраняется в таблицу **movies** (эпизоды для фильмов не создаются). Порядок полей может быть любой.

```json
{
  "work": {
    "name": "Inception",
    "type": "MOVIE",
    "language": "ENGLISH"
  },
  "film": {
    "director": "Christopher Nolan",
    "year": 2010,
    "description": "A thief who steals corporate secrets through dream-sharing technology."
  },
  "season": null,
  "episode_number": 1,
  "episode_title": "Inception (Full Script)",
  "credits": {},
  "note": null,
  "content": [
    { "type": "scene", "description": "..." },
    { "type": "dialogue", "speaker": "COBB", "text": "..." },
    { "type": "action", "text": "..." }
  ]
}
```

- **work**: `name` — название, `type` — всегда `"MOVIE"`, `language` — например `"ENGLISH"`, **`contentKey`** — стабильный ключ (например `"inception"`) для ссылок из словаря.
- **film**: `director`, `year`, `description` (все опциональны, но желательны).
- **season**: для фильма всегда **`null`**.
- **episode_number**: для фильма всегда **`1`**.
- **episode_title**, **credits**, **note** — по желанию.
- **content** — массив блоков: `scene`, `dialogue`, `action`, `transition`, `section` и т.д.

### 3. Добавить путь в манифест

Открыть **`storage/content-manifest.json`** и добавить путь в массив **`movies`**:

```json
{
  "movies": [
    "movies/interstellar/content.json",
    "movies/inception/content.json"
  ],
  "series_episode": [
    "series_episode/friends"
  ]
}
```

Путь указывается **относительно папки `storage/`**, без ведущего слеша.

### 4. Перезапустить приложение

Liquibase при старте прочитает манифест и зальёт новую сущность в таблицы `work` и `movies` (в т.ч. content).

---

## Как добавить новый сериал (series)

### 1. Создать папку сериала

- Папка: **`storage/series_episode/<slug>/`**  
  Пример: `storage/series_episode/breaking-bad/`

### 2. Создать `work.json` в корне папки сериала

В этой папке **обязателен** файл **`work.json`** с полями **work** и **film**:

```json
{
  "work": {
    "name": "Breaking Bad",
    "type": "SERIES",
    "language": "ENGLISH",
    "contentKey": "breaking-bad"
  },
  "film": {
    "director": "Vince Gilligan",
    "year": 2008,
    "description": "A high school chemistry teacher turned meth producer."
  }
}
```

- **work.type** для сериала всегда **`"SERIES"`**.

### 3. Создать папки эпизодов и их `content.json`

Структура:

- **`storage/series_episode/<slug>/episode-1/content.json`**
- **`storage/series_episode/<slug>/episode-2/content.json`**
- и т.д.

Имена папок **обязательно** в формате **`episode-1`**, **`episode-2`**, … (так загрузчик их находит и сортирует).

В каждом **`content.json`** эпизода — только данные эпизода, **без** work и film (они уже в `work.json`):

```json
{
  "season": 1,
  "episode_number": 1,
  "episode_title": "101 - Pilot",
  "credits": {},
  "note": null,
  "content": [
    { "type": "scene", "description": "..." },
    { "type": "dialogue", "speaker": "WALTER", "text": "..." }
  ]
}
```

- **season** — номер сезона (число).
- **episode_number** — номер эпизода.
- **content** — массив блоков, как у фильма.

### 4. Добавить путь в манифест

В **`storage/content-manifest.json`** в массив **`series_episode`** добавить **путь к папке сериала** (без имени файла):

```json
{
  "movies": [
    "movies/interstellar/content.json"
  ],
  "series_episode": [
    "series_episode/friends",
    "series_episode/breaking-bad"
  ]
}
```

### 5. Перезапустить приложение

Liquibase загрузит один work и один film из `work.json`, затем по подпапкам **episode-1**, **episode-2**, … загрузит все эпизоды в таблицу `episode`.

---

## Краткий чек-лист

**Новый фильм**

1. Создать `storage/movies/<slug>/content.json` с полями work, film, content, credits, note.
2. В `storage/content-manifest.json` добавить в `movies` путь: `movies/<slug>/content.json`.
3. Перезапустить приложение.

**Новый сериал**

1. Создать папку `storage/series_episode/<slug>/`.
2. Создать в ней `work.json` с work и film.
3. Создать подпапки `episode-1`, `episode-2`, … и в каждой файл `content.json` (season, episode_number, episode_title, credits, note, content).
4. В `storage/content-manifest.json` добавить в `series_episode` путь: `series_episode/<slug>`.
5. Перезапустить приложение.

---

## Типы блоков в `content`

В массиве **content** используются объекты с полем **type** и остальными полями в зависимости от типа:

| type       | поля |
|-----------|------|
| scene     | description |
| dialogue  | speaker, text; опционально: parenthetical, isUncut |
| action    | text |
| transition| text |
| section   | title |

Формат можно расширять при необходимости на бэкенде и в клиенте.

---

## Путь к storage

По умолчанию загрузчик ищет папку **`storage`** относительно рабочей директории процесса (обычно корень модуля `server`).  
Чтобы указать свой каталог, задай при запуске: **`-Dstorage.path=/полный/путь/к/storage`**.
