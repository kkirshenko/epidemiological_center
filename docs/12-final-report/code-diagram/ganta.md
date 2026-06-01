```plantuml
@startgantt
title Диаграмма Ганта проекта "Санитарно-эпидемиологический центр"

' Фазы проекта
[Анализ предметной области и требований] lasts 2 weeks and starts at [Project start]
[Проектирование системы] lasts 2 weeks and starts at end of [Анализ предметной области и требований]
[Разработка backend (Spring Boot)] lasts 5 weeks and starts at end of [Проектирование системы]
[Разработка frontend (Thymeleaf)] lasts 4 weeks and starts at end of [Проектирование системы]
[Модульное и интеграционное тестирование] lasts 3 weeks and starts at end of [Разработка backend (Spring Boot)]
[Документирование системы] lasts 2 weeks and starts at end of [Модульное и интеграционное тестирование]
[Финальное тестирование и развёртывание] lasts 2 weeks and starts at end of [Документирование системы]

' Зависимости
[Разработка frontend (Thymeleaf)] starts at 1 week after start of [Разработка backend (Spring Boot)]

' Цветовое кодирование
[Анализ предметной области и требований] is colored in DarkBlue
[Проектирование системы] is colored in Blue
[Разработка backend (Spring Boot)] is colored in Green
[Разработка frontend (Thymeleaf)] is colored in Orange
[Модульное и интеграционное тестирование] is colored in Yellow
[Документирование системы] is colored in Purple
[Финальное тестирование и развёртывание] is colored in Red

' Критический путь
[Разработка backend (Spring Boot)] is highlighted
@endgantt
```
