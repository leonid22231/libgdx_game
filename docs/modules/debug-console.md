# Debug Console (Desktop)

Отдельное Swing-окно: `lwjgl3/.../DesktopDebugConsole.java`.

## Вкладки

### Runtime

- **Runtime status** — закреплён **сверху** (фиксированная высота ~190px), не прокручивается вместе с логами
- **Logs** — прокручиваемая область под статусом, auto-scroll опционально

### Entities

- Список **playable / AI** entity (без тайлов и декора)
- Детали, модули, Set active / Random pos

### Blocks

- Отдельный список **тайлов** (`TileBlockModule`) и **декора** (`PropModule`)
- Не смешиваются с персонажами во вкладке Entities

## Module DebugScreen (core)

Каждый `EntityModule` может переопределить:

| Метод | Назначение |
| ----- | ---------- |
| `populateDebugScreen(ModuleDebugPanel panel)` | строки, поля и кнопки |
| `handleDebugFieldChange(String fieldId, String value)` | чекбокс / text / number (game thread) |
| `handleDebugAction(String actionId)` | кнопки (game thread) |

### Элементы панели

| Метод `ModuleDebugPanel` | UI | Применение |
| ------------------------ | -- | ---------- |
| `line(key, value)` | текст (read-only) | — |
| `checkbox(id, label, value)` | чекбокс | сразу при клике → `handleDebugFieldChange` |
| `textField(id, label, value)` | поле + **Apply** / Enter | `handleDebugFieldChange` |
| `numberField(id, label, value)` | поле + **Apply** / Enter | `handleDebugFieldChange` |
| `numberField(id, label, value, min, max)` | то же + tooltip диапазона | `handleDebugFieldChange` |
| `action(id, label)` | кнопка | `handleDebugAction` |

Базовая реализация в `EntityModule`: enabled + чекбокс **Runtime paused**.

DTO: `ModuleDebugPanel` → `ModuleDebugScreen` (lines, fields, actions).

`DebugEntityService`:

| Метод | Описание |
| ----- | -------- |
| `collectModuleDebugScreen(tag, moduleName)` | собрать панель |
| `applyModuleDebugField(tag, module, fieldId, value)` | поле с панели |
| `executeModuleDebugAction(tag, moduleName, actionId)` | кнопка с панели |

Примеры действий: Movement — stop/rotate; Vision — reset tracking; Selectable — toggle active; Animation — reset to idle.

## API (core)

`DebugEntityService` — только с **game thread** (`Gdx.app.postRunnable`):

| Метод                                         | Описание                     |
| --------------------------------------------- | ---------------------------- |
| `collectSnapshots()`                          | снимок всех entity + modules |
| `collectModuleDebugScreen(tag, module)`       | debug-панель модуля          |
| `applyModuleDebugField(tag, module, field, value)` | поле с панели           |
| `executeModuleDebugAction(tag, module, id)`   | action с панели              |
| `setActivePlayer(tag)`                        | сделать активным             |
| `setModuleRuntimePaused(tag, module, paused)` | pause/resume runtime         |
| `teleportRandom(tag)`                         | случайная позиция            |
| `spawnPlayablePreset("man"\|"woman"\|"newgirl")` | спавн копии preset        |

## Runtime pause модулей

`EntityModule.setRuntimePaused(true)` — модуль остаётся resolved, но не вызывается `act`/`draw`.

**Не путать** с `disabled` при resolve (нет зависимостей) — такой модуль pause не включить.

## Риски

| Риск                         | Описание                                                                                 |
| ---------------------------- | ---------------------------------------------------------------------------------------- |
| **Thread safety**            | UI только через `Gdx.app.postRunnable` — иначе race с Scene2D                            |
| **Pause movement/vision**    | можно «заморозить» управление или vision thread — осознанно для отладки                  |
| **Spawn**                    | много entity → нагрузка на vision threads; tag автоматически уникализируется (`Man2`, …) |
| **Нет hot-unload модулей**   | нельзя удалить модуль из entity в runtime, только pause или re-resolve (не реализован)   |
| **Нет custom module picker** | spawn только preset Man/Woman; кастомные наборы модулей — следующий этап                 |
| **Swing + LibGDX**           | два окна, EDT vs game thread — все мутации мира только на game thread                    |

## Следующие шаги

- Спавн с выбором модулей (checkboxes перед resolve)
- `re-resolve modules` с предупреждением (dispose + init)
- Команды `entity spawn`, `entity pause` в `DebugCommandService`
