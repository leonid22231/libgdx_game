# Зависимости модулей

Как модуль объявляет зависимости через **классы** и как `ModuleInitializationService` решает порядок `init()`.

## Зачем

Без явных зависимостей модуль мог вызвать `getModule(...)`, получить `null` и молча не работать. Теперь модуль **объявляет** нужные классы — если их нет на сущности, он **не стартует**, а в лог пишется причина.

Перед `init()` зависимости **привязываются автоматически** — в коде модуля достаточно `require(SpriteModule.class)`.

## API

### EntityModule.getRequiredModules()

```java
@Override
public List<Class<? extends EntityModule>> getRequiredModules() {
    return List.of(SpriteModule.class);
}
```

- Классы — те же Java-классы модулей (`MovementModule.class`, не строки)
- Пустой список (default) — модуль не зависит ни от кого
- Зависимости **транзитивны**: если `MovementModule` требует `SpriteModule`, а `SpriteModule` требует `TextureModule`, то для movement должны быть зарегистрированы все три

### EntityModule.require()

```java
@Override
public void act(float delta) {
    require(MovementModule.class).getFacingDirection();
}
```

- Доступен в `init()`, `act()`, `draw()` — после успешного resolve
- Бросает `IllegalStateException`, если зависимость не была привязана (ошибка конфигурации)
- **Не нужно** вручную искать модуль через `getEntity().getModule("...")` в `init()`

### Регистрация и resolve

```java
entity.registerModule(new TextureModule());
entity.registerModule(new SpriteModule());
entity.registerModule(new MovementModule());
entity.resolveModules();   // ModuleInitializationService → init() в порядке deps
```

**Важно:** `registerModule()` **не** вызывает `init()`. Init только после `resolveModules()`.

`PlayableModules.register(entity)` делает то же самое и в конце вызывает `entity.resolveModules()`.

## ModuleInitializationService

`ModuleRegistry.resolveAndInit()` делегирует сюда.

```
1. Собрать registered-модули, индекс по Class
2. Итеративно помечать active те модули, у которых все required Class зарегистрированы и уже active
3. Остальные → markDisabled("missing: VisionModule") + Gdx.app.debug
4. Topological sort active-модулей по Class → порядок init (deps первыми)
5. Для каждого active: bindDependencies() → markEnabled() → init()
6. Entity.modules = только active
```

### Пример цепочки A → B → C → D

```
TextureModule   (нет deps)
MovementModule  → (нет deps, хранит facingDirection)
SpriteModule    → TextureModule + MovementModule
AiBrainModule   → VisionModule + MovementModule
```

Init-порядок для playable + AI:

```
TextureModule → MovementModule → SpriteModule → VisionModule → SelectableModule → AiBrainModule
```

(VisionModule и SelectableModule без deps — попадают в active, когда их deps не требуются; порядок между «независимыми» — по порядку register)

### Пример: AiBrain без movement

```java
entity.registerModule(new VisionModule());
entity.registerModule(new AiBrainModule());  // requires MovementModule + VisionModule
entity.resolveModules();
// vision_module  → enabled
// ai_brain_module → disabled (missing: MovementModule)
```

## Текущие зависимости

| Модуль | getRequiredModules() |
|--------|----------------------|
| `TextureModule` | — |
| `SpriteModule` | `TextureModule.class`, `MovementModule.class` |
| `MovementModule` | — |
| `VisionModule` | — |
| `SelectableModule` | — |
| `AiBrainModule` | `VisionModule.class`, `MovementModule.class` |

## Entity.getModule(Class)

Для внешнего кода (input, world, debug):

```java
MovementModule movement = entity.getModule(MovementModule.class);
```

Возвращает только **enabled** модуль или `null`.

## Логирование

При disabled-модуле:

```
[DEBUG][ModuleInitializationService] Player[Man] disabled module: ai_brain_module — missing: MovementModule
```

## Добавление нового модуля

1. Создать класс `extends EntityModule`
2. Реализовать `getName()` (уникальное snake_case имя)
3. При необходимости — `getRequiredModules()` с `List.of(...class)`
4. Использовать `require(MyDep.class)` вместо ручного lookup
5. Зарегистрировать: `entity.registerModule(new MyModule())`
6. Вызвать `entity.resolveModules()`

Пример:

```java
public class AiBrainModule extends EntityModule {
    @Override
    public String getName() {
        return "ai_brain_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(VisionModule.class, MovementModule.class);
    }

    @Override
    public void init() {
        VisionModule vision = require(VisionModule.class);
        MovementModule movement = require(MovementModule.class);
    }
}
```

## Что дальше

- **EntityEventBus** — один `publish()` на событие, много подписчиков
- **Capabilities** — `MovementCommands` вместо cast к `MovementModule`
- **Runtime re-resolve** — hot add/remove модулей
