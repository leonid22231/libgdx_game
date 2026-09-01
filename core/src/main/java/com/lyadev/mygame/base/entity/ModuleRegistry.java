package com.lyadev.mygame.base.entity;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

public class ModuleRegistry {
    private static final String TAG = "ModuleRegistry";

    private final Entity entity;
    private final ArrayList<EntityModule> registered = new ArrayList<>();
    private final ArrayList<EntityModule> active = new ArrayList<>();

    public ModuleRegistry(Entity entity) {
        this.entity = entity;
    }

    public void register(EntityModule module) {
        for(EntityModule existing : registered){
            if(existing.getName().equals(module.getName())){
                Gdx.app.error(TAG, "Duplicate module name: " + module.getName());
                return;
            }
            if(existing.getClass().equals(module.getClass())){
                Gdx.app.error(TAG, "Duplicate module class: " + module.getClass().getSimpleName());
                return;
            }
        }
        module.attach(entity);
        registered.add(module);
    }

    public void resolveAndInit() {
        active.clear();
        ModuleInitializationService.Result result = ModuleInitializationService.resolveAndInitialize(entity, registered);
        active.addAll(result.getActiveModules());
    }

    public List<EntityModule> getActiveModules() {
        return active;
    }

    public List<EntityModule> getRegisteredModules() {
        return registered;
    }
}
