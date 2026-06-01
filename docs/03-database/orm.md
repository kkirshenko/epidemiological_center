# ORM в проекте

**Проект:** Информационная система Центра гигиены и эпидемиологии
**Репозиторий:** https://github.com/kkirshenko/epidemiological_center  
**Технология ORM:** Spring Data JPA (Hibernate 6.x)  
**СУБД:** PostgreSQL 15  
**Дата актуализации:** 01.06.2026

---

## Используемые технологии

### Основные компоненты ORM-стека

| Компонент | Версия | Назначение |
|-----------|--------|------------|
| **Spring Data JPA** | 3.2.x | Абстракция доступа к данным, репозитории, пагинация |
| **Hibernate ORM** | 6.4.x | Реализация JPA, маппинг сущностей, кэширование, управление сессиями |
| **PostgreSQL JDBC Driver** | 42.6.x | Низкоуровневый драйвер для подключения к СУБД |
| **HikariCP** | 5.1.x | Пул соединений для высокой производительности |
| **Flyway / schema.sql** | — | Миграции схемы базы данных |
| **Lombok** | 1.18.30 | Генерация бойлерплейт-кода (`@Data`, `@Builder`, `@NoArgsConstructor`) |

### Зависимости в `build.gradle`

```groovy
dependencies {
    // Spring Data JPA (включает Hibernate)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // PostgreSQL драйвер
    runtimeOnly 'org.postgresql:postgresql'
    
    // Пул соединений (по умолчанию в Spring Boot)
    implementation 'com.zaxxer:HikariCP'
    
    // Lombok для уменьшения шаблонного кода
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Валидация данных
    implementation 'org.springframework.boot:spring-boot-starter-validation'
}
```

---

## Конфигурация ORM

Основные настройки:

### Подключение к базе данных
spring.datasource.url=jdbc:postgresql://localhost:5432/sanepidcenter
spring.datasource.username=sanepid
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

### Настройки пула соединений (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000

### JPA / Hibernate настройки
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

### Оптимизация для PostgreSQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

### Логирование (для разработки)
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

---

## Сущности JPA

### Основные сущности

| Сущность | Таблица | Назначение | Ключевые поля |
|----------|---------|------------|--------------|
| **Profile** | `profiles` | Учётные записи пользователей | `id`, `username`, `role`, `isActive` |
| **Organization** | `organizations` | Реестр поднадзорных организаций | `id`, `name`, `riskCategory`, `isActive` |
| **Inspection** | `inspections` | Журнал проверок | `id`, `organizationId`, `inspectorId`, `status`, `scheduledDate` |
| **Violation** | `violations` | Выявленные нарушения | `id`, `inspectionId`, `severity`, `resolved`, `correctionDeadline` |

### Ключевые аннотации

```java
@Entity                    // Маркер JPA-сущности
@Table(name = "...")       // Имя таблицы в БД
@Id                        // Первичный ключ
@GeneratedValue(strategy = GenerationType.UUID)  // Автогенерация UUID
@Column(nullable = false)  // Ограничения колонки
@Enumerated(EnumType.STRING)  // Хранение enum как строки
@ManyToOne / @OneToMany  // Связи между сущностями
@JoinColumn(name = "...")  // Внешний ключ
@PrePersist / @PreUpdate // Колбэки для аудита (created_at, updated_at)
```

---

## Особенности маппинга

- UUID вместо Long — глобальная уникальность идентификаторов
- FetchType.LAZY — отложенная загрузка связанных сущностей (оптимизация)
- CascadeType.ALL — каскадное сохранение/удаление (например, нарушения при удалении проверки)
- EnumType.STRING — хранение перечислений как читаемых строк, а не чисел
