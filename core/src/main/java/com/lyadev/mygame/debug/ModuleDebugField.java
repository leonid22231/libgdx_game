package com.lyadev.mygame.debug;

public final class ModuleDebugField {
    public final String id;
    public final String label;
    public final ModuleDebugFieldType type;
    public final String value;
    public final Double min;
    public final Double max;

    public ModuleDebugField(
            String id,
            String label,
            ModuleDebugFieldType type,
            String value,
            Double min,
            Double max) {
        this.id = id;
        this.label = label;
        this.type = type;
        this.value = value == null ? "" : value;
        this.min = min;
        this.max = max;
    }
}
