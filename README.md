# СанЭпидЦентр

**Автор:** Зволибовская Екатерина Валерьевна  
**Траектория:** Веб-разработка (Spring Boot + Thymeleaf)  
**Дата начала:** 01.03.2026  
**Дата сдачи:** 1.06.2026

## Описание проекта

Система управления санитарно-эпидемиологического центра — это веб-приложение для автоматизации процессов проверок и контроля организаций в сфере санитарно-эпидемиологического надзора.

Позволяет инспекторам проводить проверки организаций, фиксировать нарушения, формировать акты и контролировать их устранение.

## Траектория выполнения

- [ ] Десктоп
- [x] **Веб-разработка** (Spring Boot + Thymeleaf)
- [ ] Мобильная
- [ ] Enterprise

## Технологический стек

| Компонент       | Технология                        |
|-----------------|-----------------------------------|
| Бэкенд          | Java 17+, Spring Boot 3.2.x       |
| Фронтенд        | HTML, CSS, JavaScript, Thymeleaf  |
| API             | REST, OpenAPI (Swagger)           |
| База данных     | PostgreSQL 12+                    |
| ORM             | Spring Data JPA / Hibernate       |
| Безопасность    | JWT, BCrypt                       |
| Сборка          | Gradle 8.x                        |
| Контейнеризация | Docker                            |
| Инструменты     | Git, Postman                      |

## Требования к окружению

| Требование     | Версия   |
|----------------|----------|
| Java JDK       | 17+      |
| PostgreSQL     | 12+      |
| Gradle         | 8.x      |
| Docker         | 20+      |


## Установка и запуск

### 1. Клонирование репозитория

```bash
git clone https://github.com/kkirshenko/epidemiological_center.git
cd epidemiological_center
```

### 2. Настройка базы данных

Создайте базу данных:

```sql
CREATE DATABASE sanepidcenter_db;
```
Примените схему:

```bash
psql -U postgres -d sanepidcenter_db -f database/postgresql/schema.sql
```

### 3. Конфигурация приложения

Отредактируйте src/main/resources/application.properties:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sanepidcenter_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 4. Запуск backend

```bash
# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun

# Или запуск JAR файла
java -jar build/libs/sanepidcenter-1.0.0.jar
```

Сервер запустится на http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

### 5. Запуск через Docker

```bash
docker compose up --build
```

Приложение: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html
Тестовый админ: admin1 / admin1

## API Endpoints

**Базовый URL:** `http://localhost:8080/api`

### Организации

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| `GET` | `/organizations` | Список всех организаций | PUBLIC |
| `GET` | `/organizations/{id}` | Просмотр организации | PUBLIC |
| `GET` | `/organizations/new` | Форма создания | PUBLIC |
| `POST` | `/organizations` | Создание организации | PUBLIC |
| `GET` | `/organizations/{id}/edit` | Форма редактирования | PUBLIC |
| `POST` | `/organizations/{id}` | Обновление организации | PUBLIC |
| `POST` | `/organizations/{id}/delete` | Удаление организации | PUBLIC |
| `GET` | `/organizations/search` | Поиск организаций | PUBLIC |

### Проверки

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| `GET` | `/inspections` | Список всех проверок | PUBLIC |
| `GET` | `/inspections/{id}` | Просмотр проверки | PUBLIC |
| `GET` | `/inspections/new` | Форма создания | PUBLIC |
| `POST` | `/inspections` | Создание проверки | PUBLIC |
| `GET` | `/inspections/{id}/edit` | Форма редактирования | PUBLIC |
| `POST` | `/inspections/{id}` | Обновление проверки | PUBLIC |
| `POST` | `/inspections/{id}/delete` | Удаление проверки | PUBLIC |
| `GET` | `/inspections/planned` | Плановые проверки | PUBLIC |

### Администрирование (только ROLE_ADMIN)

