# Использование OpenAPI и Swagger

**Проект:** Информационная система Центра гигиены и эпидемиологии
**Библиотека:** SpringDoc OpenAPI 2.4.0  
**Дата:** 01.06.2026

---

## 1. Назначение

OpenAPI (Swagger) обеспечивает автоматическую генерацию интерактивной документации для REST API проекта.

**Основные цели:**
- **Документирование** — актуальное описание всех эндпоинтов, моделей данных и схем ответов
- **Тестирование** — возможность выполнения запросов прямо из браузера через Swagger UI
- **Интеграция** — упрощение разработки клиентских приложений за счёт чётких контрактов
- **Автогенерация** — документация обновляется автоматически при изменении кода контроллеров

---

## 2. Документация API

### Swagger UI (интерактивная документация)
**URL:** `http://localhost:8080/swagger-ui.html`

Интерактивный интерфейс для просмотра и тестирования REST API endpoints.

**Возможности:**
- Просмотр всех доступных эндпоинтов
- Тестирование API прямо из браузера
- Просмотр схем запросов/ответов
- Авторизация через JWT

---

### OpenAPI Specification (JSON)
**URL:** `http://localhost:8080/v3/api-docs`

Спецификация API в формате OpenAPI 3.0 (JSON).

**Использование:**
- Генерация клиентских SDK
- Интеграция с внешними системами
- Валидация API-контрактов
- Импорт в Postman/Insomnia

---

### Конфигурация (OpenApiConfig.java)

```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("СанЭпидЦентр API")
            .version("1.0")
            .description("Информационная система санитарно-эпидемиологического центра"))
        .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
        .components(new Components()
            .addSecuritySchemes("Bearer Authentication", 
                new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
}
```

### Зависимость (build.gradle)

```java
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0'
```

---

## 3. Основные адреса

