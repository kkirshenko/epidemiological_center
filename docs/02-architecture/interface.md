# Интерфейсы проекта

**Проект:** Информационная система Центра гигиены и эпидемиологии  
**Архитектура:** PCMEF (Presentation, Control, Mediator, Entity, Foundation)  
**Репозиторий:** https://github.com/kkirshenko/epidemiological_center  
**Дата актуализации:** 01.06.2026

---

## Обзор архитектуры интерфейсов

Система следует принципу **инверсии зависимостей (DIP)**: верхние слои зависят от абстракций (интерфейсов), а не от конкретных реализаций.

---

┌─────────────────┐
│ Presentation │ ← Thymeleaf, JavaScript
└────────┬────────┘
│ использует интерфейсы
▼
┌─────────────────┐
│ Control │ ← @Controller, @RestController
└────────┬────────┘
│ делегирует через интерфейсы
▼
┌─────────────────┐
│ Mediator │ ← @Service (бизнес-логика)
└────────┬────────┘
│ вызывает через интерфейсы
▼
┌─────────────────┐
│ Foundation │ ← JpaRepository, утилиты
└─────────────────┘


**Преимущества такого подхода:**
- Легко тестировать каждый слой изолированно (Mockito)
- Можно менять реализацию, не затрагивая зависимые модули
- Чёткие контракты между компонентами

---

## Control → Mediator (Service Interfaces)

### 1. InspectionService
Отвечает за управление жизненным циклом проверок, валидация статусов, расчёт дат.

```java
public interface InspectionService {
    
    Optional<Inspection> getInspectionById(UUID id);
    List<Inspection> getAllInspections();
    List<Inspection> getInspectionsByOrganizationId(UUID orgId);
    List<Inspection> getInspectionsByStatus(String status);
    List<Inspection> getPlannedInspections();
    
    Inspection createInspection(Inspection inspection);
    
    Inspection updateInspection(UUID id, Inspection updatedInspection);
    
    void deleteInspection(UUID id);

    void startInspection(UUID id);
    void completeInspection(UUID id, String result, String findings);
    void cancelInspection(UUID id, String reason);
}
```

### 2. ViolationService
Учёт нарушений, контроль сроков устранения, фильтрация по тяжести.

```java
public interface ViolationService {
    
    // Чтение
    Optional<Violation> getViolationById(UUID id);
    List<Violation> getViolationsByInspectionId(UUID inspectionId);
    List<Violation> getUnresolvedViolations();
    List<Violation> searchViolations(String query);
    
    Violation createViolation(Violation violation);
    
    Violation updateViolation(UUID id, Violation updatedViolation);
    
    void deleteViolation(UUID id);
    
    void resolveViolation(UUID id, String resolutionNotes);
    boolean isOverdue(UUID id);
    List<Violation> getOverdueViolations();
}
```

### 3. OrganizationService
Управление реестром организаций, расчёт категорий риска.

```java
public interface OrganizationService {
    
    // Чтение
    Optional<Organization> getOrganizationById(UUID id);
    List<Organization> getAllOrganizations();
    List<Organization> getActiveOrganizations();
    List<Organization> searchOrganizations(String query);
    List<Organization> getOrganizationsByRiskCategory(String category);
    
    Organization createOrganization(Organization organization);
    
    Organization updateOrganization(UUID id, Organization updatedOrganization);
    
    void deactivateOrganization(UUID id);
    
    RiskCategory calculateRiskCategory(Organization organization);
    List<Inspection> getOrganizationInspectionHistory(UUID orgId);
}
```

### 4. ProfileService
Управление пользователями, аутентификация, ролевая модель.
```java
public interface ProfileService {
    
    Optional<Profile> authenticate(String username, String password);
    Profile getCurrentUser();
    
    Optional<Profile> getProfileById(UUID id);
    List<Profile> getAllProfiles();
    List<Profile> getProfilesByRole(String role);
    
    Profile createProfile(Profile profile, String rawPassword);
    Profile updateProfile(UUID id, Profile updatedProfile);
    void deactivateProfile(UUID id);
    
    boolean hasRole(UUID profileId, String role);
    void assignRole(UUID profileId, String role);
}
```

## Mediator → Foundation (Repository Interfaces)

### 1. InspectionRepository

