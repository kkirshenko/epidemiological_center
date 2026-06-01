```plantuml
@startuml ComponentDiagram_EpidCenter
title Архитектурная компонентная диаграмма
skinparam componentStyle rectangle
skinparam shadowing false
skinparam packageStyle rectangle

rectangle "Клиентский слой" {
  [Веб-браузер] as Browser
  [Swagger UI] as Swagger
}

rectangle "Web Layer (Spring MVC)" {
  component "OrganizationController" as OrgCtrl
  component "InspectionController" as InspCtrl
  component "ViolationController" as ViolCtrl
  component "ProfileController" as ProfileCtrl
  component "AuthController" as AuthCtrl
}

rectangle "Service Layer (Business Logic)" {
  component "OrganizationService" as OrgSvc
  component "InspectionService" as InspSvc
  component "ViolationService" as ViolSvc
  component "ProfileService" as ProfileSvc
  component "JwtTokenProvider" as Jwt
}

rectangle "Security Layer" {
  component "SecurityConfig" as SecConfig
  component "JwtAuthenticationFilter" as JwtFilter
  component "CustomUserDetailsService" as UserDetailsService
}

rectangle "Data Access Layer" {
  component "OrganizationRepository" as OrgRepo
  component "InspectionRepository" as InspRepo
  component "ViolationRepository" as ViolRepo
  component "ProfileRepository" as ProfileRepo
}

database "PostgreSQL 15" as DB {
  folder "Сущности" {
    [organizations] as tbl_org
    [inspections] as tbl_insp
    [violations] as tbl_viol
    [profiles] as tbl_prof
    [organization_types] as tbl_org_type
    [inspection_types] as tbl_insp_type
  }
}

rectangle "Infrastructure" {
  component "DataInitializer" as Init
  component "OpenAPI Config" as OpenAPI
  component "WebSocket Config" as WebSocket
}

rectangle "Docker" {
  [PostgreSQL Container] as DockerDB
  [App Container] as DockerApp
}

' Потоки данных
Browser --> OrgCtrl : HTTP/HTTPS
Browser --> InspCtrl : REST API
Browser --> ViolCtrl : REST API
Browser --> AuthCtrl : /auth/login
Swagger --> OrgCtrl : API тестирование

' Контроллеры → Сервисы
OrgCtrl ..> OrgSvc : использует
InspCtrl ..> InspSvc : использует
ViolCtrl ..> ViolSvc : использует
ProfileCtrl ..> ProfileSvc : использует
AuthCtrl ..> ProfileSvc : аутентификация
AuthCtrl ..> Jwt : генерация токена

' Сервисы → Репозитории
OrgSvc ..> OrgRepo : CRUD
InspSvc ..> InspRepo : CRUD + поиск
ViolSvc ..> ViolRepo : CRUD + фильтрация
ProfileSvc ..> ProfileRepo : управление пользователями

' Безопасность
SecConfig ..> JwtFilter : фильтр запросов
JwtFilter ..> Jwt : валидация токена
UserDetailsService ..> ProfileRepo : загрузка пользователя

' Репозитории → БД
OrgRepo ..> tbl_org : JPA/Hibernate
InspRepo ..> tbl_insp : JPA/Hibernate
ViolRepo ..> tbl_viol : JPA/Hibernate
ProfileRepo ..> tbl_prof : JPA/Hibernate

' Связи между таблицами
tbl_org }|-- tbl_org_type : type_id
tbl_insp }|-- tbl_org : organization_id
tbl_insp }|-- tbl_insp_type : type_id
tbl_insp }|-- tbl_prof : inspector_id
tbl_viol }|-- tbl_insp : inspection_id

' Инициализация
Init ..> tbl_org_type : загрузка справочников
Init ..> tbl_insp_type : загрузка справочников

' Конфигурация
OpenAPI ..> OrgCtrl : документация эндпоинтов
WebSocket ..> InspCtrl : real-time уведомления

' Docker оркестрация
DockerApp ..> OrgCtrl : запуск приложения
DockerDB ..> tbl_org : хранение данных
DockerApp ..> DockerDB : JDBC соединение

' Легенда
note right of Jwt
  JWT-аутентификация:
  - ROLE_ADMIN
  - ROLE_INSPECTOR
  - ROLE_LABORANT
end note

note bottom of DB
  Нормализация: 3NF
  Индексы: name, risk_category,\ninspection_date, severity
end note

@enduml
```