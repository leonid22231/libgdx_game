package com.lyadev.mygame.base;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Batch;
import java.util.function.Consumer;

public abstract class EntityModule {
    private static final ThreadLocal<EntityModule> initializingModule = new ThreadLocal<>();

    private Entity entity;
    private boolean enabled = false;
    private boolean runtimePaused = false;
    private String disabledReason;
    private Map<Class<? extends EntityModule>, EntityModule> dependencies = Map.of();
    private final List<Runnable> eventCleanups = new ArrayList<>();

    public final void attach(Entity entity) {
        this.entity = entity;
    }

    protected Entity getEntity() {
        return entity;
    }

    public abstract String getName();

    /**
     * Классы модулей, которые должны быть зарегистрированы и инициализированы раньше этого.
     * В {@link #init()} и далее используйте {@link #require(Class)} — зависимости уже привязаны.
     */
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return Collections.emptyList();
    }

    public List<String> getRequiredModuleNames() {
        List<String> names = new ArrayList<>();
        for(Class<? extends EntityModule> type : getRequiredModules()){
            names.add(type.getSimpleName());
        }
        return names;
    }

    protected final <T extends EntityModule> T require(Class<T> type) {
        EntityModule module = dependencies.get(type);
        if(module == null){
            throw new IllegalStateException(
                    getName() + " missing required module: " + type.getSimpleName());
        }
        return type.cast(module);
    }

    void bindDependencies(Map<Class<? extends EntityModule>, EntityModule> resolvedDependencies) {
        dependencies = Collections.unmodifiableMap(new HashMap<>(resolvedDependencies));
    }

    void clearDependencies() {
        dependencies = Map.of();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public boolean isRuntimePaused() {
        return runtimePaused;
    }

    public void setRuntimePaused(boolean runtimePaused) {
        this.runtimePaused = runtimePaused;
    }

    public boolean isRuntimeActive() {
        return enabled && !runtimePaused;
    }

    void resetModuleState() {
        enabled = false;
        runtimePaused = false;
        disabledReason = null;
    }

    void markEnabled() {
        enabled = true;
        disabledReason = null;
    }

    void markDisabled(String reason) {
        enabled = false;
        disabledReason = reason;
    }

    public void init() {
    }

    public void act(float delta) {
    }

    public void draw(Batch batch, float parentAlpha) {
    }

    public void dispose() {
        clearEventSubscriptions();
    }

    static void beginInit(EntityModule module) {
        initializingModule.set(module);
    }

    static void endInit() {
        initializingModule.remove();
    }

    /**
     * Модуль, который сейчас в {@link #init()}. Нужен для {@code otherModule.onSomething(handler)}
     * без явной передачи {@code this}.
     */
    protected static EntityModule currentInitializingModule() {
        EntityModule module = initializingModule.get();
        if(module == null){
            throw new IllegalStateException(
                    "Module event subscription is only allowed inside init() of another module");
        }
        return module;
    }

    /** Создать канал событий для публичных {@code onXxx(Consumer)} методов модуля. */
    protected final <E> ModuleEventChannel<E> createEventChannel() {
        return new ModuleEventChannel<>(this);
    }

    /** Подписаться на канал другого модуля из {@code init()} — явный {@code this}. */
    protected final <E> void subscribeTo(ModuleEventChannel<E> channel, Consumer<E> handler) {
        channel.subscribe(this, handler);
    }

    void registerEventCleanup(Runnable cleanup) {
        eventCleanups.add(cleanup);
    }

    protected final void clearEventSubscriptions() {
        for(Runnable cleanup : eventCleanups){
            cleanup.run();
        }
        eventCleanups.clear();
    }
}