```java
public interface InspectionRepository extends JpaRepository<Inspection, UUID> {
    
    // Стандартные методы JpaRepository:
    // save(), findById(), findAll(), deleteById(), existsById()
    
    // Кастомные запросы
    List<Inspection> findByOrganizationId(UUID orgId);
    List<Inspection> findByInspectorId(UUID inspectorId);
    List<Inspection> findByStatusOrderByScheduledDateDesc(String status);
    List<Inspection> findByScheduledDateBetween(LocalDate start, LocalDate end);
    
    @Query("SELECT i FROM Inspection i WHERE i.result = :result")
    List<Inspection> findByResult(@Param("result") InspectionResult result);
    
    @Query("SELECT i FROM Inspection i JOIN FETCH i.violations WHERE i.id = :id")
    Optional<Inspection> findByIdWithViolations(@Param("id") UUID id);
    
    @Query("SELECT i FROM Inspection i JOIN FETCH i.organization WHERE i.status = :status")
    List<Inspection> findByStatusWithOrganization(@Param("status") String status);
}
```

### 2. ViolationRepository

```java
public interface ViolationRepository extends JpaRepository<Violation, UUID> {
    
    List<Violation> findByInspectionId(UUID inspectionId);
    List<Violation> findByResolvedFalse();
    List<Violation> findBySeverity(ViolationSeverity severity);
    
    @Query("SELECT v FROM Violation v WHERE v.correctionDeadline < :today AND v.resolved = false")
    List<Violation> findOverdueViolations(@Param("today") LocalDate today);
    
    @Query("SELECT v FROM Violation v JOIN FETCH v.inspection WHERE v.id = :id")
    Optional<Violation> findByIdWithInspection(@Param("id") UUID id);
}
```

### 3. OrganizationRepository
```java
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
    
    List<Organization> findByIsActiveTrue();
    List<Organization> findByRiskCategory(String category);
    
    @Query("SELECT o FROM Organization o WHERE LOWER(o.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Organization> searchByName(@Param("query") String query);
    
    @Query("SELECT o FROM Organization o JOIN FETCH o.inspections WHERE o.id = :id")
    Optional<Organization> findByIdWithInspections(@Param("id") UUID id);
}
```

### 4. ProfileRepository

```java
public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    
    Optional<Profile> findByUsername(String username);
    List<Profile> findByRole(String role);
    List<Profile> findByIsActiveTrue();
    
    boolean existsByUsername(String username);
}
```
---

### Ключевые архитектурные решения:

1. **Чёткое разделение слоёв**  
   Интерфейсы `*Service` (Mediator) и `*Repository` (Foundation) образуют стабильные контракты между слоями. Контроллеры (Control) зависят только от абстракций, что позволяет:
   - Изолированно тестировать бизнес-логику через Mockito
   - Заменять реализацию репозиториев без изменения сервисов
   - Легко масштабировать отдельные модули системы

2. **Единый стиль проектирования**  
   Все сервисы следуют согласованному шаблону:
   - Методы чтения: `getById()`, `getAll()`, `findBy*()`
   - Методы изменения: `create()`, `update()`, `delete()`
   - Бизнес-операции: предметно-ориентированные методы (`startInspection()`, `resolveViolation()`, `calculateRiskCategory()`)

3. **Оптимизация доступа к данным**  
   Репозитории используют:
   - Derived Query Methods для простых выборок
   - JPQL с `@Query` для сложной логики
   - `JOIN FETCH` для предотвращения проблемы N+1
   - Параметризованные запросы для защиты от SQL-инъекций

4. **Типизация и безопасность**  
   - Использование `UUID` вместо `Long` для глобальной уникальности идентификаторов
   - Возврат `Optional<T>` для явной обработки отсутствующих данных
   - Энумы (`InspectionResult`, `ViolationSeverity`, `RiskCategory`) вместо строковых констант

5. **Поддержка бизнес-правил**  
   Предметная логика инкапсулирована в сервисах:
   - Валидация статусов и переходов между ними
   - Расчёт категорий риска на основе атрибутов организации
   - Контроль сроков устранения нарушений (`isOverdue()`)

### Соответствие требованиям качества:

| Атрибут | Реализация в интерфейсах |
|---------|-------------------------|
| **Тестируемость** | Интерфейсы позволяют мокировать зависимости; 68 тестов покрывают ключевые сценарии |
| **Поддерживаемость** | Единый стиль именования, разделение по доменным агрегатам, минимальная связность |
| **Расширяемость** | Новые методы добавляются без изменения существующих контрактов (Open/Closed Principle) |
| **Безопасность** | Параметризованные запросы, валидация на уровне сервисов, ролевая модель в `ProfileService` |
