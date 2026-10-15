package dev.jeschek.xray;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class XrayConfig {

    public static final int MIN_RADIUS = 1;
    public static final int MAX_RADIUS = 64;
    public static final int MIN_INTENSITY = 5;
    public static final int MAX_INTENSITY = 100;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("xray.json");

    public int radius = 24;
    // glow strength in percent
    public int intensity = 70;
    // ore id -> highlighted, missing entries count as enabled
    public Map<String, Boolean> ores = new LinkedHashMap<>();

    public static XrayConfig load() {
        XrayConfig config = new XrayConfig();
        if (Files.exists(FILE)) {
            try {
                config = GSON.fromJson(Files.readString(FILE), XrayConfig.class);
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Could not read {}, using defaults", FILE, e);
            }
        }
        config.radius = Math.clamp(config.radius, MIN_RADIUS, MAX_RADIUS);
        config.intensity = Math.clamp(config.intensity, MIN_INTENSITY, MAX_INTENSITY);
        if (config.ores == null) {
            config.ores = new LinkedHashMap<>();
        }
        // write every ore into the file so it can be edited by hand too
        for (OreType type : OreType.values()) {
            config.ores.putIfAbsent(type.id(), true);
        }
        return config;
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

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", FILE, e);
        }
    }
}
