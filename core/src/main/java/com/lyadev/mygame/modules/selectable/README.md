# SelectableModule

Выбор активного персонажа и видимость (fog of war / vision).

## Клавиши (декларативно)

```java
@Override
public List<ModuleKeyDecl> getKeyBindings() {
    return List.of(
            ModuleKeyDecl.hold(
                    "pick_mode",
                    ModuleKeyChord.ofGroups(
                            ModuleKeyChord.any(Keys.CONTROL_LEFT, Keys.CONTROL_RIGHT),
                            ModuleKeyChord.any(Keys.ALT_LEFT, Keys.ALT_RIGHT)),
                    "Hold Ctrl+Alt: reveal all characters, then LMB to select"));
}

@Override
protected void onKeyBinding(String actionId, ModuleKeyEvent event) {
    if("pick_mode".equals(actionId)){
        syncPickMode(event.isHold());
    }
}
```

1. Зажать **Ctrl+Alt** → все персонажи видны  
2. **LMB** → выбрать  
3. Отпустить → снова обычный fog of war  

## Видимость

| Условие | Виден? |
|---------|--------|
| Map editor ON | нет |
| pick mode | да |
| `active` / `visibleByVision` | да |
| иначе | нет |

См. `base/entity/README-input.md`.
