# UI-слой модулей

Screen-space HUD поверх мира (не зависит от world camera).

## Порядок кадра

```
stage.draw()          ← мир (камера)
VisionDebug overlays
OrthoMapEditor HUD
drawModuleUiLayer()   ← EntityModule.drawUi (screen)
logger overlay
```

## API

```java
// EntityModule — override когда нужен HUD (inventory, HP bar…)
@Override
public void drawUi(Batch batch, float parentAlpha) {
    // координаты: пиксели экрана (0,0 = bottom-left)
    MainService.getInstance().getFont().draw(batch, "HP", 16, Gdx.graphics.getHeight() - 16);
}
```

Вызов: `MainService` → `GlobalWorld.drawModuleUi(batch)` → `Entity.drawUi` → модули.

## Правила

| Слой | Метод | Проекция |
|------|--------|----------|
| Мир | `draw(Batch, …)` | world camera |
| UI | `drawUi(Batch, …)` | screen ortho |
| Debug shapes | `VisionDebug.drawOverlays` | world camera |

Не вызывайте `batch.end()` внутри `drawUi` без крайней нужды — batch уже начат для всего UI-прохода.
ShapeRenderer для UI: возьмите `MainService.getShapeRenderer()` (проекция уже screen) и закончите/начните batch осознанно.

Позже сюда же можно повесить отдельный Scene2D `Stage` + `ScreenViewport` для кликабельных окон.
