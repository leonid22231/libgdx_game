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
| `movement/` | MovementModule — `facingDirection` (4 стороны) + угол vision |
| `vision/` | VisionModule, VisionSettings, … |
| `visionmouse/` | VisionMouseModule — free look (только animated/newgirl) |
| `selectable/` | SelectableModule |
| `ai/` | AiBrainModule — тестовый AI (man в GlobalWorld) |
| `example/` | ExampleModule — шпаргалка, **не в игре** → `modules/example/README.md` |
| `playable/` | PlayableBlueprint, PlayableModules |
| `camera/` | CameraFollowModule — камера за active entity |

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
- [ExampleModule — шпаргалка](../../core/src/main/java/com/lyadev/mygame/modules/example/README.md)
- [UI-слой модулей](./ui-layer.md)
- [Клавиши модулей](../../core/src/main/java/com/lyadev/mygame/base/entity/README-input.md)
- [Debug Console](./debug-console.md)

## План

1. EventBus (vision → selectable)
2. AnimalFactory / WanderModule
3. Runtime re-resolve модулей
