# Docker

## Назначение Docker в проекте

Docker в проекте `epidemiological_center` решает ключевые задачи по обеспечению переносимости, воспроизводимости и упрощению развёртывания приложения:

### Основные цели использования

| Задача | Решение через Docker |
|--------|---------------------|
| **Единообразие окружений** | Контейнеры гарантируют идентичную среду на всех стадиях: разработка → тестирование → продакшен |
| **Упрощение запуска** | Одна команда `docker compose up --build` поднимает всё приложение с базой данных |
| **Изоляция зависимостей** | Приложение работает в изолированном окружении с предустановленными Java 17, PostgreSQL 15, Gradle |
| **Автоматизация сборки** | Multi-stage Dockerfile компилирует проект и создаёт минимальный runtime-образ |
| **Health-мониторинг** | Встроенные healthcheck'и контролируют готовность сервисов перед стартом зависимостей |
| **Безопасность** | Запуск приложения от не-root пользователя (`appuser:1001`) снижает риски при компрометации контейнера |

### Архитектура контейнеризации

┌─────────────────────────────────────────┐
│ docker-compose.yml (оркестрация) │
├─────────────────┬───────────────────────┤
│ │ │
▼ ▼ │
┌─────────┐ ┌─────────┐ │
│ app │ │postgres │ │
│ :8080 │ │ :5432 │ │
└────┬────┘ └────┬────┘ │
│ │ │
│ sanepidcenter-network (bridge) │
│ │ │
▼ ▼ │
┌─────────────────────────┐ │
│ Volume: postgres_data │ │
│ (постоянное хранение) │ │
└─────────────────────────┘ │
└───────────────────────────────────────┘

---

## Состав Docker Compose

Файл `docker-compose.yml` описывает два сервиса, объединённых в единую сеть с общим томом для данных.

### Структура конфигурации

```yaml
name: sanepidcenter

services:
  # ─────────────────────────────────────
  # PostgreSQL Database
  # ─────────────────────────────────────
  postgres:
    image: postgres:15-alpine
    container_name: sanepidcenter-db
    environment:
      POSTGRES_DB: sanepidcenter_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./database/postgresql/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - sanepidcenter-network

  # ─────────────────────────────────────
  # Spring Boot Application
  # ─────────────────────────────────────
  app:
    image: sanepidcenter-app:latest
    build:
      context: .
      dockerfile: Dockerfile
    container_name: sanepidcenter-app
    ports:
      - "8080:8080"
    environment:
      DB_HOST: postgres
      DB_USERNAME: postgres
      DB_PASSWORD: postgres
      JWT_SECRET: SanEpidCenterSecretKeyForJWTTokenGenerationAndValidation2024VeryLongSecretKey
      JWT_EXPIRATION: 86400000
      SPRING_PROFILES_ACTIVE: docker
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - sanepidcenter-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1"]
      interval: 30s
      timeout: 5s
      retries: 5

volumes:
  postgres_data:
    driver: local

networks:
  sanepidcenter-network:
    driver: bridge
	```
---

## Переменные окружения

Система конфигурируется через переменные окружения, что позволяет гибко управлять поведением приложения при переходе между средами (Development, Test, Production) без изменения исходного кода.

### Ключевые переменные конфигурации

Все параметры разделены на три логические группы: подключение к БД, безопасность приложения и настройки Spring Boot.

#### 1. База данных (PostgreSQL)
Переменные используются как для инициализации контейнера БД, так и для подключения приложения к ней.

| Переменная | Пример значения | Описание |
|------------|-----------------|----------|
| `DB_HOST` | `localhost` (dev) / `postgres` (docker) | Хост или имя сервиса базы данных |
| `DB_PORT` | `5432` | Порт подключения к PostgreSQL |
| `DB_NAME` | `sanepidcenter_db` | Название создаваемой базы данных |
| `DB_USERNAME` | `postgres` | Логин для подключения к БД |
| `DB_PASSWORD` | `postgres` | Пароль для подключения к БД |

#### 2. Безопасность и JWT
Настройки модуля аутентификации. Критически важно для безопасности продакшена.

| Переменная | Пример значения | Описание |
|------------|-----------------|----------|
| `JWT_SECRET` | `SanEpidCenterSecretKey...` | Секретный ключ для подписи токенов (мин. 256 бит) |
| `JWT_EXPIRATION` | `86400000` | Время жизни токена в миллисекундах (по умолчанию 24 часа) |

#### 3. Spring Boot Framework
Стандартные переменные, управляющие жизненным циклом и профилями Spring.

| Переменная | Пример значения | Описание |
|------------|-----------------|----------|
| `SPRING_PROFILES_ACTIVE` | `docker`, `prod`, `dev` | Активный профиль конфигурации |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...` | Полная строка подключения JDBC (генерируется из `DB_*` автоматически в `application.properties`) |

---

###  Файлы конфигурации

В проекте используется следующий приоритет загрузки конфигураций:

1. **`.env` файл** (только для локальной разработки, **не** коммитится в Git)
   Содержит секреты и настройки для локального запуска.
   ```bash
   # .env
   DB_PASSWORD=SuperSecretLocalPass
   JWT_SECRET=LocalDevelopmentKey123
   SPRING_PROFILES_ACTIVE=dev
   ```
---

## Запуск

1. Запустить контейнеры:

   ```bash
   docker compose up --build
   ```

4. Приложение:

   ```text
   http://localhost:8080
   ```

5. Открыть Swagger UI:

   ```text
   http://localhost:8080/swagger-ui.html
   ```
