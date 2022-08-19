package com.gmail.anthonythegu.crayolaplugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandCrayolaPluginConfigReload implements CommandExecutor {

    private final Main plugin = Main.getPlugin(Main.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.isOp()) {
            plugin.reloadConfig();
            sender.sendMessage("CrayolaPlugin configuration has been reloaded");
            return true;
        }
        sender.sendMessage(ChatColor.RED + "You must be op to use this command!");
        return false;
    }
}
