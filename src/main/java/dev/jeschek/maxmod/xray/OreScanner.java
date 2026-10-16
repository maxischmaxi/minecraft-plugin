package dev.jeschek.maxmod.xray;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class OreScanner {

    public record Hit(BlockPos pos, int color) {
    }

    private OreScanner() {
    }

    // returns all enabled ores within a sphere around center
    public static List<Hit> scan(ClientLevel level, BlockPos center, int radius, Set<OreType> enabled) {
        List<Hit> found = new ArrayList<>();
        if (enabled.isEmpty()) {
            return found;
        }
        Predicate<BlockState> isOre = state -> enabled.contains(OreType.byBlock(state.getBlock()));
        long radiusSq = (long) radius * radius;

        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + radius);
        int minSectionY = Math.max(level.getMinSectionY(), SectionPos.blockToSectionCoord(center.getY() - radius));
        int maxSectionY = Math.min(level.getMaxSectionY(), SectionPos.blockToSectionCoord(center.getY() + radius));

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }

                for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
                    LevelChunkSection section = chunk.getSection(level.getSectionIndexFromSectionY(sectionY));
                    // the palette tells us cheaply whether the section can contain an ore at all
                    if (section.hasOnlyAir() || !section.getStates().maybeHas(isOre)) {
                        continue;
                    }

                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                BlockState state = section.getBlockState(x, y, z);
                                if (!isOre.test(state)) {
                                    continue;
                                }
                                pos.set(
                                        SectionPos.sectionToBlockCoord(chunkX, x),
                                        SectionPos.sectionToBlockCoord(sectionY, y),
                                        SectionPos.sectionToBlockCoord(chunkZ, z));
                                if (pos.distSqr(center) <= radiusSq) {
                                    found.add(new Hit(pos.immutable(), OreType.byBlock(state.getBlock()).color()));
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }
}
