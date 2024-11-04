package com.h14turkiye.xraysuspect.packet;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class ChunkUtils {
    
    public static Set<Chunk> getPlayerChunks(Player player) {
        Set<Chunk> chunks = new HashSet<>();
        Chunk playerChunk = player.getLocation().getChunk();
        int renderDistance = Bukkit.getServer().getViewDistance();
        
        int startX = playerChunk.getX() - renderDistance;
        int startZ = playerChunk.getZ() - renderDistance;
        int endX = playerChunk.getX() + renderDistance;
        int endZ = playerChunk.getZ() + renderDistance;

        for (int x = startX; x <= endX; x++) {
            for (int z = startZ; z <= endZ; z++) {
                chunks.add(player.getWorld().getChunkAt(x, z));
            }
        }

        return chunks;
    }
}
