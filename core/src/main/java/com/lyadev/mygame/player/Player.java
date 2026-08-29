package com.lyadev.mygame.player;

import com.lyadev.mygame.base.Entity;
import com.lyadev.mygame.base.PlayableEntitySettings;
import com.lyadev.mygame.entity_modules.PlayableModules;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Entity {
    int health = 0;

    public Player(PlayableEntitySettings settings) {
        super(settings);
        PlayableModules.register(this);
    }
}
