# Карты (top-down)

Миры — объекты `WorldEntity` (см. [`docs/worlds/README.md`](../worlds/README.md)).
Инфраструктура: `com.lyadev.mygame.base.world.*`. Конкретные карты: `com.lyadev.mygame.worlds.*`.
Карта по умолчанию — `ForestLakeWorld` → `maps/forest_lake.json`.
Тестовый интерьер — `HouseInteriorWorld` → `maps/house_interior.json`.

`TopDownMapWorld` — фасад на **активный** мир (редактор / legacy API).

Камера смотрит на **~400×400** world px (`WorldCameraSettings`) — не на всю карту.
`forest_lake` **40×28** (640×448) больше окна: follow / **СКМ** pan / в редакторе Ctrl+Wheel zoom.

## Миры / двери (тест)

- Entity-декор = `PropModule` + `DoorModule`
- Пересечение с **active player** → `WorldController.travel(worldId, …)`
- Только traveler переходит; остальные playable остаются в списке исходного мира (off stage)
- Порталы: `ForestLakeWorld` / `HouseInteriorWorld.installPortals()`

| Где | Визуал | Куда |
|-----|--------|------|
| Лес `[8,22]` | rocks_a | дом `[6,4]` |
| Дом `[6,1]` | mushroom_a | лес `[8,20]` |

Старт всегда `forest_lake`. В доме небо/`background.png` не рисуется.

## Масштаб персонажей

| Кто | Кадр | scale | На экране |
|-----|------|-------|-----------|
| man/woman | 16×32 | 1 | ~16×32 |
| newgirl | 64×128 | **0.25** | ~16×32 |
| cat | свои кадры | 1 | как в sheet |

Крутилка: `SCALE_FACTOR` / `TextureSettings.scaleFactor` (теперь `float`) в классе персонажа.

## Декор

Crop’ы props из bounding box’ов — по одному экземпляру (`asset_catalog.json`).

## Редактор

**M** — вкл/выкл. Персонажи скрываются, камера follow паузится.

Editable area = **`width` × `height` в JSON карты**, не `WorldCameraSettings` и не zoom.
**LMB за краем расширяет карту** (до 256×256); **F5** пишет новый размер в JSON.

| Клавиша | Действие |
|---------|----------|
| `1` / `2` | ground / decor |
| LMB | paint tile / place prop (растёт карта) |
| RMB | erase tile / delete prop |
| Wheel | цикл кисти |
| **Ctrl+Wheel** | zoom in/out |
| MMB drag | pan |
| F5 | save (в `assets/`, workingDir) |

Подсказки управления — панель справа сверху, пока редактор включён.

## Модель Entity

- земля → `TileBlockModule`
- декор → `PropModule`
- JSON — сериализация; в рантайме всё Entity
