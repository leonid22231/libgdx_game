package com.lyadev.mygame.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Снимок debug-панели модуля для Swing UI (собирается на game thread). */
public final class ModuleDebugScreen {
    public final String moduleName;
    public final String title;
    public final List<ModuleDebugLine> lines;
    public final List<ModuleDebugField> fields;
    public final List<ModuleDebugAction> actions;

    public ModuleDebugScreen(
            String moduleName,
            String title,
            List<ModuleDebugLine> lines,
            List<ModuleDebugField> fields,
            List<ModuleDebugAction> actions) {
        this.moduleName = moduleName;
        this.title = title;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
        this.fields = Collections.unmodifiableList(new ArrayList<>(fields));
        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
    }

    public static ModuleDebugScreen empty(String moduleName, String message) {
        return new ModuleDebugScreen(
                moduleName,
                moduleName,
                List.of(new ModuleDebugLine("info", message)),
                List.of(),
                List.of());
    }

    public String signature() {
        StringBuilder builder = new StringBuilder();
        builder.append(title).append('|');
        for(ModuleDebugLine line : lines){
            builder.append(line.key).append('=').append(line.value).append(';');
        }
        for(ModuleDebugField field : fields){
            builder.append(field.id)
                    .append(':')
                    .append(field.type)
                    .append('=')
                    .append(field.value)
                    .append(';');
        }
        for(ModuleDebugAction action : actions){
            builder.append(action.id).append(':').append(action.label).append(';');
        }
        return builder.toString();
    }
}
