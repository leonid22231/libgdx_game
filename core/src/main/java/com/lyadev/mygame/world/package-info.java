package com.lyadev.mygame.world;

/**
 * World runtime infrastructure (not concrete maps).
 *
 * <ul>
 *   <li>{@link WorldEntity} / {@link JsonMapWorld} — world base</li>
 *   <li>{@link WorldController} — registry + travel / switch</li>
 *   <li>{@link GlobalWorld} — stage bootstrap facade</li>
 *   <li>{@code topdown/}, {@code tiles/}, {@code props/}, {@code doors/} — map tooling</li>
 * </ul>
 *
 * <p>Concrete playable maps: {@code com.lyadev.mygame.worlds}.
 */
