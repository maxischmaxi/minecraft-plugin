package dev.jeschek.maxmod.xray;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// one toggleable ore kind with its highlight color (rgb), covering all block variants
public enum OreType {
    COAL("coal", 0x808080, Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
    COPPER("copper", 0xE0773C, Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
    IRON("iron", 0xD8AF93, Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
    GOLD("gold", 0xFFD700, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE, Blocks.NETHER_GOLD_ORE),
    REDSTONE("redstone", 0xFF2020, Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
    LAPIS("lapis", 0x2050FF, Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
    DIAMOND("diamond", 0x00FFFF, Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
    EMERALD("emerald", 0x00E060, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
    QUARTZ("quartz", 0xF0F0F0, Blocks.NETHER_QUARTZ_ORE),
    ANCIENT_DEBRIS("ancient_debris", 0xA0522D, Blocks.ANCIENT_DEBRIS);

    private static final Map<Block, OreType> BY_BLOCK = new HashMap<>();

    static {
        for (OreType type : values()) {
            for (Block block : type.blocks) {
                BY_BLOCK.put(block, type);
            }
        }
    }

    private final String id;
    private final int color;
    private final List<Block> blocks;

    OreType(String id, int color, Block... blocks) {
        this.id = id;
        this.color = color;
        this.blocks = List.of(blocks);
    }

    public static @Nullable OreType byBlock(Block block) {
        return BY_BLOCK.get(block);
    }

    // key used in the config file
    public String id() {
        return id;
    }

    public int color() {
        return color;
    }

    // vanilla translation key of the main block, so names come localized for free
    public String translationKey() {
        return blocks.getFirst().getDescriptionId();
    }
}
