package com.h14turkiye.xraysuspect.listener;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.h14turkiye.xraysuspect.packet.ChunkUtils;
import com.h14turkiye.xraysuspect.packet.PacketManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlockBreakListener implements Listener {
    
    private final Plugin plugin;
    private final Map<Player, Location> playerLastLocation = new HashMap<>();
    private final Map<Player, Map<String, Integer>> playerVeinCounts = new HashMap<>();
    private static final int DISTANCE_SQUARED_THRESHOLD = 256; // Example: 16 blocks squared
    
    public BlockBreakListener(Plugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("paper.antixray.bypass")) return;
        Location currentLocation = event.getBlock().getLocation();
        String blockTypeName = event.getBlock().getType().name().toLowerCase();
        
        // Get the "veins" configuration section from the plugin's config
        Set<String> veinKeys = plugin.getConfig().getConfigurationSection("veins").getKeys(false); // e.g. diamond_ore: 2, gold_ore: 1
        
        // Check if the block broken is a recognized vein type
        if (veinKeys.contains(blockTypeName)) {
            // Initialize player's vein count if not already done
            playerVeinCounts.putIfAbsent(player, new HashMap<>());
            Map<String, Integer> veinCounts = playerVeinCounts.get(player);
            
            // Update vein count for the specific block type
            veinCounts.put(blockTypeName, veinCounts.getOrDefault(blockTypeName, 0) + 1);
            
            // Check if the player has moved significantly
            Location lastLocation = playerLastLocation.get(player);
            boolean movedSignificantly = lastLocation == null || hasMovedSignificantly(lastLocation, currentLocation);
            
            if (movedSignificantly) {
                // Update the player's last known location
                playerLastLocation.put(player, currentLocation);
                
                
                
                // Check if the vein limit for this material is exceeded
                int veinLimit = plugin.getConfig().getInt("veins." + blockTypeName);
                if (veinCounts.get(blockTypeName) > veinLimit) {
                    String command = plugin.getConfig().getString("permission.revoke_command").replace("%player", player.getName()); // we revoke bypass permission
                    if (command != null && !command.isEmpty()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    }
                    
                    // we give the bypass perm again
                    int delay = plugin.getConfig().getInt("permission.give_delay_ticks");
                    if (delay > 0) {
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            if(!player.isOnline()) return; 
                            String command2 = plugin.getConfig().getString("permission.give_command").replace("%player", player.getName()); // we give bypass permission
                            if (command2 != null && !command2.isEmpty()) {
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command2);
                            }
                        }, delay);
                    }
                    
                    
                    if(plugin.getConfig().getBoolean("refresh-chunks"))
                    resendNearbyChunks(player);
                }
            }
        }
    }
    
    private boolean hasMovedSignificantly(Location lastLocation, Location currentLocation) {
        return (lastLocation.distanceSquared(currentLocation) > DISTANCE_SQUARED_THRESHOLD);
    }
    
    private void resendNearbyChunks(Player player) {
        Set<Chunk> nearbyChunks = ChunkUtils.getPlayerChunks(player);
        for (Chunk chunk : nearbyChunks) {
            Object chunkPacket = PacketManager.createChunkPacket(player, chunk);
            if (chunkPacket != null) {
                PacketManager.sendPacket(player, chunkPacket);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerLastLocation.remove(player);
        playerVeinCounts.remove(player);
        String command = plugin.getConfig().getString("permission.give_command").replace("%player", player.getName()); // we give bypass permission
        if (command != null && !command.isEmpty()) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
}
