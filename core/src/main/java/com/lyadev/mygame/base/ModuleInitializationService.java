package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;

/**
 * Разрешает зависимости модулей по {@link Class} и инициализирует их в топологическом порядке:
 * если D требует C, C требует A и B — порядок init: A, B, C, D.
 */
public final class ModuleInitializationService {
    private static final String TAG = "ModuleInitializationService";

    private ModuleInitializationService() {
        throw new UnsupportedOperationException();
    }

    public static final class Result {
        private final List<EntityModule> activeModules;

        Result(List<EntityModule> activeModules) {
            this.activeModules = Collections.unmodifiableList(activeModules);
        }

        public List<EntityModule> getActiveModules() {
            return activeModules;
        }
    }

    public static Result resolveAndInitialize(Entity entity, List<EntityModule> registered) {
        for(EntityModule module : registered){
            module.resetModuleState();
            module.clearDependencies();
        }

        Map<Class<? extends EntityModule>, EntityModule> byClass = indexByClass(registered);
        Set<Class<? extends EntityModule>> activeClasses = collectActiveClasses(registered, byClass);
        logDisabledModules(entity, registered, byClass, activeClasses);

        List<EntityModule> initOrder = buildInitOrder(registered, byClass, activeClasses);
        List<EntityModule> activeModules = new ArrayList<>(initOrder.size());

        for(EntityModule module : initOrder){
            module.bindDependencies(buildDependencyMap(module, byClass, activeClasses));
            module.markEnabled();
            activeModules.add(module);
            ModuleInputRegistry.resolve(module);
            EntityModule.beginInit(module);
            try {
                module.init();
            } finally {
                EntityModule.endInit();
            }
        }

        return new Result(activeModules);
    }

    private static Map<Class<? extends EntityModule>, EntityModule> indexByClass(List<EntityModule> registered) {
        Map<Class<? extends EntityModule>, EntityModule> byClass = new HashMap<>();
        for(EntityModule module : registered){
            Class<? extends EntityModule> type = module.getClass();
            if(byClass.containsKey(type)){
                Gdx.app.error(TAG, "Duplicate module class: " + type.getSimpleName());
                continue;
            }
            byClass.put(type, module);
        }
        return byClass;
    }

    private static Set<Class<? extends EntityModule>> collectActiveClasses(
            List<EntityModule> registered,
            Map<Class<? extends EntityModule>, EntityModule> byClass) {
        Set<Class<? extends EntityModule>> activeClasses = new HashSet<>();
        boolean changed = true;
        while(changed){
            changed = false;
            for(EntityModule module : registered){
                Class<? extends EntityModule> type = module.getClass();
                if(activeClasses.contains(type)){
                    continue;
                }
                if(canActivate(module, byClass, activeClasses)){
                    activeClasses.add(type);
                    changed = true;
                }
            }
        }
        return activeClasses;
    }

    private static boolean canActivate(
            EntityModule module,
            Map<Class<? extends EntityModule>, EntityModule> byClass,
            Set<Class<? extends EntityModule>> activeClasses) {
        for(Class<? extends EntityModule> requiredType : module.getRequiredModules()){
            if(!byClass.containsKey(requiredType)){
                return false;
            }
            if(!activeClasses.contains(requiredType)){
                return false;
            }
        }
        return true;
    }

    private static void logDisabledModules(
            Entity entity,
            List<EntityModule> registered,
            Map<Class<? extends EntityModule>, EntityModule> byClass,
            Set<Class<? extends EntityModule>> activeClasses) {
        for(EntityModule module : registered){
            if(activeClasses.contains(module.getClass())){
                continue;
            }
            String reason = buildDisabledReason(module, byClass, activeClasses);
            module.markDisabled(reason);
            Gdx.app.debug(TAG, entity.getTAG() + " disabled module: " + module.getName() + " — " + reason);
        }
    }

    private static String buildDisabledReason(
            EntityModule module,
            Map<Class<? extends EntityModule>, EntityModule> byClass,
            Set<Class<? extends EntityModule>> activeClasses) {
        List<String> missing = new ArrayList<>();
        List<String> inactive = new ArrayList<>();

        for(Class<? extends EntityModule> requiredType : module.getRequiredModules()){
            if(!byClass.containsKey(requiredType)){
                missing.add(requiredType.getSimpleName());
                continue;
            }
            if(!activeClasses.contains(requiredType)){
                inactive.add(requiredType.getSimpleName());
            }
        }

        if(!missing.isEmpty()){
            return "missing: " + String.join(", ", missing);
        }
        if(!inactive.isEmpty()){
            return "inactive dependency: " + String.join(", ", inactive);
        }
        return "dependency check failed";
    }

    private static List<EntityModule> buildInitOrder(
            List<EntityModule> registered,
            Map<Class<? extends EntityModule>, EntityModule> byClass,
            Set<Class<? extends EntityModule>> activeClasses) {
        List<EntityModule> initOrder = new ArrayList<>();
        Set<Class<? extends EntityModule>> initialized = new HashSet<>();

        while(initOrder.size() < activeClasses.size()){
            boolean progress = false;
            for(EntityModule module : registered){
                Class<? extends EntityModule> type = module.getClass();
                if(!activeClasses.contains(type) || initialized.contains(type)){
                    continue;
                }
                if(dependenciesInitialized(module, initialized)){
                    initOrder.add(module);
                    initialized.add(type);
                    progress = true;
                }
            }
            if(!progress){
                Gdx.app.error(TAG, "Circular module dependency detected");
                break;
            }
        }
        return initOrder;
    }

    private static boolean dependenciesInitialized(
            EntityModule module,
            Set<Class<? extends EntityModule>> initialized) {
        for(Class<? extends EntityModule> requiredType : module.getRequiredModules()){
            if(!initialized.contains(requiredType)){
                return false;
            }
        }
        return true;
    }

    private static Map<Class<? extends EntityModule>, EntityModule> buildDependencyMap(
            EntityModule module,
            Map<Class<? extends EntityModule>, EntityModule> byClass,
            Set<Class<? extends EntityModule>> activeClasses) {
        Map<Class<? extends EntityModule>, EntityModule> dependencies = new HashMap<>();
        for(Class<? extends EntityModule> requiredType : module.getRequiredModules()){
            if(!activeClasses.contains(requiredType)){
                continue;
            }
            EntityModule dependency = byClass.get(requiredType);
            if(dependency != null){
                dependencies.put(requiredType, dependency);
            }
        }
        return dependencies;
    }
}
