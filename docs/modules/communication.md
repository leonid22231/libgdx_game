# Общение модулей

## Правило

**Модули инкапсулированы.** Вся логика и runtime-state — внутри пакета `modules/<name>/`.

**Модули общаются только друг с другом** — через `require()`, `Module.from(entity)` или package-private API между модулями одного пакета.

Внешний код (`GlobalWorld`, input, debug) **не лезет внутрь** модуля — только публичный фасад модуля.

## Что может снаружи

| Кто | Как |
|-----|-----|
| `GlobalWorld` | `SelectableModule.from(e).setActive()` — публичный API модуля |
| `KeyboardInputService` | `entity.getModule(MovementModule.class).moveLeftToggle()` |
| Debug | `VisionModule.from(entity).getVisibleEntityCount()` |
| MyLogger | `VisionDebug.collectBackgroundThreadLines()` — debug-facade пакета |
| UI HUD | `EntityModule.drawUi` — screen-space, см. [ui-layer.md](./ui-layer.md) |
| Клавиши | `getKeyBindings()` + `onKeyBinding` — как deps; см. `base/entity/README-input.md` |

## Что нельзя

- Логика vision в `utils/listeners/EntityListenerThread` ❌ (удалено)
- `GlobalWorld.listeners` — реестр потоков vision ❌ (заменён на `VisionRegistry` внутри пакета)
- `VisionTracker` вызывает `SelectableModule` напрямую ❌ (теперь только `VisionModule.notifyTargetSpotted`)

## Пример: vision полностью в пакете

```
modules/vision/
├── VisionModule.java           ← lifecycle + runBackgroundScan + module→module notify
├── VisionSettings.java
├── VisionTracker.java          ← только state (контур, список visible)
├── VisionScanner.java          ← алгоритм LOS
├── VisionBackgroundThread.java ← sleep + owner.runBackgroundScan()
├── VisionRegistry.java         ← peers внутри пакета (package-private)
└── VisionDebug.java            ← debug API наружу
```

Поток — **private деталь** `VisionModule`, не общая утилита.

## Cross-module (модуль → модуль)

### Прямой вызов (legacy, visibility)

```java
// VisionModule → SelectableModule target-entity (пока напрямую)
SelectableModule selectable = SelectableModule.from(target.getEntity());
selectable.setVisibleByVision(true);
```

### События модуля (предпочтительно для AI и новых подписчиков)

Подписчик не знает про bus — только fluent API издателя:

```java
// ExampleModule.init() — подписчик (шпаргалка)
// AiBrainModule.init() — то же, живой тест на man
require(VisionModule.class).onEntitySpotted(event -> {
    Entity target = event.getTarget();
});
```

Инфраструктура в `EntityModule` + `ModuleEventChannel`:

- издатель: `createEventChannel()` + `onXxx(Consumer)` + `publish` только если `hasSubscribers()`
- подписчик регистрируется только в `init()` (через `currentInitializingModule()`)
- отписка автоматически в `EntityModule.dispose()` подписчика
- dispatch на main thread: {@code publish()} из фонового потока → {@code flushPending()} в {@code act()} издателя

Добавить новое событие в модуле:

```java
private final ModuleEventChannel<MyEvent> myEvents = createEventChannel();

public void onMyEvent(Consumer<MyEvent> handler) {
    myEvents.subscribe(currentInitializingModule(), handler);
}

// внутри логики модуля:
if (myEvents.hasSubscribers()) {
    myEvents.publish(new MyEvent(...));
}
// в act() издателя:
myEvents.flushPending();
```

## Зависимости через Class

```java
@Override
public List<Class<? extends EntityModule>> getRequiredModules() {
    return List.of(SpriteModule.class);
}

require(MovementModule.class).getFacingDirection();
```

Новый модуль (`HearingModule`) — **своя папка**, свой поток/логика; подписчики через `onXxx()` без правок издателя.
