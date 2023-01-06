package com.gmail.anthonythegu.crayolaplugin;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Register each of the following commands (set an instance of each command's class as executor)
//        this.getCommand("gettime").setExecutor(new CommandGetTime());
        this.getCommand("remindme").setExecutor(new CommandRemindMe());
        this.getCommand("crayolapluginconfigreload").setExecutor(new CommandCrayolaPluginConfigReload());

        // Register each of the following Event Listeners (set an instance of each command's class as executor)
        getServer().getPluginManager().registerEvents(new SneakListener(), this);

        // Create config file from jar config file
        this.saveDefaultConfig();
        // This thread is really useful https://www.spigotmc.org/threads/whats-the-difference-between-savedefaultconfig-copydefaults.301865/
    }

    @Override
    public void onDisable() {
    }
}
