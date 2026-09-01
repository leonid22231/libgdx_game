package com.lyadev.mygame.base.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.graphics.g2d.Batch;

import com.lyadev.mygame.debug.ModuleDebugPanel;

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

    /**
     * Declarative key / chord list (like {@link #getRequiredModules()}).
     * Resolved together by {@link ModuleInputRegistry#resolve(EntityModule)}.
     * Handle in {@link #onKeyBinding(String, ModuleKeyEvent)}.
     */
    public List<ModuleKeyDecl> getKeyBindings() {
        return Collections.emptyList();
    }

    /**
     * Implementation for actions declared in {@link #getKeyBindings()}.
     * {@code actionId} is the local id from the decl (without module name prefix).
     */
    protected void onKeyBinding(String actionId, ModuleKeyEvent event) {
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

    /**
     * Screen-space UI (HUD). Called after the world pass with screen projection —
     * not affected by the world camera. Override for inventory, HP bars, etc.
     */
    public void drawUi(Batch batch, float parentAlpha) {
    }

    public void dispose() {
        ModuleInputRegistry.unregisterModule(this);
        clearEventSubscriptions();
    }

    /**
     * Debug Console: строки и кнопки для вкладки Entities (клик по модулю).
     * Вызывается на game thread.
     */
    public void populateDebugScreen(ModuleDebugPanel panel) {
        panel.line("module", getName());
        panel.line("enabled", isEnabled());
        panel.line("runtimeActive", isRuntimeActive());
        if(!isEnabled() && disabledReason != null){
            panel.line("disabledReason", disabledReason);
        }
        panel.checkbox("runtime_paused", "Runtime paused", isRuntimePaused());
    }

    /**
     * Debug Console: чекбоксы и поля ввода из {@link #populateDebugScreen(ModuleDebugPanel)}.
     * @return сообщение для UI или {@code null}, если field не обработан
     */
    public String handleDebugFieldChange(String fieldId, String value) {
        if("runtime_paused".equals(fieldId)){
            if(!isEnabled()){
                return "Module is disabled at resolve";
            }
            setRuntimePaused(Boolean.parseBoolean(value));
            return isRuntimePaused() ? "Runtime paused" : "Runtime resumed";
        }
        return null;
    }

    /**
     * Debug Console: обработка кнопок из {@link #populateDebugScreen(ModuleDebugPanel)}.
     * @return сообщение для UI или {@code null}, если action не обработан
     */
    public String handleDebugAction(String actionId) {
        if("toggle_pause".equals(actionId)){
            if(!isEnabled()){
                return "Module is disabled at resolve";
            }
            setRuntimePaused(!isRuntimePaused());
            return isRuntimePaused() ? "Runtime paused" : "Runtime resumed";
        }
        return null;
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
