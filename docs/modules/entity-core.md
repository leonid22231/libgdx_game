# Entity — lean core и модули

## Принцип

**Entity** — минимальное ядро на сцене (`extends Actor`). Поведение и runtime — только в **модулях** под `com.lyadev.mygame.modules.*`.

## Что в Entity

| Поле | Назначение |
|------|------------|
| `tag` | Короткий идентификатор (`Man`, `Woomen`) |
| `TAG` | Формат лога `Player[Man]` |
| Actor x/y/width/height | Transform |
| `ModuleRegistry` | Регистрация и resolve модулей |

**Настроек на Entity нет.** Конфиг передаётся в конструкторы модулей при `registerModule()`.

## Структура modules/

```
modules/
├── texture/
│   ├── TextureModule.java
│   ├── TextureSettings.java
│   └── EntityTexture.java
├── sprite/
│   ├── SpriteModule.java
│   └── SpriteSettings.java
├── movement/
│   ├── MovementModule.java
│   └── MovementSettings.java
├── vision/
│   ├── VisionModule.java
│   ├── VisionSettings.java
│   ├── VisionTracker.java
│   ├── VisionScanner.java
│   ├── VisionBackgroundThread.java
│   ├── VisionRegistry.java      ← package-private
│   └── VisionDebug.java         ← debug API наружу
├── selectable/
│   └── SelectableModule.java
├── ai/
│   └── AiBrainModule.java
└── playable/
    ├── PlayableBlueprint.java   ← DTO для spawn, не на Entity
    └── PlayableModules.java
```

## Сборка playable

```java
PlayableBlueprint blueprint = GlobalWorld.buildPlayableBlueprint(
        "Man", Assets.PERSON_MAN, visibleRadius);

Entity entity = new Entity(blueprint.getTag());
PlayableModules.register(entity, blueprint);
```

`PlayableBlueprint` — агрегат module settings для фабрики. Живёт только при создании сущности.

## Пример: животное (будущее)

```java
Entity wolf = new Entity("Wolf");
wolf.registerModule(new TextureModule(textureSettings));
wolf.registerModule(new SpriteModule(spriteSettings));
wolf.registerModule(new VisionModule(visionSettings));
wolf.registerModule(new AiBrainModule());
// без SelectableModule и keyboard MovementModule
wolf.resolveModules();
```

## Доступ к state

```java
SelectableModule.from(entity).isActive();
VisionModule.from(entity).getVisibleEntityCount();
entity.getModule(MovementModule.class);
```

Не используй `entity.getSettings()` — такого API больше нет.
