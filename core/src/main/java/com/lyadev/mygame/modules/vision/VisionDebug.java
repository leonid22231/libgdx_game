package com.lyadev.mygame.modules.vision;

import java.util.ArrayList;
import java.util.List;

/** Публичный debug-API пакета vision (для MyLogger / debug console). */
public final class VisionDebug {
    private VisionDebug() {
        throw new UnsupportedOperationException();
    }

    public static int getActiveModuleCount() {
        return VisionRegistry.count();
    }

    public static List<String> collectBackgroundThreadLines() {
        List<String> lines = new ArrayList<>();
        for(VisionModule module : VisionRegistry.snapshot()){
            VisionBackgroundThread thread = module.getBackgroundThreadForDebug();
            if(thread == null){
                continue;
            }
            if(thread.isRunning()){
                lines.add(String.format("  Vision[%s] running %ss", thread.getName(), thread.getRuntimeSeconds()));
            } else {
                lines.add(String.format("  Vision[%s] stopped", thread.getName()));
            }
        }
        return lines;
    }
}
