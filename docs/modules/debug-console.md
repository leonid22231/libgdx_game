# Debug Console (Desktop)

Отдельное Swing-окно: `lwjgl3/.../DesktopDebugConsole.java`.

## Вкладки

### Runtime

- **Runtime status** — закреплён **сверху** (фиксированная высота ~190px), не прокручивается вместе с логами
- **Logs** — прокручиваемая область под статусом, auto-scroll опционально

### Entities

- Список всех `GlobalWorld.entities` (обновление ~300 ms)
- Детали: позиция, размер, status flags, sprite, vision count
- Таблица модулей: имя, state (`active` / `paused` / `disabled`), requires, чекбокс **Pause**
- Кнопки:
  - **Set active** — `GlobalWorld.setActivePlayer`
  - **Random pos** — телепорт + reset vision
  - **Spawn Man / Spawn Woomen** — новая playable-сущность с уникальным tag

## API (core)

`DebugEntityService` — только с **game thread** (`Gdx.app.postRunnable`):

| Метод | Описание |
|-------|----------|
| `collectSnapshots()` | снимок всех entity + modules |
| `setActivePlayer(tag)` | сделать активным |
| `setModuleRuntimePaused(tag, module, paused)` | pause/resume runtime |
| `teleportRandom(tag)` | случайная позиция |
| `spawnPlayablePreset("man"\|"woman")` | спавн копии preset |

## Runtime pause модулей

`EntityModule.setRuntimePaused(true)` — модуль остаётся resolved, но не вызывается `act`/`draw`.

**Не путать** с `disabled` при resolve (нет зависимостей) — такой модуль pause не включить.

## Риски

| Риск | Описание |
|------|----------|
| **Thread safety** | UI только через `Gdx.app.postRunnable` — иначе race с Scene2D |
| **Pause movement/vision** | можно «заморозить» управление или vision thread — осознанно для отладки |
| **Spawn** | много entity → нагрузка на vision threads; tag автоматически уникализируется (`Man2`, …) |
| **Нет hot-unload модулей** | нельзя удалить модуль из entity в runtime, только pause или re-resolve (не реализован) |
| **Нет custom module picker** | spawn только preset Man/Woman; кастомные наборы модулей — следующий этап |
| **Swing + LibGDX** | два окна, EDT vs game thread — все мутации мира только на game thread |

## Следующие шаги

- Спавн с выбором модулей (checkboxes перед resolve)
- `re-resolve modules` с предупреждением (dispose + init)
- Команды `entity spawn`, `entity pause` в `DebugCommandService`
