package com.h14turkiye.xraysuspect.listener;

import org.bukkit.Bukkit;
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
    private final Map<Player, Location> playerLastMineLocation = new ConcurrentHashMap<>();
    private final Map<Player, Map<String, Integer>> playerOreCounts = new ConcurrentHashMap<>();
    private final Map<Player, Long> lastMiningTimes = new ConcurrentHashMap<>();
    
    private final Set<String> oreTypes;
    private final String permissionGrantCommand;
    private final String permissionRevokeCommand;
    private final int permissionGrantDelay;
    private final boolean refreshChunkPackets;
    private final int resetOreCountDelay;
    private static final int LOCATION_SQUARED_THRESHOLD = 256;
    
    public BlockBreakListener(Plugin plugin) {
        this.plugin = plugin;
        this.oreTypes = plugin.getConfig().getConfigurationSection("ores").getKeys(false);
        this.permissionGrantCommand = plugin.getConfig().getString("permission.command.give");
        this.permissionRevokeCommand = plugin.getConfig().getString("permission.command.revoke");
        this.permissionGrantDelay = plugin.getConfig().getInt("suspect-phase-time");
        this.refreshChunkPackets = plugin.getConfig().getBoolean("refresh-chunks");
        this.resetOreCountDelay = plugin.getConfig().getInt("reset-count-inactivity-delay", 600);
        startInactivityResetTask();
    }
    
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> handleMining(event));
    }
    
    private void handleMining(BlockBreakEvent event) {
        Player player = event.getPlayer();
        
        if (player.hasPermission("paper.antixray.bypass")) return;
        
        Location currentLocation = event.getBlock().getLocation();
        String blockTypeName = event.getBlock().getType().name().toLowerCase();
        
        if (oreTypes.contains(blockTypeName)) {
            updateOreCount(player, blockTypeName, currentLocation);
        }
    }
    
    private void updateOreCount(Player player, String blockTypeName, Location currentLocation) {
        playerOreCounts.putIfAbsent(player, new HashMap<>());
        Map<String, Integer> oreCounts = playerOreCounts.get(player);
        lastMiningTimes.put(player, System.currentTimeMillis());
        
        if (hasPlayerMovedFromLastMineLocation(player, currentLocation)) {
            incrementOreCount(player, blockTypeName, oreCounts);
            playerLastMineLocation.put(player, currentLocation);
        }
    }
    
    private void incrementOreCount(Player player, String blockTypeName, Map<String, Integer> oreCounts) {
        oreCounts.put(blockTypeName, oreCounts.getOrDefault(blockTypeName, 0) + 1);
        
        int oreLimit = plugin.getConfig().getInt("ores." + blockTypeName);
        if (oreCounts.get(blockTypeName) > oreLimit) {
            executeCommand(permissionRevokeCommand, player);
            schedulePermissionRegrant(player);
            if (refreshChunkPackets) resendNearbyChunks(player);
        }
    }
    
    private boolean hasPlayerMovedFromLastMineLocation(Player player, Location currentLocation) {
        Location lastLocation = playerLastMineLocation.get(player);
        return lastLocation == null || lastLocation.distanceSquared(currentLocation) > LOCATION_SQUARED_THRESHOLD;
    }
    
    private void executeCommand(String commandTemplate, Player player) {
        if (commandTemplate != null && !commandTemplate.isEmpty()) {
            String command = commandTemplate.replace("%player", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }
    
    private void schedulePermissionRegrant(Player player) {
        if (permissionGrantDelay > 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (player.isOnline()) {
                    executeCommand(permissionGrantCommand, player);
                }
            }, permissionGrantDelay);
        }
    }
    
    private void resendNearbyChunks(Player player) {
        ChunkUtils.getPlayerChunks(player).forEach(chunk -> {
            Object chunkPacket = PacketManager.createChunkPacket(player, chunk);
            if (chunkPacket != null) {
                PacketManager.sendPacket(player, chunkPacket);
            }
        });
    }
    
    private void startInactivityResetTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::resetInactivePlayers, 0L, 20L * 60);
    }
    
    private void resetInactivePlayers() {
        long currentTime = System.currentTimeMillis();
        lastMiningTimes.forEach((player, lastTime) -> {
            if (currentTime - lastTime >= resetOreCountDelay * 50L) {
                playerOreCounts.remove(player);
                playerLastMineLocation.remove(player);
                lastMiningTimes.remove(player);
            }
        });
    }
    
    @EventHandler
    public void onPlayerDisconnect(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        clearPlayerData(player);
    }
    
    private void clearPlayerData(Player player) {
        playerLastMineLocation.remove(player);
        playerOreCounts.remove(player);
        lastMiningTimes.remove(player);
        executeCommand(permissionGrantCommand, player);
    }
}
