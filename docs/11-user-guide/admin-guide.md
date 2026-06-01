# Руководство администратора

## 1. Подготовка окружения
- Java 17+
- PostgreSQL 12+
- Gradle 8+


## 2. Настройка БД
1. Создайте БД `sanepidcenter_db`.
2. Примените `database/postgresql/schema.sql`.
3. Укажите настройки подключения в `src/main/resources/application.properties`.


## 3. Конфигурация приложения
Отредактируйте `src/main/resources/application.properties`:

spring.datasource.url=jdbc:postgresql://localhost:5432/sanepidcenter_db
spring.datasource.username=postgres
spring.datasource.password=your_password


## 4. Проверка качества
```bash
gradle test jacocoTestReport
```
Отчёт покрытия: `build/reports/jacoco/test/html/index.html`.


## 5. Запуск в Docker
```bash
docker compose up --build
```

- Приложение: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API администратора: `/api/admin/*` (только `ROLE_ADMIN`)

---

## 6. Проверка работоспособности

Для быстрой проверки состояния системы используйте следующие команды и эндпоинты:

### 6.1 Проверка здоровья (Healthcheck)

Вызовите эндпоинт `GET`:
```bash
curl -s http://localhost:8080/actuator/health
```
### 6.2 Доступность документации (Swagger)

Откройте в браузере:
```text
http://localhost:8080/swagger-ui.html
```

Интерфейс должен загрузиться без ошибок.

### 6.3 Тест аутентификации (Login)

Проверьте выдачу JWT-токена через POST:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin", "password":"admin"}'
  ```
  
### 6.4 Доступ к Админке

Попробуйте зайти на защищенную страницу:

http://localhost:8080/admin/dashboard

Ожидаемое поведение: перенаправление на страницу входа (/login), если токен отсутствует.
---

### 6.5 Изменение роли

Изменение полномочий пользователя выполняется администратором через REST API. Доступно только для пользователей с ролью `ROLE_ADMIN`.

**Эндпоинт:**
```http
PUT /api/admin/users/{userId}
```

Пример:

```bash
curl -X PUT "http://localhost:8080/api/admin/users/15" \
  -H "Authorization: Bearer <admin_token>" \
  -H "Content-Type: application/json" \
  -d '{
    "role": "ROLE_ADMIN"
  }'
  ```
---

## 7. Панель администратора

Панель администратора — веб-интерфейс для централизованного управления пользователями, мониторинга системы и настройки параметров приложения. Доступ строго ограничен ролью `ROLE_ADMIN`.

### Доступ и навигация
- **URL:** `http://<host>:8080/admin/dashboard`
- **Авторизация:** Форма входа (`/login`) → установка JWT в `localStorage`/`cookie` → автоматический редирект на дашборд.
- **Защита маршрутов:** Фронтенд-гард + бэкенд-фильтр (`/api/admin/**` требует `ROLE_ADMIN`).

### Основные модули
| Модуль | Назначение | Связанные API |
|--------|------------|---------------|
| **Пользователи** | CRUD, блокировка/разблокировка, смена ролей | `GET/POST/PUT/PATCH/DELETE /api/admin/users` |
| **Метрики** | Статистика проверок, нарушений, активность за период | `GET /api/admin/metrics` |
| **Аудит** | Журнал действий администраторов (изменения, входы, ошибки) | `GET /api/admin/audit` |
| **Настройки** | Управление параметрами системы, сброс паролей, экспорт данных | `GET/PUT /api/admin/settings` |

### Интерфейс и UX
- **Структура:** Боковое меню навигации + основная рабочая область.
- **Таблицы:** Пагинация, сортировка по столбцам, быстрый поиск.
- **Формы:** Клиентская и серверная валидация, подсветка полей с ошибками.
- **Обратная связь:** Toast-уведомления об успехе/ошибке, спиннеры загрузки.

### Безопасность
- **RBAC:** Проверка токена и роли при каждом запросе к `/api/admin/**`.
- **Защита от атак:** Экранирование вывода, CSRF-токены для форм, CSP-заголовки.
- **Сессионный контроль:** Автоматический логаут при истечении JWT или изменении прав.
