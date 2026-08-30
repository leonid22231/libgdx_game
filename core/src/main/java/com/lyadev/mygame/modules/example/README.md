# ExampleModule — шпаргалка по модулям

**Не подключать в игру.** Эталон кода: `ExampleModule.java`. Живой тест: `AiBrainModule` на man в `GlobalWorld`.

## Структура пакета

```
modules/myfeature/
├── MyFeatureModule.java    ← extends EntityModule
├── MyFeatureSettings.java  ← конфиг в конструктор (optional)
└── MyFeatureEvent.java     ← payload для onXxx() (optional)
```

## Жизненный цикл

```
registerModule(new TextureModule(...))
registerModule(new VisionModule(...))
registerModule(new MyFeatureModule(...))   ← все ДО resolve
entity.resolveModules()
    → ModuleInitializationService
    → topo-sort по getRequiredModules()
    → bindDependencies + init() на каждом активном модуле
    → Entity.act() каждый кадр → module.act()
    → entity.dispose() → module.dispose()
```

## Зависимости

```java
@Override
public List<Class<? extends EntityModule>> getRequiredModules() {
    return List.of(VisionModule.class);
}

@Override
public void init() {
    VisionModule vision = require(VisionModule.class);  // уже инициализирован
}
```

## Вызов методов другого модуля

| Где | Как |
|-----|-----|
| `init()`, `act()`, `draw()` | `require(MovementModule.class).stopMoving()` |
| Статический фасад | `VisionModule.from(getEntity())` |
| Снаружи пакета (GlobalWorld, debug) | `SelectableModule.from(entity).setActive(true)` |

**Не лезть** во внутренности пакета (`VisionTracker`, `VisionRegistry`).

## Подписка на события (модуль → модуль)

```java
// В init() подписчика:
require(VisionModule.class).onEntitySpotted(event -> {
    Entity target = event.getTarget();   // кого увидели
    Entity observer = event.getObserver(); // я (эта entity)
});
```

- Подписка только в `init()` другого модуля
- Отписка автоматически при `dispose()` подписчика
- `onEntitySpotted` = observer увидел target; скан только у **active** player

## Свои события (издатель)

```java
private final ModuleEventChannel<MyEvent> events = createEventChannel();

public void onMyEvent(Consumer<MyEvent> handler) {
    events.subscribe(currentInitializingModule(), handler);
}

if (events.hasSubscribers()) {
    events.publish(new MyEvent(...));
}
```

## Подключить модуль только к man

```java
PlayableModules.registerModules(entity, blueprint);
entity.registerModule(new AiBrainModule());  // extra только для man
entity.resolveModules();
```

**Ошибка:** `resolveModules()` → потом `registerModule()` → `init()` не вызовется.

## См. также

- [communication.md](../communication.md) — правила общения модулей
- [dependencies.md](../dependencies.md) — граф зависимостей
- [entity-core.md](../entity-core.md) — lean Entity
