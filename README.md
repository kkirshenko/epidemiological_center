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

```
sanepidcenter/
├── build.gradle                 # Конфигурация сборки Gradle
├── settings.gradle              # Настройки проекта
├── src/
│   ├── main/
│   │   ├── java/com/sanepidcenter/
│   │   │   ├── SanepidcenterApplication.java  # Главный класс приложения
│   │   │   ├── config/                        # Конфигурация
│   │   │   ├── controller/                    # Контроллеры (MVC)
│   │   │   ├── dto/                           # Data Transfer Objects
│   │   │   ├── model/                         # JPA Entity классы
│   │   │   ├── repository/                    # Spring Data репозитории
│   │   │   └── service/                       # Бизнес-логика
│   │   └── resources/
│   │       ├── application.properties         # Конфигурация приложения
│   │       ├── static/css/                    # CSS стили
│   │       ├── static/js/                     # JavaScript файлы
│   │       └── templates/                     # Thymeleaf шаблоны
│   └── test/java/                             # Тесты
└── database/postgresql/
    └── schema.sql                             # SQL схема базы данных
```

## Модели данных

### Profile (Профили пользователей)
- id (UUID)
- fullName (ФИО)
- role (Роль: ROLE_USER, ROLE_ADMIN)
- phone (Телефон)
- position (Должность)
- isActive (Активен ли)

### OrganizationType (Типы организаций)
- id (Integer)
- name (Название типа)
- description (Описание)

### Organization (Организации)
- id (UUID)
- name (Полное название)
- shortName (Краткое название)
- registrationNumber (Регистрационный номер)
- type (Тип организации)
- address (Адрес)
- city (Город)
- directorName (Руководитель)
- phone (Телефон)
- email (Email)
- employeeCount (Количество сотрудников)
- riskCategory (Категория риска: low, medium, high, critical)
- isActive (Активна ли)

### InspectionType (Типы проверок)
- id (Integer)
- name (Название)
- code (Код: PLAN, UNPLAN, REPEAT, RAID, MONITOR)
- description (Описание)

### Inspection (Проверки)
- id (UUID)
- organization (Организация)
- type (Тип проверки)
- inspector (Инспектор)
- scheduledDate (Плановая дата)
- startDate (Дата начала)
- endDate (Дата окончания)
- status (Статус: planned, in_progress, completed, cancelled)
- result (Результат: pending, satisfactory, unsatisfactory, critical)
- findingsSummary (Описание нарушений)
- recommendations (Рекомендации)
- actNumber (Номер акта)

### Violation (Нарушения)
- id (UUID)
- inspection (Проверка)
- code (Код нарушения)
- description (Описание)
- severity (Серьезность: minor, moderate, major, critical)
- articleReference (Ссылка на статью)
- correctionDeadline (Срок устранения)
- resolved (Устранено ли)

## API Endpoints

### Организации
- `GET /organizations` - Список всех организаций
- `GET /organizations/{id}` - Просмотр организации
- `GET /organizations/new` - Форма создания
- `POST /organizations` - Создание организации
- `GET /organizations/{id}/edit` - Форма редактирования
- `POST /organizations/{id}` - Обновление организации
- `POST /organizations/{id}/delete` - Удаление организации
- `GET /organizations/search?query=` - Поиск организаций

### Проверки
- `GET /inspections` - Список всех проверок
- `GET /inspections/{id}` - Просмотр проверки
- `GET /inspections/new` - Форма создания
- `POST /inspections` - Создание проверки
- `GET /inspections/{id}/edit` - Форма редактирования
- `POST /inspections/{id}` - Обновление проверки
- `POST /inspections/{id}/delete` - Удаление проверки
- `GET /inspections/planned` - Плановые проверки

### Администрирование (только ROLE_ADMIN)
- `GET /api/admin/users` - Список пользователей
- `GET /api/admin/users/{id}` - Получить пользователя
- `POST /api/admin/users` - Создать пользователя
- `PUT /api/admin/users/{id}` - Обновить пользователя
- `PATCH /api/admin/users/{id}/status?active=true|false` - Изменить активность
- `DELETE /api/admin/users/{id}` - Удалить пользователя
- `GET /api/admin/metrics` - Метрики админ-панели

> Доступ к `/api/admin/**` выполняется через JWT (`Authorization: Bearer <token>`).  
> Роль `admin` при логине/создании пользователя автоматически нормализуется в `ROLE_ADMIN`.
> Также поддерживается формат без префикса: `Authorization: <token>` (или `X-Auth-Token: <token>`).
> Все остальные API-эндпоинты (`/api/organizations`, `/api/inspections`, `/api/violations`) доступны без авторизации.

## Установка и запуск

### Требования
- Java 17+
- PostgreSQL 12+
- Gradle 8.x

### Настройка базы данных

1. Создайте базу данных:
```sql
CREATE DATABASE sanepidcenter_db;
```

2. Примените схему:
```bash
psql -U postgres -d sanepidcenter_db -f database/postgresql/schema.sql
```

### Конфигурация приложения

Отредактируйте `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/sanepidcenter_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### Сборка и запуск

```bash
# Сборка проекта
./gradlew build

# Запуск приложения
./gradlew bootRun

# Или запуск JAR файла
java -jar build/libs/sanepidcenter-1.0.0.jar
```

Приложение будет доступно по адресу: http://localhost:8080

## Тестирование

```bash
# Запуск тестов
./gradlew test
```

## Документация по требованиям

- Матрица соответствия требованиям МУ_КП: `docs/requirements-checklist.md`
- Руководство пользователя: `docs/user-guide.md`
- Руководство администратора: `docs/admin-guide.md`

## Docker запуск (со Swagger)

```bash
docker compose up --build
```

- Приложение: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Админ для входа (seed): `admin1 / admin1`

## Лицензия

MIT
