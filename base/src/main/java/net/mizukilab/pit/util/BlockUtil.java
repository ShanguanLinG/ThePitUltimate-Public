package net.mizukilab.pit.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Araykal
 * @since 2025/1/22
 */
public class BlockUtil {
    public static Set<Material> blockType = new ObjectOpenHashSet<>();

    static {
        blockType.add(Material.CACTUS);
        blockType.add(Material.ENDER_PORTAL_FRAME);
    }

    public static boolean isBlockNearby(Location location, int range) {
        World world = location.getWorld();
        int baseX = location.getBlockX();
        int baseY = location.getBlockY();
        int baseZ = location.getBlockZ();
        int chunkX = baseX >> 4;
        int chunkZ = baseZ >> 4;
        Chunk lastChunk = world.getChunkAt(chunkX, chunkZ);
        for (int x = -range; x <= range; x++) {
            int curX = (baseX + x);
            int curChunkX = curX >> 4;
            int xChunkIn = curX & 15;
            for (int y = -range; y <= range; y++) {
                int curY = (baseY + y);
                for (int z = -range; z <= range; z++) {
                    int curZ = (baseZ + z);
                    int curChunkZ = curZ >> 4;
                    if(curChunkZ == chunkX && curChunkZ == chunkZ) {

                    } else {
                        lastChunk = world.getChunkAt(chunkX = curChunkX, chunkZ = curChunkZ);
                    }
                    if(lastChunk != null && lastChunk.isLoaded()) {
                        if (blockType.contains(lastChunk.getBlock(xChunkIn, curY, curZ & 15).getType())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
