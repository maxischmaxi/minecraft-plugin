package dev.jeschek.maxmod.durability;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Tool;

import java.util.List;

// adds durability and mining speed to item tooltips
public final class DurabilityTooltip {

    private DurabilityTooltip() {
    }

    public static void append(ItemStack stack, TooltipFlag flag, List<Component> lines) {
        // advanced tooltips (F3+H) already show it for damaged items
        if (stack.isDamageableItem() && !(flag.isAdvanced() && stack.isDamaged())) {
            lines.add(Component.translatable("maxmod.tooltip.durability", remaining(stack))
                    .withColor(stack.getBarColor()));
        }

        Tool tool = stack.get(DataComponents.TOOL);
        if (tool != null) {
            lines.add(Component.translatable("maxmod.tooltip.mining_speed", formatSpeed(miningSpeed(tool)))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    // "remaining/max"
    static String remaining(ItemStack stack) {
        return (stack.getMaxDamage() - stack.getDamageValue()) + "/" + stack.getMaxDamage();
    }

    // the best speed the tool reaches on any block, without efficiency
    private static float miningSpeed(Tool tool) {
        float speed = tool.defaultMiningSpeed();
        for (Tool.Rule rule : tool.rules()) {
            speed = Math.max(speed, rule.speed().orElse(0f));
        }
        return speed;
    }

    private static String formatSpeed(float speed) {
        return speed == (int) speed ? Integer.toString((int) speed) : Float.toString(speed);
    }
}
