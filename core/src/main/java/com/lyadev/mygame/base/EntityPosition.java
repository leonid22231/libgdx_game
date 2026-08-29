package com.lyadev.mygame.base;

import com.lyadev.mygame.utils.Position;
import com.lyadev.mygame.utils.Size;

public class EntityPosition extends Position{
    public static EntityPosition DEFAULT = new EntityPosition(0, 0);
    public EntityPosition(float x, float y) {
        super(x, y);
    }

    public Position getCenterPositionFromSize(Size size){
        return new Position(super.getX() + size.getWidth() / 2, super.getY() + size.getHeight() / 2);
    }
}