| Назначение | URL |
| --- | --- |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/v3/api-docs` |

### Веб-интерфейс (Thymeleaf)
| Адрес | Назначение |
|---|---|
| `/` | Главная страница (Dashboard) |
| `/organizations` | Реестр поднадзорных организаций |
| `/inspections` | Журнал плановых и внеплановых проверок |
| `/violations` | Учёт и контроль устранения нарушений |
| `/users` | Управление учётными записями и ролями |
| `/login` | Форма аутентификации |
| `/admin` | Панель системного администратора |

### REST API (Base URL: `/api/v1`)
| Метод | Эндпоинт | Описание |
|---|---|---|
| `POST` | `/auth/login` | Аутентификация, возврат JWT-токена |
| `GET` | `/organizations` | Список организаций (пагинация, фильтрация) |
| `POST` | `/organizations` | Создание новой организации |
| `GET` | `/inspections` | Список проверок по статусу/дате |
| `POST` | `/inspections/{id}/violations` | Фиксация нарушения по проверке |
| `GET` | `/violations/overdue` | Список просроченных нарушений |
| `PUT` | `/violations/{id}/resolve` | Отметка об устранении нарушения |

### Инфраструктура и мониторинг
| Адрес | Назначение | Доступ |
|---|---|---|
| `/swagger-ui.html` | Интерактивная документация OpenAPI | Все |
| `/v3/api-docs` | Спецификация API в формате JSON | Все |
| `/actuator/health` | Статус работоспособности приложения и БД | Все |
| `/actuator/metrics` | Метрики JVM, HTTP-запросов, пула соединений | `ROLE_ADMIN` |
| `/actuator/env` | Активные свойства конфигурации | `ROLE_ADMIN` |
| `/ws` | WebSocket-канал для push-уведомлений | Аутентифицированные |
| `/static/**` | Статические ресурсы (CSS, JS, изображения) | Все |

> Веб-маршруты обслуживаются сервером через Thymeleaf. REST API требует валидный JWT в заголовке `Authorization: Bearer <token>`. Инфраструктурные эндпоинты защищены Spring Security согласно ролевой модели.

---

## 4. Доступность Swagger и OpenAPI

## Доступность Swagger и OpenAPI без авторизации

Интерфейс Swagger UI и спецификация OpenAPI настроены как **публичные ресурсы** и не требуют JWT-аутентификации. Это стандартная практика для инструментов документации, позволяющая разработчикам, тестировщикам и внешним интеграторам быстро изучать контракт API без предварительного получения токена.

### Конфигурация Spring Security

В классе `SecurityConfig` явно разрешён доступ к путям документации:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth

            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

            .requestMatchers("/api/auth/**").permitAll()

            .requestMatchers("/api/v1/**").authenticated()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

## 5. Как формируется спецификация

Спецификация OpenAPI в проекте `epidemiological_center` формируется автоматически с помощью библиотеки **springdoc-openapi** версии 2.4.0, которая интегрирована в приложение через зависимость в `build.gradle`:

```gradle
implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.4.0'
```
---

## 6. Группы API, которые отображаются в Swagger UI

В Swagger UI эндпоинты автоматически группируются по **тегам (tags)**, которые формируются на основе базового пути `@RequestMapping` контроллера. В проекте `epidemiological_center` используются следующие группы:

### Admin
**Administrative endpoints**

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| `GET` | `/api/admin/users/{id}` | Get user by id |
| `PUT` | `/api/admin/users/{id}` | Update user |
| `DELETE` | `/api/admin/users/{id}` | Delete user |
| `GET` | `/api/admin/users` | Get all users |
| `POST` | `/api/admin/users` | Create user |
| `PATCH` | `/api/admin/users/{id}/status` | Set active status for user |
| `GET` | `/api/admin/metrics` | Get admin dashboard metrics |

### Authentication
**API for user authentication and registration**

| Метод | Эндпоинт | Описание |
|-------|----------|----------|
| `POST` | `/api/auth/register` | Register new user |
| `POST` | `/api/auth/login` | Login user |
| `GET` | `/api/auth/me` | Get current user info |

### violation-api-controller

| Метод | Эндпоинт |
|-------|----------|
| `GET` | `/api/violations/{id}` |
| `PUT` | `/api/violations/{id}` |
| `DELETE` | `/api/violations/{id}` |
| `GET` | `/api/violations` |
| `POST` | `/api/violations` |

### organization-api-controller

| Метод | Эндпоинт |
|-------|----------|
| `GET` | `/api/organizations/{id}` |
| `PUT` | `/api/organizations/{id}` |
| `DELETE` | `/api/organizations/{id}` |
| `GET` | `/api/organizations` |
| `POST` | `/api/organizations` |
| `GET` | `/api/organizations/search` |

### inspection-api-controller

| Метод | Эндпоинт |
|-------|----------|
| `GET` | `/api/inspections/{id}` |
| `PUT` | `/api/inspections/{id}` |
| `DELETE` | `/api/inspections/{id}` |
| `GET` | `/api/inspections` |
| `POST` | `/api/inspections` |

Эндпоинты группы **Admin** требуют авторизации с ролью `ROLE_ADMIN`. Эндпоинты **Authentication** доступны без аутентификации. Остальные API требуют JWT-токен.

---

## 7. Работа с JWT в Swagger UI

### Авторизация в Swagger UI

Для работы с защищёнными эндпоинтами API необходимо получить JWT-токен и добавить его в Swagger UI.

#### Получение токена

1. **Регистрация пользователя** (если аккаунт ещё не создан):
   - Откройте эндпоинт `POST /api/auth/register`
   - Введите данные пользователя в формате JSON:
   ```json
   {
     "username": "admin",
     "email": "admin@example.com",
     "password": "password123",
     "role": "ROLE_ADMIN"
   }
```
2. **Аутентификация и получение токена**
   - Откройте эндпоинт POST /api/auth/login
   - Введите учётные данные:
   ```json
{
  "username": "admin",
  "password": "password123"
}
   ```
   
   В ответе получите токен:
   ```json
   {
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcxNzM5MjAwMCwiZXhwIjoxNzE3NDc4NDAwfQ...",
  "expiresIn": 86400
}
   ```   
   Затем токен добавляется в Swagger UI.
   
---

   #### Проверка авторизации
	После успешной авторизации:
   - Кнопка Authorize изменит цвет на зелёный
   - Рядом с защищёнными эндпоинтами исчезнет значок замка 🔒 (или изменит цвет)
   - Теперь можно выполнять запросы к защищённым эндпоинтам:
		GET /api/admin/users — получение списка всех пользователей
		POST /api/admin/users — создание нового пользователя
		GET /api/admin/metrics — получение метрик администратора
		POST /api/violations — создание нарушения
	И другие защищённые эндпоинты
 
---

## 8. Проверка доступности

Docker запуск (со Swagger)

docker compose up --build

Приложение: http://localhost:8080

Swagger UI: http://localhost:8080/swagger-ui.html

Админ для входа (seed): admin1 / admin1