| Метод | Эндпоинт | Описание | Доступ |
|-------|----------|----------|--------|
| `GET` | `/api/admin/users` | Список пользователей | ADMIN (JWT) |
| `GET` | `/api/admin/users/{id}` | Получить пользователя | ADMIN (JWT) |
| `POST` | `/api/admin/users` | Создать пользователя | ADMIN (JWT) |
| `PUT` | `/api/admin/users/{id}` | Обновить пользователя | ADMIN (JWT) |
| `PATCH` | `/api/admin/users/{id}/status` | Изменить активность | ADMIN (JWT) |
| `DELETE` | `/api/admin/users/{id}` | Удалить пользователя | ADMIN (JWT) |
| `GET` | `/api/admin/metrics` | Метрики админ-панели | ADMIN (JWT) |

Доступ к `/api/admin/**` выполняется через JWT.

## Структура документации

Вся документация находится в папке `docs/`:

- `docs/00-project-charter/` — Паспорт проекта, IDEF0, BUC, SWOT, ROI
- `docs/01-requirements/` — Use Case, Domain Model, трассировка
- `docs/02-architecture/` — PCMEF, ADR, интерфейсы
- `docs/03-database/` — ER-диаграмма, DDL, ORM
- `docs/04-detailed-design/` — Sequence диаграммы, спецификация методов
- `docs/05-implementation/` — Реализация слоёв
- `docs/06-testing/` — Тест-планы, JaCoCo, Postman
- `docs/07-refactoring/` — «Запахи кода», Data Mapper, Identity Map
- `docs/08-ui/` — Скриншоты интерфейсов
- `docs/09-api/` — OpenAPI, Swagger
- `docs/10-deployment/` — Docker, CI/CD, администрирование
- `docs/11-user-guide/` — Руководство пользователя
- `docs/12-final-report/` — Пояснительная записка, презентация

## Архитектура (PCMEF)

Система построена на архитектурном паттерне **PCMEF** (Presentation-Control-Mediator-Entity-Foundation).

Распределение слоёв:

| Слой | Расположение | Ответственность |
|------|--------------|-----------------|
| Presentation (P) | Thymeleaf (браузер) | UI, отображение, ввод данных |
| Control (C) | Spring Boot | REST API, валидация DTO |
| Mediator (M) | Spring Boot | Бизнес-логика, транзакции |
| Entity (E) | Spring Boot | JPA-сущности |
| Foundation (F) | Spring Boot | Репозитории, доступ к БД |

### Ключевые ADR

- ADR-001: Выбор архитектурного паттерна
- ADR-002: Выбор базы данных и ORM
- ADR-003: Стратегия аутентификации

## Модели данных

В системе используются следующие сущности для хранения и обработки информации:

### Profile (Профили пользователей)
| Поле | Тип / Описание |
|------|----------------|
| `id` | UUID (уникальный идентификатор) |
| `fullName` | Строка (ФИО сотрудника) |
| `role` | Enum (`ROLE_USER`, `ROLE_ADMIN`) |
| `phone` | Строка (контактный телефон) |
| `position` | Строка (должность) |
| `isActive` | Boolean (статус активности учётной записи) |

### OrganizationType (Типы организаций)
| Поле | Тип / Описание |
|------|----------------|
| `id` | Integer (уникальный идентификатор) |
| `name` | Строка (название типа) |
| `description` | Текст (описание типа организации) |

### Organization (Организации)
| Поле | Тип / Описание |
|------|----------------|
| `id` | UUID |
| `name` | Строка (полное юридическое название) |
| `shortName` | Строка (краткое название) |
| `registrationNumber` | Строка (регистрационный номер/ИНН) |
| `type` | Ссылка на `OrganizationType` |
| `address` | Строка (юридический/фактический адрес) |
| `city` | Строка (город расположения) |
| `directorName` | Строка (ФИО руководителя) |
| `phone` | Строка (контактный телефон) |
| `email` | Строка (электронная почта) |
| `employeeCount` | Integer (количество сотрудников) |
| `riskCategory` | Enum (`low`, `medium`, `high`, `critical`) |
| `isActive` | Boolean (статус организации) |

