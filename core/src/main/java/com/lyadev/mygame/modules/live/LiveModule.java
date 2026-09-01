package com.lyadev.mygame.modules.live;

import com.lyadev.mygame.base.entity.EntityModule;
import com.lyadev.mygame.debug.ModuleDebugPanel;

public class LiveModule extends EntityModule {
    private double health = 100;
    private LiveStatus status = LiveStatus.live;

    @Override
    public String getName() {
        return "live_module";
    }

    @Override
    public void act(float delta) {
        if(health < 0) health = 0;
        if(health == 0) status = LiveStatus.death; else status = LiveStatus.live;
    }

    @Override
    public void populateDebugScreen(ModuleDebugPanel panel) {
        super.populateDebugScreen(panel);
        if(!isEnabled()){
            return;
        }
        panel.line("status", status.name());
    }

    public void hit(double damage){
        health-=damage;
    }
}
