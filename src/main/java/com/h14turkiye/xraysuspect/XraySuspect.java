package com.h14turkiye.xraysuspect;

import org.bukkit.plugin.java.JavaPlugin;

import com.h14turkiye.xraysuspect.listener.BlockBreakListener;

public class XraySuspect extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        // config
        this.saveDefaultConfig();
    }
}