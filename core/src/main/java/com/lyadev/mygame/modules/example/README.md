# ExampleModule — полная шпаргалка

**Не подключать в игру.** Эталон рядом: `ExampleModule.java`.  
Живой тест тех же паттернов: `AiBrainModule` на man.

| Файл | Роль |
|------|------|
| `ExampleModule.java` | поведение + комментарии по секциям |
| `ExampleEvent.java` | payload для `onExampleEvent` |
| `README.md` | эта документация |

Общая архитектура модулей: `docs/modules/` (`communication.md`, `dependencies.md`, `entity-core.md`, `ui-layer.md`, `debug-console.md`).

---

## 1. Структура нового модуля

```
modules/myfeature/
├── MyFeatureModule.java      ← extends EntityModule
├── MyFeatureSettings.java    ← immutable конфиг в конструктор (optional)
└── MyFeatureEvent.java       ← payload для onXxx (optional)
```

Конфиг **не** на `Entity`. При spawn:

```java
entity.registerModule(new ExampleModule(/* optional settings */));
entity.resolveModules(); // обязательно ПОСЛЕ всех registerModule
```

---

## 2. Жизненный цикл

```
registerModule(...) × N          ← все модули ДО resolve
entity.resolveModules()
  → ModuleInitializationService
  → topo-sort по getRequiredModules()
  → bindDependencies + markEnabled
  → ModuleInputRegistry.resolve(getKeyBindings())
  → init() на каждом активном
  → disabled, если deps нет

каждый кадр:
  Entity.act(delta)  → module.act(delta)   // runtimeActive
  Entity.draw(...)   → module.draw(...)    // world camera
  Entity.drawUi(...) → module.drawUi(...)  // screen HUD (после мира)

entity.dispose() → module.dispose()
```

**Ошибка:** `resolveModules()` → потом `registerModule()` → `init()` не вызовется.

---

## 3. API `EntityModule` (базовый класс)

### Обязательно / override lifecycle

| Метод | Когда | Назначение |
|-------|--------|------------|
| `getName()` | всегда | id модуля (`"example_module"`) |
| `getRequiredModules()` | resolve | deps: кто должен быть на entity раньше |
| `getKeyBindings()` | resolve | декларация клавиш/chord + description |
| `onKeyBinding(id, event)` | input | реализация объявленных клавиш |
| `init()` | один раз после resolve | подписки, кеш `require`, старт потоков |
| `act(float delta)` | каждый кадр | логика; `flushPending()` каналов событий |
| `draw(Batch, float)` | world pass | отрисовка в координатах мира / камеры |
| `drawUi(Batch, float)` | UI pass | HUD в пикселях экрана (`docs/modules/ui-layer.md`) |
| `dispose()` | снятие entity | стоп потоков, `clear()` каналов, `super.dispose()` |

### Зависимости и entity

| Метод | Доступ | Назначение |
|-------|--------|------------|
| `getEntity()` | protected | владелец-модуля `Entity` |
| `require(Class<T>)` | protected | dep после bind; иначе `IllegalStateException` |
| `getRequiredModuleNames()` | public | имена deps (debug) |
| `attach(Entity)` | framework | вызывается registry, не руками из игры |

### Включение / пауза

| Метод | Назначение |
|-------|------------|
| `isEnabled()` | прошёл resolve (deps ок) |
| `getDisabledReason()` | почему disabled |
| `isRuntimePaused()` / `setRuntimePaused(boolean)` | пауза без dispose |
| `isRuntimeActive()` | `enabled && !runtimePaused` — условие для act/draw |

### События (инфра)

| Метод | Назначение |
|-------|------------|
| `createEventChannel()` | создать `ModuleEventChannel<E>` у издателя |
| `subscribeTo(channel, handler)` | явная подписка `this` на чужой канал |
| `currentInitializingModule()` | модуль в `init()` — для `onXxx(handler)` без передачи `this` |
| `clearEventSubscriptions()` | снять все подписки, где этот модуль — подписчик (`dispose`) |

### Debug Console

| Метод | Назначение |
|-------|------------|
| `populateDebugScreen(ModuleDebugPanel)` | строки, checkbox, поля, кнопки |
| `handleDebugFieldChange(fieldId, value)` | реакция на checkbox/input |
| `handleDebugAction(actionId)` | реакция на кнопку |

Базовый `populateDebugScreen` уже даёт: `module`, `enabled`, `runtimeActive`, `disabledReason`, checkbox `runtime_paused`.

---

## 4. Что делает сам `ExampleModule`

### Поля

| Поле | Зачем |
|------|--------|
| `exampleEvents` | канал `ModuleEventChannel<ExampleEvent>` |
| `exampleEventTimer` | раз в 5 с публикует tick, если есть подписчики |

### Методы ExampleModule

| Метод | Тип | Описание |
|-------|-----|----------|
| `getName()` | override | `"example_module"` |
| `getRequiredModules()` | override | `VisionModule`, `MovementModule` |
| `init()` | override | `require` deps + `vision.onEntitySpotted(this::handleEntitySpotted)` |
| `onExampleEvent(Consumer)` | public API | подписка других модулей (только из их `init()`) |
| `from(Entity)` | static | `entity.getModule(ExampleModule.class)` |
| `act(delta)` | override | `flushPending`, пример `publish` раз в 5 с |
| `draw(batch, α)` | override | пусто (world debug — см. Vision) |
| `drawUi(batch, α)` | override | пусто (HUD — `docs/modules/ui-layer.md`) |
| `dispose()` | override | `exampleEvents.clear()` + `super.dispose()` |
| `handleEntitySpotted(...)` | private | лог observer → target |

