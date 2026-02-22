# Обновление приложения на сервере

Как применить изменения в коде на боевом сервере (после того как первый раз всё настроено по [DEPLOY.md](DEPLOY.md)).

---

## Порядок действий

### 1. На своём компьютере: изменения и пуш в Git

```bash
cd /path/to/english-movies/server   # твоя локальная папка проекта

# Вносишь изменения в коде, затем:
git add .
git commit -m "Описание изменений"
git push origin main
```

(Ветку `main` замени на свою, если используешь другую.)

---

### 2. На сервере: подтянуть код и пересобрать

Подключись по SSH и выполни:

```bash
ssh root@ТВОЙ_IP   # или твой пользователь и IP сервера

cd /opt/english-movies/server   # или /opt/english-movies-server — куда клонировал проект

git pull origin main

./scripts/deploy.sh
```

Скрипт `deploy.sh`:
- соберёт проект (`./mvnw package -DskipTests`);
- скопирует собранный JAR в `app.jar`;
- перезапустит сервис `english-movies` (systemd).

---

### 3. Проверка

На сервере:

```bash
curl -s "http://localhost:8080/api/movies/titles?limit=2"
```

Или снаружи (если настроен домен и HTTPS):

```bash
curl -s "https://sae-polyglot.ru/api/movies/titles?limit=2"
```

Должен вернуться JSON. Статус сервиса:

```bash
systemctl status english-movies
```

---

## Кратко (одной строкой на сервере)

После `git push` с локальной машины на сервере достаточно:

```bash
cd /opt/english-movies/server && git pull && ./scripts/deploy.sh
```

---

## Если что-то пошло не так

| Проблема | Что сделать |
|----------|-------------|
| `git pull` просит пароль / отказ доступа | Настроить SSH-ключ на сервере для доступа к репо или использовать HTTPS с токеном. |
| `./scripts/deploy.sh`: Permission denied | `chmod +x scripts/deploy.sh` |
| Сборка падает (mvnw) | Смотреть вывод скрипта; на сервере должна быть установлена Java 21. |
| Сервис не стартует после deploy | `journalctl -u english-movies -n 100` — смотреть логи. |

Сброс БД и полное пересоздание схемы (если нужно): см. раздел «Сброс схемы» в [DEPLOY.md](DEPLOY.md).
