# Module input (клавиши)

Объявление как у `getRequiredModules()` → общий resolve → реализация отдельно.

## Паттерн

```java
@Override
public List<ModuleKeyDecl> getKeyBindings() {
    return List.of(
            ModuleKeyDecl.downUp("up", ModuleKeyChord.of(Keys.W), "Move up (active player)"),
            ModuleKeyDecl.hold(
                    "pick_mode",
                    ModuleKeyChord.ofGroups(
                            ModuleKeyChord.any(Keys.CONTROL_LEFT, Keys.CONTROL_RIGHT),
                            ModuleKeyChord.any(Keys.ALT_LEFT, Keys.ALT_RIGHT)),
                    "Hold Ctrl+Alt: reveal characters, LMB to pick"));
}

@Override
protected void onKeyBinding(String actionId, ModuleKeyEvent event) {
    switch(actionId){
        case "up":
            moveUpToggle();
            break;
        case "pick_mode":
            syncPickMode(event.isHold());
            break;
        default:
            break;
    }
}
```

## Resolve

`ModuleInitializationService` после `markEnabled()`:

```text
getKeyBindings() → ModuleInputRegistry.resolve(module)
```

- один `actionId` на тип модуля (несколько entity только подписываются listener'ами)
- одинаковый chord+trigger у разных action → `IllegalStateException`
- system: `GameSystemKeyBindings.getKeyBindings()` + `resolveSystem`

## Trigger

| Decl | Когда зовётся `onKeyBinding` |
|------|------------------------------|
| `down` | keyDown |
| `up` | keyUp |
| `downUp` | keyDown и keyUp (toggle) |
| `hold` | смена зажатости (`event.isHold()`) |

## Debug

- вкладка **Keys** (таблица: Chord / Action / Owner / Trigger / Description)
- команда `keys` / `bindings`

## Классы

| Класс | Роль |
|-------|------|
| `ModuleKeyDecl` | декларация (id, chord, trigger, description) |
| `ModuleKeyChord` | Ctrl\|Ctrl + Alt\|Alt |
| `ModuleKeyEvent` | событие в handler |
| `ModuleInputRegistry` | resolve + dispatch |
| `GameSystemKeyBindings` | system decls (M, F5, …) |
