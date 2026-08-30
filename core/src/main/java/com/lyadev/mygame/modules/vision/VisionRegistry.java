package com.lyadev.mygame.modules.vision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Внутренний реестр vision-модулей. Не GlobalWorld — только пакет vision.
 */
final class VisionRegistry {
    private static final CopyOnWriteArrayList<VisionModule> modules = new CopyOnWriteArrayList<>();

    private VisionRegistry() {
        throw new UnsupportedOperationException();
    }

    static void register(VisionModule module) {
        if(module != null && !modules.contains(module)){
            modules.add(module);
        }
    }

    static void unregister(VisionModule module) {
        modules.remove(module);
    }

    static List<VisionModule> peers(VisionModule self) {
        List<VisionModule> peers = new ArrayList<>();
        for(VisionModule module : modules){
            if(module != self){
                peers.add(module);
            }
        }
        return peers;
    }

    static int count() {
        return modules.size();
    }

    static List<VisionModule> snapshot() {
        return Collections.unmodifiableList(new ArrayList<>(modules));
    }
}
