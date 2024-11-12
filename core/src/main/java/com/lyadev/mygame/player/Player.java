package com.lyadev.mygame.player;

import com.lyadev.mygame.base.Entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Entity  {
    int health = 0;
    
    public Player(PlayerSettings settings) {
        super(settings);
    }
}
