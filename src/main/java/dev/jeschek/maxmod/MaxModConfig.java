package dev.jeschek.maxmod;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import dev.jeschek.maxmod.durability.DurabilityConfig;
import dev.jeschek.maxmod.xray.XrayConfig;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// persisted settings, one section per feature
public final class MaxModConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("maxmod.json");

    public XrayConfig xray = new XrayConfig();
    public DurabilityConfig durability = new DurabilityConfig();

    public static MaxModConfig load() {
        MaxModConfig config = null;
        if (Files.exists(FILE)) {
            try {
                config = GSON.fromJson(Files.readString(FILE), MaxModConfig.class);
            } catch (IOException | RuntimeException e) {
                LOGGER.warn("Could not read {}, using defaults", FILE, e);
            }
        }
        if (config == null) {
            config = new MaxModConfig();
        }
        if (config.xray == null) {
            config.xray = new XrayConfig();
        }
        if (config.durability == null) {
            config.durability = new DurabilityConfig();
        }
        config.xray.normalize();
        return config;
    }

    public void save() {
        try {
            Files.writeString(FILE, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", FILE, e);
        }
    }
}
