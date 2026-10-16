package dev.jeschek.maxmod;

import dev.jeschek.maxmod.durability.Durability;
import dev.jeschek.maxmod.sort.InventorySorting;
import dev.jeschek.maxmod.xray.Xray;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class MaxModClient implements ClientModInitializer {

    public static final String MOD_ID = "maxmod";

    private static MaxModConfig config;

    public static MaxModConfig config() {
        return config;
    }

    @Override
    public void onInitializeClient() {
        config = MaxModConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, MOD_ID));
        new Xray(config).register(category);
        new InventorySorting().register(category);
        new Durability(config).register();
    }
}