### InspectionType (Типы проверок)
| Поле | Тип / Описание |
|------|----------------|
| `id` | Integer |
| `name` | Строка (название типа проверки) |
| `code` | Enum (`PLAN`, `UNPLAN`, `REPEAT`, `RAID`, `MONITOR`) |
| `description` | Текст (описание процедуры) |

### Inspection (Проверки)
| Поле | Тип / Описание |
|------|----------------|
| `id` | UUID |
| `organization` | Ссылка на `Organization` |
| `type` | Ссылка на `InspectionType` |
| `inspector` | Ссылка на `Profile` (ответственный инспектор) |
| `scheduledDate` | Date (плановая дата проведения) |
| `startDate` | Date (фактическая дата начала) |
| `endDate` | Date (фактическая дата завершения) |
| `status` | Enum (`planned`, `in_progress`, `completed`, `cancelled`) |
| `result` | Enum (`pending`, `satisfactory`, `unsatisfactory`, `critical`) |
| `findingsSummary` | Текст (краткое описание выявленных нарушений) |
| `recommendations` | Текст (рекомендации по устранению) |
| `actNumber` | Строка (номер составленного акта) |

### Violation (Нарушения)
| Поле | Тип / Описание |
|------|----------------|
| `id` | UUID |
| `inspection` | Ссылка на `Inspection` |
| `code` | Строка/Enum (код нарушения по классификатору) |
| `description` | Текст (детальное описание отклонения) |
| `severity` | Enum (`minor`, `moderate`, `major`, `critical`) |
| `articleReference` | Строка (ссылка на нормативный акт/статью) |
| `correctionDeadline` | Date (предельный срок устранения) |
| `resolved` | Boolean (факт устранения нарушения) |


### Backend

Модульная структура бэкенда проекта `epidemiological_center`:

| Модуль | Назначение | Ключевые компоненты |
|--------|------------|---------------------|
| `account` | Управление пользователями: регистрация, аутентификация, профиль, смена пароля | `Profile`, `ProfileRepository`, `ProfileService`, `ProfileController`, `AuthRequest/Response DTO` |
| `security` | JWT-аутентификация, фильтрация запросов, настройки безопасности, OpenAPI security scheme | `JwtTokenProvider`, `JwtAuthenticationFilter`, `SecurityConfig`, `OpenApiConfig`, `Role` enum |
| `organization` | CRUD организаций: создание, редактирование, поиск, фильтрация по типу и риску | `Organization`, `OrganizationType`, `OrganizationRepository`, `OrganizationService`, `OrganizationController` |
| `inspection` | Управление проверками: планирование, проведение, фиксация результатов, статусы | `Inspection`, `InspectionType`, `InspectionRepository`, `InspectionService`, `InspectionController` |
| `violation` | Учёт нарушений: привязка к проверке, классификация по серьёзности, сроки устранения | `Violation`, `ViolationRepository`, `ViolationService`, `ViolationController`, `Severity` enum |
| `admin` | Административные эндпоинты: управление пользователями, метрики системы, аудит действий | `AdminUserController`, `AdminMetricsService`, `AuditLog`, `@PreAuthorize("hasRole('ADMIN')")` |
| `common` | Общие утилиты: валидаторы, обработчики исключений, пагинация, маппинг DTO↔Entity | `GlobalExceptionHandler`, `MapperUtils`, `PageableResponse`, `ValidationGroups` |

## Тестирование

## Запуск JUnit-тестов

Из корня проекта:

```bash
gradle test
```

Результаты тестов:
- консольный вывод Gradle;
- HTML-отчёт: `build/reports/tests/test/index.html`.

---

## JaCoCo: отчёт покрытия и контроль порога 40%

В проекте настроены:
- генерация XML/HTML отчётов JaCoCo;
- порог покрытия **минимум 40%** (instruction covered ratio);
- проверка порога включена в `check`.

Запуск:

```bash
gradle clean test jacocoTestReport jacocoTestCoverageVerification
```

Где смотреть отчёт покрытия:
- HTML: `build/reports/jacoco/test/html/index.html`
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`

## Полезные ссылки

- [Документация проекта](docs/)

