package com.lyadev.mygame.modules.example;

import java.util.List;
import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;

import com.lyadev.mygame.base.entity.Entity;
import com.lyadev.mygame.base.entity.EntityModule;
import com.lyadev.mygame.base.entity.ModuleEventChannel;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.vision.EntitySpottedEvent;
import com.lyadev.mygame.modules.vision.VisionModule;

/**
 * Справочный модуль — <b>не подключать в игру</b>, только читать как шпаргалку.
 * Живой пример всего API: {@link com.lyadev.mygame.modules.ai.AiBrainModule}.
 *
 * <p>Полная документация: {@code modules/example/README.md}
 */
public class ExampleModule extends EntityModule {

    // =========================================================================
    // 1. ИМЯ И НАСТРОЙКИ
    // =========================================================================
    // Папка: modules/example/
    //   ExampleModule.java      — поведение (extends EntityModule)
    //   ExampleSettings.java    — immutable конфиг (если нужен конструктору)
    //   ExampleEvent.java       — payload для onExampleEvent(...)
    //
    // Конфиг НЕ кладётся на Entity. При spawn:
    //   entity.registerModule(new ExampleModule(new ExampleSettings(...)));
    // =========================================================================

    private static final String LOG_TAG = "ExampleModule";

    // private final ExampleSettings settings;  // если модулю нужен конфиг из blueprint

    // =========================================================================
    // 2. СВОИ СОБЫТИЯ (издатель)
    // =========================================================================
    // createEventChannel() — из EntityModule. Публикуй только если hasSubscribers().
    // =========================================================================

    private final ModuleEventChannel<ExampleEvent> exampleEvents = createEventChannel();

    // =========================================================================
    // 3. RUNTIME-STATE (private поля модуля)
    // =========================================================================
    // Вся логика и state — внутри пакета modules/example/. Снаружи — только public API.
    // =========================================================================

    private float exampleEventTimer;

    @Override
    public String getName() {
        return "example_module";
    }

    // =========================================================================
    // 4. ЗАВИСИМОСТИ (объявить Class, использовать require)
    // =========================================================================
    // getRequiredModules() — кого должно быть на entity ДО init этого модуля.
    // ModuleInitializationService отсортирует: Texture → Sprite → Movement → Vision → Example.
    // Если VisionModule нет на entity — ExampleModule не активируется (disabled + log).
    // =========================================================================

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(VisionModule.class, MovementModule.class);
    }

    // =========================================================================
    // 4b. КЛАВИШИ (объявить как getRequiredModules, реализовать в onKeyBinding)
    // =========================================================================
    // getKeyBindings() → ModuleInputRegistry.resolve(this) вместе с deps.
    // Описание видно в Debug Console → Keys / команда `keys`.
    // =========================================================================

    @Override
    public List<com.lyadev.mygame.base.entity.ModuleKeyDecl> getKeyBindings() {
        // Пример (не активен в игре — модуль не подключают):
        // return List.of(
        //     ModuleKeyDecl.down("ping", ModuleKeyChord.of(Keys.P), "Example ping"));
        return List.of();
    }

    @Override
    protected void onKeyBinding(String actionId, com.lyadev.mygame.base.entity.ModuleKeyEvent event) {
        // switch(actionId) { case "ping": ... }
    }

    // =========================================================================
    // 5. init() — один раз после resolveModules()
    // =========================================================================
    // Здесь: подписки на события, кеш require(...), старт потоков.
    // Подписка на onEntitySpotted — ТОЛЬКО в init() (currentInitializingModule()).
    //
    // ВАЖНО: registerModule(example) ДО entity.resolveModules(), иначе init() не будет!
    //
    //   PlayableModules.registerModules(entity, blueprint);
    //   entity.registerModule(new ExampleModule());
    //   entity.resolveModules();
    // =========================================================================

    @Override
    public void init() {
        VisionModule vision = require(VisionModule.class);
        MovementModule movement = require(MovementModule.class);

        vision.onEntitySpotted(this::handleEntitySpotted);

        // Альтернатива без require — если deps не объявлены (не рекомендуется):
        // VisionModule vision = VisionModule.from(getEntity());

        Gdx.app.debug(LOG_TAG, getEntity().getTAG() + " init, movement=" + movement.getName());
    }

    // =========================================================================
    // 6. ПУБЛИЧНЫЙ API ДЛЯ ДРУГИХ МОДУЛЕЙ
    // =========================================================================
    // Fluent-подписка. Вызывается из init() другого модуля:
    //   require(ExampleModule.class).onExampleEvent(e -> ...);
    // Отписка автоматическая при dispose() подписчика.
    // =========================================================================

    public void onExampleEvent(Consumer<ExampleEvent> handler) {
        exampleEvents.subscribe(currentInitializingModule(), handler);
    }

    public static ExampleModule from(Entity entity) {
        return entity.getModule(ExampleModule.class);
    }

    // =========================================================================
    // 7. act(delta) — каждый кадр на main thread (Entity.act → module.act)
    // =========================================================================
    // Вызывать методы других модулей через require() или Module.from(entity):
    //   require(MovementModule.class).moveLeftToggle();
    //   SelectableModule.from(getEntity()).isActive();
    //
    // Не вызывать тяжёлую логику из чужих фоновых потоков — события vision
    // уже приходят на main thread через ModuleEventChannel.publish().
    // =========================================================================

    @Override
    public void act(float delta) {
        exampleEvents.flushPending();

        if(!isRuntimeActive()){
            return;
        }

        if(exampleEvents.hasSubscribers()){
            exampleEventTimer += delta;
            if(exampleEventTimer >= 5f){
                exampleEventTimer = 0f;
                exampleEvents.publish(new ExampleEvent(getEntity(), "example tick"));
            }
        }

        // Пример прямого вызова API другого модуля (раскомментируй для эксперимента):
        // if (SelectableModule.from(getEntity()).isActive()) {
        //     require(MovementModule.class).stopMoving();
        // }
    }

    // =========================================================================
    // 8. draw(batch) — world pass (камера мира)
    // =========================================================================
    // Не делайте batch.end()/ShapeRenderer на каждого entity — общий проход снаружи.
    // =========================================================================

    @Override
    public void draw(Batch batch, float parentAlpha) {
        // World debug — см. VisionDebug.drawOverlays
    }

    // =========================================================================
    // 8b. drawUi(batch) — screen HUD (после мира, см. docs/modules/ui-layer.md)
    // =========================================================================

    @Override
    public void drawUi(Batch batch, float parentAlpha) {
        // Inventory / HP bar — пиксели экрана, камера не влияет
    }

    // =========================================================================
    // 9. dispose() — entity.dispose() → module.dispose()
    // =========================================================================
    // super.dispose() снимает все подписки, где ЭТОТ модуль был подписчиком.
    // Издатель: exampleEvents.clear() если нужно.
    // =========================================================================

    @Override
    public void dispose() {
        exampleEvents.clear();
        super.dispose();
    }

    // =========================================================================
    // 10. ОБРАБОТЧИКИ СОБЫТИЙ (private)
    // =========================================================================
    // onEntitySpotted — «Я (observer) увидел target». Скан vision только у active entity.
    // «Меня увидели» — отдельного события пока нет (будущий onSpottedBy).
    // =========================================================================

    private void handleEntitySpotted(EntitySpottedEvent event) {
        Gdx.app.debug(
                LOG_TAG,
                event.getObserver().getTAG() + " → spotted → " + event.getTarget().getTAG());
    }
}
