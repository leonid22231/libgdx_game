# Worlds

## Пакеты

| Пакет | Что внутри |
|-------|------------|
| `com.lyadev.mygame.world` | Инфраструктура: `WorldEntity`, `JsonMapWorld`, `WorldController`, `GlobalWorld`, camera, `topdown/`, tiles/props/doors factories |
| `com.lyadev.mygame.worlds` | Конкретные карты: `ForestLakeWorld`, `HouseInteriorWorld`, регистрация через `Worlds.registerAll()` |
| `com.lyadev.mygame.base` | `Entity`, модули registry / input |
| `com.lyadev.mygame.modules` | Игровые модули (movement, door, …) |

```
Worlds.registerAll();
WorldController.start(Worlds.startWorldId(), stage, cx, cy);
WorldController.travel(HouseInteriorWorld.ID, traveler, col, row);
```

| Class | Role |
|-------|------|
| `WorldEntity` | id, entities, load / activate / deactivate |
| `JsonMapWorld` | JSON top-down map (tiles, props, portals helper) |
| `ForestLakeWorld` / `HouseInteriorWorld` | concrete worlds + `installPortals()` in `worlds/` |
| `Worlds` | register all concrete worlds |
| `WorldController` | registry, `getActive()`, `travel` / `switchActive` |
| `TopDownMapWorld` | thin facade → active `JsonMapWorld` (editor) |

## Travel

1. `from.deactivate(traveler)` — other residents stay in `from.entities` (off stage)
2. `active = to`
3. `to.activate(traveler, col, row)` — traveler joins `to`, placed on cell

Doors use `DoorSettings(targetWorldId, col, row)` → `WorldController.travel`.

## Debug

Runtime status / console header: `Active world: forest_lake (Forest Lake)`.
Entities / Blocks tabs show only the active world (`GlobalWorld.entities`).

Commands:
```
worlds
world travel house_interior          — move active entity
world travel forest_lake 8 20
world switch house_interior          — activate only, no entity move
```

UI (header + **Worlds** tab):
- **Travel active entity** — `WorldController.travel` (player moves)
- **Switch without entity** — `WorldController.switchActive` (player stays parked)
