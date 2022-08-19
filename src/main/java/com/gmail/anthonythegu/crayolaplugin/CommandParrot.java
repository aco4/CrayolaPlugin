package com.gmail.anthonythegu.crayolaplugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandParrot implements CommandExecutor {

    // This method is called when a player uses our command
    // CommandSender represents whatever is sending the command. This could be a Player, ConsoleCommandSender, or BlockCommandSender (a command block)
    // Command represents what the command being called is. This will almost always be known ahead of time.
    // Label represents the exact first word of the command (excluding arguments) that was entered by the sender
    // Args is the remainder of the command statement (excluding the label) split up by spaces and put into an array.
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        int len = args.length;

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /parrot <text>");
            return false;
        }

        StringBuilder message = new StringBuilder();

        // Combine all the arguments together, and add spaces between them
        for (String arg: args) {
            message.append(arg);
            --len;
            if (len > 0) {
                message.append(" ");
            }
        }

        sender.sendMessage(ChatColor.ITALIC + String.valueOf(message));
        return true;
    }
}