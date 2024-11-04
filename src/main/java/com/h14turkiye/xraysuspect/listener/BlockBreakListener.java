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
import java.util.concurrent.ConcurrentHashMap;

public class BlockBreakListener implements Listener {
    
    private final Plugin plugin;
    private final Map<Player, Location> playerLastLocation = new ConcurrentHashMap<>();
    private final Map<Player, Map<String, Integer>> playerVeinCounts = new ConcurrentHashMap<>();
    private final Map<Player, Long> lastInteractionTimes = new ConcurrentHashMap<>();
    private static final int DISTANCE_SQUARED_THRESHOLD = 256; // 16 blocks squared
    
    private final Set<String> veinKeys;
    private final String giveCommand;
    private final String revokeCommand;
    private final int giveDelay;
    private final boolean refreshChunks;
    private final int resetCountInactivityDelay;
    
    public BlockBreakListener(Plugin plugin) {
        this.plugin = plugin;
        
        // Caching configuration values
        this.veinKeys = plugin.getConfig().getConfigurationSection("veins").getKeys(false);
        this.giveCommand = plugin.getConfig().getString("permission.command.give");
        this.revokeCommand = plugin.getConfig().getString("permission.command.revoke");
        this.giveDelay = plugin.getConfig().getInt("suspect-phase-time");
        this.refreshChunks = plugin.getConfig().getBoolean("refresh-chunks");
        this.resetCountInactivityDelay = plugin.getConfig().getInt("reset-count-inactivity-delay", 600);
        
        startInactivityResetTask();
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()-> {
            Player player = event.getPlayer();
            if (player.hasPermission("paper.antixray.bypass")) return;
            
            Location currentLocation = event.getBlock().getLocation();
            String blockTypeName = event.getBlock().getType().name().toLowerCase();
            
            // Check if the block broken is a recognized vein type
            if (veinKeys.contains(blockTypeName)) {
                // Initialize player's vein count if not already done
                playerVeinCounts.putIfAbsent(player, new HashMap<>());
                Map<String, Integer> veinCounts = playerVeinCounts.get(player);
                
                // Update last interaction time
                lastInteractionTimes.put(player, System.currentTimeMillis());
                
                // Check if the player has moved significantly
                Location lastLocation = playerLastLocation.get(player);
                boolean movedSignificantly = lastLocation == null || hasMovedSignificantly(lastLocation, currentLocation);
                
                if (movedSignificantly) { 
                    // Update vein count for the specific block type
                    veinCounts.put(blockTypeName, veinCounts.getOrDefault(blockTypeName, 0) + 1);
                    
                    playerLastLocation.put(player, currentLocation);
                    
                    int veinLimit = plugin.getConfig().getInt("veins." + blockTypeName);
                    if (veinCounts.get(blockTypeName) > veinLimit) {
                        executeCommand(revokeCommand, player);
                        
                        // Schedule permission reapplication
                        if (giveDelay > 0) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                if (player.isOnline()) {
                                    executeCommand(giveCommand, player);
                                }
                            }, giveDelay);
                        }
                        
                        if (refreshChunks) {
                            resendNearbyChunks(player);
                        }
                    }
                }
            }
        });
    }
    
    private boolean hasMovedSignificantly(Location lastLocation, Location currentLocation) {
        return (lastLocation.distanceSquared(currentLocation) > DISTANCE_SQUARED_THRESHOLD);
    }
    
    private void executeCommand(String commandTemplate, Player player) {
        if (commandTemplate != null && !commandTemplate.isEmpty()) {
            String command = commandTemplate.replace("%player", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
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
    
    private void startInactivityResetTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::resetInactivePlayers, 0L, 20L * 60); // Run every minute
    }
    
    private void resetInactivePlayers() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<Player, Long> entry : lastInteractionTimes.entrySet()) {
            Player player = entry.getKey();
            long lastInteractionTime = entry.getValue();
            
            if (currentTime - lastInteractionTime >= resetCountInactivityDelay * 50L) { // delay in ticks to milliseconds
                playerVeinCounts.remove(player);
                playerLastLocation.remove(player);
                lastInteractionTimes.remove(player);
            }
        }
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerLastLocation.remove(player);
        playerVeinCounts.remove(player);
        lastInteractionTimes.remove(player);
        executeCommand(giveCommand, player);
    }
}
