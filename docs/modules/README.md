# Модульная система Entity

Документация описывает сущности (`Entity`) и модули в MyGame (LibGDX).

## Идея

**Entity** — `extends Actor`, только `tag` + transform + registry модулей.

**Модули** — пакет `com.lyadev.mygame.modules.<name>/`, каждый модуль в своей папке с `*Settings.java`.

## Жизненный цикл

```
GlobalWorld.spawnPlayableEntity(blueprint)
    │
    ├─ new Entity(blueprint.getTag())
    ├─ PlayableModules.register(entity, blueprint)
    │     registerModule(new TextureModule(...)) × N
    │     entity.resolveModules()
    └─ stage.addActor(entity)
```

## Структура modules/

| Папка | Файлы |
|-------|--------|
| `texture/` | TextureModule, TextureSettings, EntityTexture |
| `sprite/` | SpriteModule, SpriteSettings — статичный strip (man, блоки) |
| `animation/` | AnimationModule, AnimationSettings, AnimationClip — idle/walk/run |
| `movement/` | MovementModule, MovementSettings |
| `vision/` | VisionModule, VisionSettings, VisionTracker |
| `selectable/` | SelectableModule |
| `ai/` | AiBrainModule — тестовый AI (man в GlobalWorld) |
| `example/` | ExampleModule — шпаргалка, **не в игре** |
| `playable/` | PlayableBlueprint, PlayableModules |

## Зависимости модулей

```java
@Override
public List<Class<? extends EntityModule>> getRequiredModules() {
    return List.of(SpriteModule.class);
}

require(MovementModule.class).getFacingDirection();
```

Порядок init: `ModuleInitializationService` (топологическая сортировка).

## Граф playable

```
TextureModule → MovementModule → SpriteModule     (man/woman)
TextureModule → MovementModule → AnimationModule  (newgirl)
VisionModule, SelectableModule — без deps
```

## Связанные документы

- [Entity lean core](./entity-core.md)
- [Общение модулей](./communication.md)
- [Зависимости модулей](./dependencies.md)
- [Debug Console](./debug-console.md)

## План

1. EventBus (vision → selectable)
2. AnimalFactory / WanderModule
3. Runtime re-resolve модулей
