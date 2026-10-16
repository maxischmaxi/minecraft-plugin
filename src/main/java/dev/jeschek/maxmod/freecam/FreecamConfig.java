package dev.jeschek.maxmod.freecam;

// freecam section of the mod config
public final class FreecamConfig {

    public static final int MIN_SPEED = 1;
    public static final int MAX_SPEED = 100;

    // blocks per second
    public int speed = 10;
    public boolean exitOnDamage = true;

    public void normalize() {
        speed = Math.clamp(speed, MIN_SPEED, MAX_SPEED);
    }
}
