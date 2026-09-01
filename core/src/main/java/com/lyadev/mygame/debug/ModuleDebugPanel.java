package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.List;

/** Builder для {@link com.lyadev.mygame.base.EntityModule#populateDebugScreen(ModuleDebugPanel)}. */
public final class ModuleDebugPanel {
    private final List<ModuleDebugLine> lines = new ArrayList<>();
    private final List<ModuleDebugField> fields = new ArrayList<>();
    private final List<ModuleDebugAction> actions = new ArrayList<>();

    public void line(String key, Object value) {
        lines.add(new ModuleDebugLine(key, String.valueOf(value)));
    }

    public void checkbox(String id, String label, boolean value) {
        fields.add(new ModuleDebugField(
                id,
                label,
                ModuleDebugFieldType.CHECKBOX,
                Boolean.toString(value),
                null,
                null));
    }

    public void textField(String id, String label, String value) {
        fields.add(new ModuleDebugField(
                id,
                label,
                ModuleDebugFieldType.TEXT,
                value,
                null,
                null));
    }

    public void numberField(String id, String label, double value) {
        numberField(id, label, value, null, null);
    }

    public void numberField(String id, String label, double value, Double min, Double max) {
        fields.add(new ModuleDebugField(
                id,
                label,
                ModuleDebugFieldType.NUMBER,
                String.valueOf(value),
                min,
                max));
    }

    public void action(String id, String label) {
        actions.add(new ModuleDebugAction(id, label));
    }

    public ModuleDebugScreen build(String moduleName, String title) {
        return new ModuleDebugScreen(moduleName, title, lines, fields, actions);
    }
}