### `ExampleEvent`

| Поле | Тип | Смысл |
|------|-----|--------|
| `entity` | `Entity` | кто опубликовал |
| `message` | `String` | текст события |

---

## 5. Зависимости (как объявлять)

```java
@Override
public List<Class<? extends EntityModule>> getRequiredModules() {
    return List.of(VisionModule.class, MovementModule.class);
}

@Override
public void init() {
    VisionModule vision = require(VisionModule.class); // уже init
    vision.onEntitySpotted(this::handleEntitySpotted);
}
```

Нет dep на entity → модуль `markDisabled("missing: …")`, `init` не зовётся как enabled.

## 5b. Клавиши (как getRequiredModules)

```java
@Override
public List<ModuleKeyDecl> getKeyBindings() {
    return List.of(
            ModuleKeyDecl.downUp("up", ModuleKeyChord.of(Keys.W), "Move up"),
            ModuleKeyDecl.hold("pick_mode", chord, "Hold to pick"));
}

@Override
protected void onKeyBinding(String actionId, ModuleKeyEvent event) {
    switch(actionId){
        case "up": ...
        case "pick_mode": sync(event.isHold());
        default: break;
    }
}
```

Resolve: `ModuleInputRegistry` при `resolveModules()`. Debug: вкладка Keys / `keys`.  
Подробнее: `base/entity/README-input.md`.

---

## 6. Вызов другого модуля

| Где | Как |
|-----|-----|
| `init` / `act` / `draw` / `drawUi` | `require(MovementModule.class).stopMoving()` |
| Без объявленного dep (хуже) | `VisionModule.from(getEntity())` |
| Снаружи пакета (world, debug, input) | `SelectableModule.from(entity).setActive(true)` |

**Нельзя:** лезть в package-private (`VisionTracker`, `VisionRegistry`).

---

## 7. Подписка на чужие события

```java
// Только в init() подписчика:
require(VisionModule.class).onEntitySpotted(event -> {
    Entity target = event.getTarget();
    Entity observer = event.getObserver();
});
```

- Отписка при `dispose()` подписчика
- `onEntitySpotted` = observer увидел target; скан vision только у **active** player
- Доставка на main thread: `publish` (в т.ч. из фона) → `flushPending()` в `act()` издателя

---

## 8. Свои события (издатель)

```java
private final ModuleEventChannel<ExampleEvent> exampleEvents = createEventChannel();

public void onExampleEvent(Consumer<ExampleEvent> handler) {
    exampleEvents.subscribe(currentInitializingModule(), handler);
}

// в act():
exampleEvents.flushPending();
if (exampleEvents.hasSubscribers()) {
    exampleEvents.publish(new ExampleEvent(getEntity(), "example tick"));
}

// в dispose():
exampleEvents.clear();
super.dispose();
```

Подписчик:

```java
require(ExampleModule.class).onExampleEvent(e -> { ... });
```

---

## 9. World draw vs UI draw

| Метод | Проекция | Когда |
|-------|----------|--------|
| `draw` | world camera | внутри `stage.draw` |
| `drawUi` | screen ortho | после мира, до logger |

Не делайте `batch.end()` / ShapeRenderer на каждого entity в `draw` — один общий проход (как `VisionDebug.drawOverlays`).

Подробнее: `docs/modules/ui-layer.md`.

---

## 10. Debug Console

```java
@Override
public void populateDebugScreen(ModuleDebugPanel panel) {
    super.populateDebugScreen(panel);
    panel.line("timer", exampleEventTimer);
    panel.action("ping", "Publish example event");
}

@Override
public String handleDebugAction(String actionId) {
    if ("ping".equals(actionId)) {
        exampleEvents.publish(new ExampleEvent(getEntity(), "debug ping"));
        return "published";
    }
    return super.handleDebugAction(actionId);
}
```

См. `docs/modules/debug-console.md`.

---

## 11. Подключить только к одному персонажу

```java
PlayableModules.register(entity, blueprint); // или registerModules
entity.registerModule(new AiBrainModule());  // extra
entity.resolveModules();
```

---

## 12. Чеклист нового модуля

1. Папка `modules/<name>/` + `*Module` (+ Settings/Event по нужде)
2. `getName`, `getRequiredModules`, `init` / `act` / `dispose`
3. `draw` только для мира; HUD → `drawUi`
4. События через `createEventChannel` + `onXxx` + `flushPending`
5. Снаружи — только public/`from(entity)`, без внутренностей пакета
6. Все `registerModule` **до** `resolveModules`
7. Не тащить `ExampleModule` в игру — копируй паттерн в свой пакет

---

## См. также

- `docs/modules/communication.md` — правила общения
- `docs/modules/dependencies.md` — граф deps
- `docs/modules/entity-core.md` — lean Entity
- `docs/modules/ui-layer.md` — screen HUD
- `docs/modules/debug-console.md` — ModuleDebugPanel
