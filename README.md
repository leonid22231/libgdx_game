# LibGdxGame

## Запуск (IntelliJ IDEA)

Конфигурации лежат в [`.idea/runConfigurations/`](.idea/runConfigurations/):

| Конфигурация | Когда использовать |
|---|---|
| **MyGame Desktop (Gradle)** | Рекомендуется — `gradle :lwjgl3:run`, working dir `assets/` из Gradle |
| **MyGame Desktop** | Application: main `Lwjgl3Launcher`, working dir `$PROJECT_DIR$/assets` |

После открытия проекта конфигурации появятся в списке Run. Если Application не находит модуль, выберите в настройках модуль `Games.lwjgl3.main` (или `lwjgl3.main` — зависит от имени Gradle-проекта).

Альтернатива из терминала:

```bash
./gradlew :lwjgl3:run
```

## Platforms
TODO
