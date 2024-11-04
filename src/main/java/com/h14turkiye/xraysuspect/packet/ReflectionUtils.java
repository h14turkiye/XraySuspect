package com.h14turkiye.xraysuspect.packet;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {
    private static final Logger LOGGER = Bukkit.getLogger();
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();

    // Constants for reflection lookup
    public static final Class<?> LEVEL_CLASS;
    public static final Class<?> ENTITY_PLAYER_CLASS;
    public static final Class<?> PLAYER_CONNECTION_CLASS;
    public static final Method GET_LIGHT_ENGINE_METHOD;
    public static final Method GET_CHUNK_METHOD;
    public static final Field CONNECTION_FIELD;

    static {
        LEVEL_CLASS = findClass("dcw", "net.minecraft.world.level.Level", "net.minecraft.class_1937", "net.minecraft.world.World", "net.minecraft.src.C_1596_", "net.minecraft.world.level.World");
        ENTITY_PLAYER_CLASS = findClass("aqv", "net.minecraft.server.level.ServerPlayer", "net.minecraft.class_3222", "net.minecraft.server.network.ServerPlayerEntity", "net.minecraft.src.C_13_");
        PLAYER_CONNECTION_CLASS = findClass("arx", "net.minecraft.server.network.ServerPlayerConnection", "net.minecraft.class_5629", "net.minecraft.server.network.PlayerAssociatedNetworkHandler", "net.minecraft.src.C_140962_");

        GET_LIGHT_ENGINE_METHOD = initializeMethod(LEVEL_CLASS, new String[]{"y_", "getLightEngine", "method_22336", "getLightingProvider", "m_5518_"});
        GET_CHUNK_METHOD = initializeMethod(LEVEL_CLASS, new String[]{"d", "getChunk", "method_8497", "m_6325_", "getChunkIfLoaded"}, int.class, int.class);
        CONNECTION_FIELD = initializeField(ENTITY_PLAYER_CLASS, "c", "connection", "field_13987", "networkHandler", "f_8906_");
    }

    // Static helper methods for reflection initialization

    public static Class<?> findClass(String... possibleNames) {
        for (String name : possibleNames) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ignored) {}
        }
        LOGGER.log(Level.WARNING, "Failed to find class for names: " + String.join(", ", possibleNames));
        return null;
    }

    public static Method initializeMethod(Class<?> clazz, String[] possibleNames, Class<?>... params) {
        if (clazz == null) return null;
        for (String name : possibleNames) {
            try {
                Method method = clazz.getDeclaredMethod(name, params);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {}
        }
        LOGGER.log(Level.WARNING, "Failed to find method for names: " + String.join(", ", possibleNames));
        return null;
    }

    public static Field initializeField(Class<?> clazz, String... possibleNames) {
        if (clazz == null) return null;
        for (String fieldName : possibleNames) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {}
        }
        LOGGER.log(Level.WARNING, "Failed to find field for names: " + String.join(", ", possibleNames));
        return null;
    }

    public static Constructor<?> initializeConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz != null ? clazz.getConstructor(parameterTypes) : null;
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE, "Failed to find constructor for class: " + clazz, e);
            return null;
        }
    }

    public static Object getCraftBukkitWorld(World world) throws Exception {
        Object craftWorld = Class.forName(cbClass("CraftWorld")).cast(world);
        return craftWorld.getClass().getMethod("getHandle").invoke(craftWorld);
    }

    public static Object getChunk(Object worldServer, int x, int z) throws Exception {
        return GET_CHUNK_METHOD.invoke(worldServer, x, z);
    }

    public static Object getLightEngine(Object worldServer) throws Exception {
        return GET_LIGHT_ENGINE_METHOD.invoke(worldServer);
    }

    public static Object getEntityPlayer(Player player) throws Exception {
        Object craftPlayer = Class.forName(cbClass("entity.CraftPlayer")).cast(player);
        return craftPlayer.getClass().getMethod("getHandle").invoke(craftPlayer);
    }

    public static Object getConnection(Object entityPlayer) throws Exception {
        return CONNECTION_FIELD.get(entityPlayer);
    }

    public static String cbClass(String clazz) {
        return CRAFTBUKKIT_PACKAGE + "." + clazz;
    }
}
