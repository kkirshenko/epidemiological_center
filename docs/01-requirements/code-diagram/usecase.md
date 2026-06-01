```PlantUML
@startuml
title Диаграмма прецедентов (Use Case) системы учета проверок ЦГиЭ
left to right direction
skinparam packageStyle rectangle

actor "Проверяющий специалист" as Inspector
actor "Лаборант" as LabTechnician

rectangle "Информационная система учета проверок" {
    usecase "Внести данные о\nвыездной проверке" as UC1
    usecase "Установить первичный\nстатус проверки" as UC2
    usecase "Просмотреть реестр\nпроверок" as UC3
    usecase "Сформировать и\nраспечатать Отчет" as UC4
    usecase "Внести запись о\nнарушениях" as UC5
    usecase "Сформировать отчет о\nнарушениях" as UC6
    usecase "Зафиксировать\nрезультаты анализа" as UC7
    usecase "Инициировать\nповторную проверку" as UC8
}

' Связи актеров с прецедентами
Inspector --> UC1
Inspector --> UC3
Inspector --> UC8

LabTechnician --> UC3
LabTechnician --> UC5
LabTechnician --> UC7

' Внутренние связи прецедентов
UC1 ..> UC2 : <<include>>
UC3 ..> UC4 : <<include>> ' 
UC6 ..> UC5 : <<extend>>

' Бизнес-правила (Notes)
note top of UC2
Статус "Удовлетворительно":
- Архивация
- Только печать отчета
end note

note left of UC5
Статус "Неудовлетворительно" и хуже:
- Заполнение нарушений (UC6)
- Печать отчета (UC7)
- Повторная проверка (UC8)
end note
@enduml
```