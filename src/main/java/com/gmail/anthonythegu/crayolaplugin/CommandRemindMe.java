package com.gmail.anthonythegu.crayolaplugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import static org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING;

public class CommandRemindMe implements CommandExecutor {

    private final Main plugin = Main.getPlugin(Main.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Sender must be a player
        if (!(sender instanceof Player))
            return false;

        // Must have at least 2 arguments
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /remindme <time> (seconds | minutes) [<message>]");
            return false;
        }

        // Parse the given time
        long time = 0;
        try {
            time = Long.parseLong(args[0]);
            if (time < 1) {
                sender.sendMessage(ChatColor.RED + "\"" + args[0] + "\"" + " is not a valid number!");
                return false;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "\"" + args[0] + "\"" + " is not a valid number!");
            return false;
        }

        // Parse the given unit of time
        String timeUnit;
        timeUnit = args[1].toLowerCase();

        // Convert given time to delayInTicks in ticks
        long delayInTicks;
        try {
            delayInTicks = toDelayInTicks(time, timeUnit);
        } catch (UnsupportedOperationException e) {
            sender.sendMessage(ChatColor.RED + "\"" + timeUnit + "\"" + " is not a valid unit of time!");
            return false;
        }

        // Parse message, if any
        String message = null;
        boolean hasMessage = args.length > 2;
        if (hasMessage)
            message = parseCommandAsString(2, args);

        // Send confirmation with message, if given
        if (hasMessage) {
            sender.sendMessage("You will be reminded in " + time + " " + timeUnit + " for " + message);
        } else {
            sender.sendMessage("You will be reminded in " + time + " " + timeUnit);
        }

        // Schedule the reminder

        // Must pass final variables to anonymous inner classes
        final String fMessage = message;

        BukkitScheduler scheduler = Bukkit.getScheduler();
        scheduler.runTaskLater(plugin, () -> {
            Player p = (Player) sender;
            p.playSound(p, BLOCK_NOTE_BLOCK_PLING, 2, 1);

            // Send reminder with message, if given
            if (hasMessage) {
                sender.sendMessage("You have been reminded for " + fMessage);
            } else {
                sender.sendMessage("You have been reminded!");
            }

        }, delayInTicks);

        return true;
    }

    private static String parseCommandAsString(int startIndex, String[] args) {
        StringBuilder s = new StringBuilder();

        int i;
        for (i = startIndex; i < args.length - 1; i++) {
            s.append(args[i] + " ");
        }
        s.append(args[i]);

        return s.toString();
    }

    private static long toDelayInTicks(long time, String unitOfTime) {

        long delayInTicks;

        switch (unitOfTime) {
            case "second":
            case "seconds":
            case "sec":
            case "s":
                delayInTicks = time * 20;
                break;
            case "minute":
            case "minutes":
            case "min":
            case "m":
                delayInTicks = time * 60 * 20;
                break;
            default:
                throw new UnsupportedOperationException("Invalid unit of time");
        }

        return delayInTicks;
    }
}
