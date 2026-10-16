package dev.jeschek.maxmod.xray;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

// x-ray section of the mod config
public final class XrayConfig {

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 64;
    public static final int MIN_INTENSITY = 5;
    public static final int MAX_INTENSITY = 100;

    public int radius = 24;
    // glow strength in percent
    public int intensity = 70;
    // ore id -> highlighted, missing entries count as enabled
    public Map<String, Boolean> ores = new LinkedHashMap<>();

    // fixes values from a hand edited or outdated config file
    public void normalize() {
        radius = Math.clamp(radius, MIN_RADIUS, MAX_RADIUS);
        intensity = Math.clamp(intensity, MIN_INTENSITY, MAX_INTENSITY);
        if (ores == null) {
            ores = new LinkedHashMap<>();
        }
        // write every ore into the file so it can be edited by hand too
        for (OreType type : OreType.values()) {
            ores.putIfAbsent(type.id(), true);
        }
    }

    public boolean isEnabled(OreType type) {
        return ores.getOrDefault(type.id(), true);
    }

    public void setEnabled(OreType type, boolean enabled) {
        ores.put(type.id(), enabled);
    }

    public Set<OreType> enabledOres() {
        Set<OreType> enabled = EnumSet.noneOf(OreType.class);
        for (OreType type : OreType.values()) {
            if (isEnabled(type)) {
                enabled.add(type);
            }
        }
        return enabled;
    }
}
