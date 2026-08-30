package com.lyadev.mygame.modules.ai;

import java.util.List;

import com.badlogic.gdx.Gdx;

import com.lyadev.mygame.base.EntityModule;
import com.lyadev.mygame.modules.movement.MovementModule;
import com.lyadev.mygame.modules.vision.VisionModule;

/** Тестовый AI-модуль. Подключается только там, где нужен (см. GlobalWorld — man). */
public class AiBrainModule extends EntityModule {
    @Override
    public String getName() {
        return "ai_brain_module";
    }

    @Override
    public List<Class<? extends EntityModule>> getRequiredModules() {
        return List.of(VisionModule.class, MovementModule.class);
    }

    @Override
    public void init() {
        require(VisionModule.class).onEntitySpotted(this, event ->
        {
            Gdx.app.log(
                getName(),
                getEntity().getTAG()
                    + " spotted "
                    + event.getTarget().getTAG());
            MovementModule targetModule = event.getTarget().getModule(MovementModule.class);
            if(targetModule != null){
                targetModule.rotate();
            }

        });
    }
}
