# Реализация слоёв проекта

**Проект:** Информационная система Центра гигиены и эпидемиологии 
**Репозиторий:** https://github.com/kkirshenko/epidemiological_center  
**Архитектура:** PCMEF (Presentation, Control, Mediator, Entity, Foundation)  
**Стек:** Java 17, Spring Boot 3.2.x, PostgreSQL 15, Thymeleaf  
**Дата:** 01.06.2026

---

## 1. Общая схема слоёв

# Архитектурная схема системы (Поток управления и данные)

## 1. Презентационный слой (Presentation — P)
**Thymeleaf Templates + HTML/CSS/JS**
- `main.html`, `inspection-form.html`
- Валидация форм, условный рендеринг
- Пользовательские события (клики, отправка данных)

⬇ *HTTP-запросы / пользовательские события*

## 2. Слой управления (Control — C)
**@Controller / @RestController**
- `OrganizationController`
- `InspectionController`
- `ViolationController`
- `ProfileController`

⬇ *Делегирование бизнес-логики*

## 3. Слой посредника / бизнес-логики (Mediator — M)
**@Service**
- `OrganizationService`
- `InspectionService`
- `ViolationService`
- `ProfileService`

⬇ *Манипуляция сущностями*

## 4. Слой сущностей (Entity — E)
**@Entity JPA-классы (доменная модель)**
- `Organization`
- `Inspection`
- `Violation`
- `Profile`

⬇ *Доступ к данным через JPA*

## 5. Фундаментальный слой (Foundation — F)
**JpaRepository + утилиты**
- `OrganizationRepository`
- `InspectionRepository`
- `ViolationRepository`
- `ProfileRepository`

⬇ *JDBC / Hibernate*

## 6. База данных
**PostgreSQL 15**
- **Схема:** `sanepidcenter`
- **Таблицы:** `organizations`, `inspections`, `violations`, `profiles` + справочники

---

## Условные обозначения потока
| Символ | Значение |
|--------|-----------|
| ⬇ | Переход управления / вызов нижележащего слоя |
| ⬆ | Возврат результата (данные, View, ResponseEntity) |

## Краткое описание взаимодействия
1. **Пользователь** → взаимодействует с **Thymeleaf-шаблонами**.
2. **Контроллер** → обрабатывает запрос, вызывает **Сервис**.
3. **Сервис** → реализует бизнес-логику, работает с **Репозиторием** через **JPA-сущности**.
4. **Репозиторий** → выполняет запросы к **PostgreSQL**.
5. **Ответ** → проходит обратный путь: данные → сущность → сервис → контроллер → View.
---


### Принципы взаимодействия слоёв

| Принцип | Реализация в проекте |
|---------|---------------------|
| **Инверсия зависимостей (DIP)** | Контроллеры зависят от интерфейсов сервисов, сервисы — от интерфейсов репозиториев |
| **Единая ответственность (SRP)** | Каждый класс решает одну задачу: Controller — HTTP, Service — бизнес-логика, Repository — данные |
| **Открытость/закрытость (OCP)** | Новые функции добавляются через новые сервисы/контроллеры без изменения существующих |
| **Подстановка Лисков (LSP)** | Все реализации репозиториев наследуют JpaRepository и могут быть заменены |
| **Разделение интерфейсов (ISP)** | Специализированные репозитории вместо единого «универсального» интерфейса |

### Технологии на каждом слое

| Слой | Технологии | Назначение |
|------|-----------|------------|
| **Presentation** | Thymeleaf, HTML5, CSS3, JavaScript | Рендеринг UI, валидация форм, адаптивность |
| **Control** | Spring MVC, @Valid, Model, RedirectView | Обработка HTTP, маппинг DTO↔Entity, возврат ответов |
| **Mediator** | @Service, @Transactional, бизнес-правила | Оркестрация процессов, валидация, транзакции |
| **Entity** | JPA @Entity, @Column, @OneToMany, Lombok | Модель данных, аннотации маппинга, бизнес-методы сущностей |
| **Foundation** | JpaRepository, @Query, JOIN FETCH, HikariCP | Абстракция CRUD, оптимизация запросов, пул соединений |

---

## 2. Серверная часть

### 2.1. Общая архитектура

Серверная часть реализована на **Spring Boot 3.2.x** с использованием архитектурного паттерна **PCMEF** (Presentation, Control, Mediator, Entity, Foundation).

**Структура пакетов:**
src/main/java/com/sanepidcenter/
├── SanEpidCenterApplication.java # Точка входа Spring Boot
├── config/ # Конфигурация безопасности и API
├── controller/ # Control Layer (C)
├── service/ # Mediator Layer (M)
├── repository/ # Foundation Layer (F)
├── model/ # Entity Layer (E)
├── dto/ # Data Transfer Objects
├── security/ # JWT фильтрация и аутентификация
└── exception/ # Глобальная обработка ошибок


---

### Ключевые компоненты

**Control Layer**
- `OrganizationController`, `InspectionController`, `ViolationController`, `ProfileController`
- Обработка REST-запросов, маппинг DTO↔Entity, возврат HTTP-статусов

**Mediator Layer**
- `OrganizationService`, `InspectionService`, `ViolationService`, `ProfileService`
- Бизнес-правила: расчёт рисков, валидация статусов, управление сроками

**Foundation Layer**
- Репозитории с Derived Query Methods и `@Query` для сложных выборок
- Оптимизация через `JOIN FETCH` (решение N+1), индексы в БД

---

### Конфигурация (application.properties)

```properties
# DataSource
spring.datasource.url=jdbc:postgresql://localhost:5432/sanepidcenter
spring.datasource.hikari.maximum-pool-size=10

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Security
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration=86400000
```

