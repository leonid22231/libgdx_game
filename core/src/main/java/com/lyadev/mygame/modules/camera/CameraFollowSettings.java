package com.lyadev.mygame.modules.camera;

import lombok.Getter;

/**
 * Follow speed and activate snap for {@link CameraFollowModule}.
 */
@Getter
public final class CameraFollowSettings {
    public static final CameraFollowSettings DEFAULT = new CameraFollowSettings(8f, true);

    /** Higher = snappier chase (exponential smoothing). */
    private final float followSpeed;
    /** Jump to target once when entity becomes active. */
    private final boolean snapOnActivate;

    public CameraFollowSettings(float followSpeed, boolean snapOnActivate) {
        this.followSpeed = followSpeed;
        this.snapOnActivate = snapOnActivate;
    }
}
