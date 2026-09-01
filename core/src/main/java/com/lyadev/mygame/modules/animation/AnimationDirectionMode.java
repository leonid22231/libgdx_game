package com.lyadev.mygame.modules.animation;

/** Как AnimationModule выбирает row / flip для facing. */
public enum AnimationDirectionMode {
    /** Sheet с 4 строками (up, right, left, down) — newgirl. */
    FOUR_DIRECTIONS,
    /** Одна строка: вправо как в sheet, влево — horizontal flip — cat. */
    MIRROR_HORIZONTAL
}
