package com.lyadev.mygame.modules.door;

import lombok.Getter;

/** Target world id + spawn cell for {@link DoorModule}. */
@Getter
public final class DoorSettings {
    private final String targetWorldId;
    private final int targetCol;
    private final int targetRow;

    public DoorSettings(String targetWorldId, int targetCol, int targetRow) {
        this.targetWorldId = targetWorldId;
        this.targetCol = targetCol;
        this.targetRow = targetRow;
    }
}
