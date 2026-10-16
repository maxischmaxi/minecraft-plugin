package dev.jeschek.maxmod.xray;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class OreRenderer {

    // fill is drawn weaker than the outline so the ore texture stays readable
    private static final float FILL_FACTOR = 0.4f;

    private OreRenderer() {
    }

    // must run inside a gizmo collector, e.g. during the client tick.
    // always-on-top gizmos skip the depth test, so they show through walls
    public static void draw(List<OreScanner.Hit> ores, int intensityPercent) {
        float intensity = intensityPercent / 100f;

        Map<Integer, GizmoStyle> styles = new HashMap<>();
        for (OreScanner.Hit hit : ores) {
            GizmoStyle style = styles.computeIfAbsent(hit.color(), rgb -> GizmoStyle.strokeAndFill(
                    // shader packs (iris) drop translucent lines, so the outline stays opaque and gets darker instead
                    ARGB.scaleRGB(ARGB.opaque(rgb), intensity),
                    2.5f,
                    // translucent fill, only visible without shaders
                    ARGB.color(intensity * FILL_FACTOR, rgb)));
            Gizmos.cuboid(hit.pos(), 0.002f, style).setAlwaysOnTop();
        }
    }
}