---

## 3. Основные потоки данных

### Обзор потоков

Система следует строгой иерархии PCMEF: данные перемещаются вертикально от клиента к базе данных и обратно.

Клиент → Control → Mediator → Foundation → PostgreSQL
↑ ↑ ↑ ↑
DTO/JSON Бизнес- CRUD/ SQL/
Thymeleaf логика JPQL JDBC
---

### Ключевые сценарии

#### 3.1. Аутентификация пользователя

[POST /auth/login] {username, password}
↓
AuthController → ProfileService.authenticate()
↓
ProfileService → ProfileRepository.findByUsername()
↓
ProfileRepository → PostgreSQL: SELECT * FROM profiles WHERE username = ?
↓
PostgreSQL → BCrypt.compare(password, hash)
↓
ProfileService → JwtTokenProvider.generateToken(profile)
↓
AuthController → {token, expiresIn, user}


**Формат данных:**
- Вход: `AuthRequestDto {username: String, password: String}`
- Выход: `AuthResponseDto {token: String, expiresIn: Long, user: ProfileSummaryDto}`

---

#### 3.2. Создание проверки
[GET /inspections/new] → InspectionController.newInspectionForm()
↓
InspectionController → OrganizationService.getActiveOrganizations()
↓
OrganizationService → OrganizationRepository.findByIsActiveTrue()
↓
PostgreSQL → список организаций → форма создания (Thymeleaf)
↓
[POST /inspections] {organizationId, type, scheduledDate, ...}
↓
InspectionController → @Valid → InspectionService.createInspection()
↓
InspectionService → валидация + нормализация статусов + attachReferences()
↓
InspectionService → InspectionRepository.save(inspection)
↓
PostgreSQL → INSERT INTO inspections (...) RETURNING id
↓
InspectionController → redirect:/inspections/{id}

**Формат данных:**
- Вход: `InspectionCreateDto {organizationId: UUID, type: InspectionType, scheduledDate: LocalDate, notes: String}`
- Выход: `InspectionDto {id: UUID, organization: OrgSummary, status: String, scheduledDate: LocalDate, ...}`

---

#### 3.3. Добавление нарушения
[POST /inspections/{id}/violations] {code, severity, deadline, ...}
↓
ViolationController → @Valid → ViolationService.createViolation()
↓
ViolationService → InspectionRepository.findById(id) [проверка существования]
↓
ViolationService → валидация: deadline ≥ violationDate, severity ∈ [MINOR..CRITICAL]
↓
ViolationService → ViolationRepository.save(violation)
↓
PostgreSQL → INSERT INTO violations (...)
↓
ViolationController → redirect:/inspections/{id}

**Формат данных:**
- Вход: `ViolationCreateDto {code: String, severity: ViolationSeverity, deadline: LocalDate, description: String}`
- Выход: `ViolationDto {id: UUID, code: String, severity: String, resolved: Boolean, ...}`

---

## Правила зависимостей между слоями

| Правило | Описание |
|---------|----------|
| **Строгая иерархия** | Зависимости идут только сверху вниз: `Presentation → Control → Mediator → Entity/Foundation`. Обратные и циклические связи запрещены. |
| **Инверсия зависимостей (DIP)** | Верхние слои зависят от интерфейсов, а не от реализаций. Сервисы инжектят `Repository`-интерфейсы, контроллеры — `Service`-интерфейсы. |
| **Изоляция Foundation** | Репозитории и инфраструктурные утилиты не знают о бизнес-логике. Они работают только с `@Entity` и стандартными API (JPA/JDBC). |
| **DTO как контракт** | Данные между слоями передаются через DTO, а не напрямую через JPA-сущности. Это предотвращает утечку ORM-деталей в API и UI. |
| **Транзакционная граница** | `@Transactional` применяется только на уровне Mediator (сервисы). Контроллеры и репозитории не управляют транзакциями. |
| **Запрет сквозного доступа** | Контроллеры не вызывают репозитории напрямую. Вся бизнес-валидация и оркестрация инкапсулирована в сервисах. |

---

## Соответствие модулей слоям (PCMEF)

| Слой PCMEF | Пакет проекта | Назначение | Ключевые компоненты |
|------------|---------------|------------|---------------------|
| **Control (C)** | `com.sanepidcenter.controller` | Обработка HTTP-запросов, валидация входных данных, маппинг DTO↔Entity, возврат ответов | `OrganizationController`, `InspectionController`, `ViolationController`, `AuthController` |
| **Mediator (M)** | `com.sanepidcenter.service` | Бизнес-логика, оркестрация процессов, управление транзакциями, валидация правил предметной области | `OrganizationService`, `InspectionService`, `ViolationService`, `ProfileService`, `JwtTokenProvider` |
| **Entity (E)** | `com.sanepidcenter.model` | Доменная модель, отображение на таблицы БД, бизнес-методы сущностей, JPA-аннотации | `Organization`, `Inspection`, `Violation`, `Profile`, enums (`Role`, `RiskCategory`, `InspectionStatus`) |
| **Foundation (F)** | `com.sanepidcenter.repository`<br>`com.sanepidcenter.config` | Абстракция доступа к данным, CRUD-операции, кастомные запросы, инфраструктурная настройка | `OrganizationRepository`, `InspectionRepository`, `SecurityConfig`, `OpenApiConfig`, `DataInit` |
| **Cross-cutting** | `com.sanepidcenter.dto`<br>`com.sanepidcenter.security`<br>`com.sanepidcenter.exception` | Сквозные механизмы: передача данных, аутентификация, глобальная обработка ошибок | `InspectionDto`, `JwtAuthenticationFilter`, `GlobalExceptionHandler`, `ErrorResponse` |

---
