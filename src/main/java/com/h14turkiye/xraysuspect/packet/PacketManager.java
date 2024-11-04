package com.h14turkiye.xraysuspect.packet;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketManager {
    private static final Logger LOGGER = Bukkit.getLogger();

    private static final Constructor<?> PACKET_CONSTRUCTOR;
    private static final Method SEND_PACKET_METHOD;

    static {
        Class<?> chunkClass = ReflectionUtils.findClass("dvi", "net.minecraft.world.level.chunk.LevelChunk", "net.minecraft.class_2818", "net.minecraft.world.chunk.WorldChunk", "net.minecraft.src.C_2137_", "net.minecraft.world.level.chunk.Chunk");
        Class<?> leClass = ReflectionUtils.findClass("eot", "net.minecraft.world.level.lighting.LevelLightEngine", "net.minecraft.class_3568", "net.minecraft.world.chunk.light.LightingProvider", "net.minecraft.src.C_2681_");
        Class<?> packetClass = ReflectionUtils.findClass("zg", "net.minecraft.network.protocol.Packet", "net.minecraft.class_2596", "net.minecraft.network.packet.Packet", "net.minecraft.src.C_5028_", "net.minecraft.network.protocol.Packet");
        Class<?> cpacketClass = ReflectionUtils.findClass("net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket", "adg", "net.minecraft.class_2672", "net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket", "net.minecraft.src.C_183120_");

        PACKET_CONSTRUCTOR = ReflectionUtils.initializeConstructor(cpacketClass, chunkClass, leClass, BitSet.class, BitSet.class);
        SEND_PACKET_METHOD = ReflectionUtils.initializeMethod(ReflectionUtils.PLAYER_CONNECTION_CLASS, new String[]{"b", "send", "method_14364", "sendPacket", "m_141995_"}, packetClass);
    }

    public static Object createChunkPacket(Player player, Chunk chunk) {
        try {
            Object worldServer = ReflectionUtils.getCraftBukkitWorld(player.getWorld());
            Object nmsChunk = ReflectionUtils.getChunk(worldServer, chunk.getX(), chunk.getZ());

            if (nmsChunk == null) {
                LOGGER.log(Level.WARNING, "Chunk not loaded at (" + chunk.getX() + ", " + chunk.getZ() + ")");
                return null;
            }

            Object lightEngine = ReflectionUtils.getLightEngine(worldServer);
            return PACKET_CONSTRUCTOR.newInstance(nmsChunk, lightEngine, null, null);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating chunk packet", e);
            return null;
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {
            Object entityPlayer = ReflectionUtils.getEntityPlayer(player);
            SEND_PACKET_METHOD.invoke(ReflectionUtils.getConnection(entityPlayer), packet);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to send packet", e);
        }
    }
}
