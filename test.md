# Тестирование, покрытие JaCoCo и статический анализ SonarQube

Ниже — пошаговый гайд для новичков, как в этом проекте:
1. Запустить JUnit-тесты.
2. Получить отчёт покрытия (и проверить порог 40%+) через JaCoCo.
3. Получить отчёт статического анализа в SonarQube.

---

## 1) Что нужно установить заранее

- **JDK 17+**
- **Gradle** (если `./gradlew` недоступен)
- **Docker + Docker Compose** (для локального SonarQube)

Проверка окружения:

```bash
java -version
gradle -v
docker --version
docker compose version
```

---

## 2) Запуск JUnit-тестов

Из корня проекта:

```bash
gradle test
```

Результаты тестов:
- консольный вывод Gradle;
- HTML-отчёт: `build/reports/tests/test/index.html`.

---

## 3) JaCoCo: отчёт покрытия и контроль порога 40%

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

Если покрытие ниже 40%, задача `jacocoTestCoverageVerification` завершится ошибкой.

---

## 4) SonarQube: статический анализ и отчёт

### Вариант A (локально через Docker)

1. Поднять SonarQube:

```bash
docker run -d --name sonarqube \
  -p 9000:9000 \
  sonarqube:community
```

2. Открыть UI:
- `http://localhost:9000`
- Первый вход обычно `admin / admin` (попросит сменить пароль).

3. Создать токен:
- `My Account -> Security -> Generate Tokens`.

4. Запустить анализ из корня проекта:

```bash
gradle clean test jacocoTestReport sonarqube \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<ВАШ_SONAR_TOKEN>
```

5. Посмотреть отчёт:
- В веб-интерфейсе SonarQube откройте проект `sanepidcenter`.

### Вариант B (корпоративный SonarQube)

Если у вас уже есть сервер SonarQube, используйте его URL и токен:

```bash
gradle clean test jacocoTestReport sonarqube \
  -Dsonar.host.url=https://sonarqube.company.local \
  -Dsonar.token=<ВАШ_SONAR_TOKEN>
```

---

## 5) Полезные команды «всё сразу»

### Быстрая проверка локально

```bash
gradle clean check jacocoTestReport
```

### Полный цикл с SonarQube

```bash
gradle clean check jacocoTestReport sonarqube \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=<ВАШ_SONAR_TOKEN>
```

---

## 6) Частые проблемы

1. **`Plugin ... not found` / нет доступа к репозиториям**
   - Проверьте интернет/прокси и доступ к Maven/Gradle Plugin Portal.

2. **`./gradlew` не запускается**
   - Дайте права: `chmod +x gradlew`.
   - Если в репозитории нет Gradle Wrapper (`gradle/wrapper/*`), используйте системный `gradle`.

3. **SonarQube не стартует**
   - Проверьте свободную память Docker.
   - Подождите 1–2 минуты после запуска контейнера.

4. **Порог покрытия не проходит**
   - Добавьте/расширьте JUnit-тесты на сервисы/контроллеры с низким покрытием.

