# Система управления санитарно-эпидемиологического центра

Веб-приложение для автоматизации процессов проверок и контроля организаций в сфере санитарно-эпидемиологического надзора.

## Технологический стек (веб-траектория)

- **Серверный язык:** Java 17+
- **Серверный фреймворк:** Spring Boot 3.2.x
- **Клиентская часть:** HTML, CSS, JavaScript (Thymeleaf templates)
- **База данных:** PostgreSQL
- **ORM:** Spring Data JPA / Hibernate
- **Сборка проекта:** Gradle

## Структура проекта

```text
.
├── build.gradle
├── Dockerfile
├── docker-compose.yml
├── database/
│   └── postgresql/
│       └── schema.sql
└── src/
    ├── main/
    │   ├── java/com/sanepidcenter/
    │   └── resources/
    │       ├── application.properties
    │       ├── static/
    │       └── templates/
    └── test/
```

## Запуск через Docker


```bash
docker compose up --build
```

Что поднимется:
- `sanepidcenter-db` (PostgreSQL 15, порт `5432`)
- `sanepidcenter-app` (Spring Boot, порт `8080`)

Проверка статуса:

```bash
docker compose ps
```

Логи приложения:

```bash
docker compose logs -f app
```

Остановка:

```bash
docker compose down
```

Остановка с удалением тома БД:

```bash
docker compose down -v
```

---

## Как взаимодействовать с БД через командную строку

Ниже — самые частые сценарии.

### Подключение к PostgreSQL в контейнере

```bash
docker exec -it sanepidcenter-db psql -U postgres -d sanepidcenter_db
```

### Выполнить SQL-команду без интерактивного режима

```bash
docker exec -i sanepidcenter-db psql -U postgres -d sanepidcenter_db -c "SELECT now();"
```

### Посмотреть таблицы

```bash
docker exec -i sanepidcenter-db psql -U postgres -d sanepidcenter_db -c "\dt"
```

### Описать структуру таблицы

```bash
docker exec -i sanepidcenter-db psql -U postgres -d sanepidcenter_db -c "\d organization"
```

### Пример выборки данных

```bash
docker exec -i sanepidcenter-db psql -U postgres -d sanepidcenter_db -c "SELECT id, name, city FROM organization LIMIT 10;"
```

### Выполнить SQL-скрипт из файла

```bash
docker exec -i sanepidcenter-db psql -U postgres -d sanepidcenter_db < database/postgresql/schema.sql
```

### Бэкап БД в файл

```bash
docker exec sanepidcenter-db pg_dump -U postgres -d sanepidcenter_db > backup.sql
```

### Восстановление БД из бэкапа

```bash
cat backup.sql | docker exec -i sanepidcenter-db psql -U postgres -d sanepidcenter_db
```

---

## Сборка и тесты

Собрать WAR:

```bash
./gradlew clean bootWar
```

Запустить тесты:

```bash
./gradlew test
```

Готовый WAR-файл:

```text
build/libs/sanepidcenter-1.0.0.war
```

## Полезные endpoints

- Приложение: `GET /`
- Swagger UI: `GET /swagger-ui.html`
- OpenAPI JSON: `GET /v3/api-docs`

