package com.gmail.anthonythegu.crayolaplugin;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING;

public class CommandRemindMe implements CommandExecutor {

    private final Main plugin = Main.getPlugin(Main.class);

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            long time;
            long timeUnit;
            long delay;
            LocalTime now = LocalTime.now();
            LocalTime remindTime;

            if (args.length == 2) {
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
                switch (args[1]) {
                    case "second":
                    case "seconds":
                    case "sec":
                    case "s":
                        timeUnit = 20L;
                        delay = timeUnit * time;
                        remindTime = now.plus(time, ChronoUnit.SECONDS);
                        break;
                    case "minute":
                    case "minutes":
                    case "min":
                    case "m":
                        timeUnit = 1200L;
                        delay = timeUnit * time;
                        remindTime = now.plus(time, ChronoUnit.MINUTES);
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "\"" + args[1] + "\"" + " is not a valid unit of time!");
                        return false;
                }

                DateTimeFormatter digiClock = DateTimeFormatter.ofPattern("h:mm a");
                String clockRemindTime = remindTime.format(digiClock);
                sender.sendMessage("You will be reminded in " + time + " " + args[1] + " at " + clockRemindTime);

                BukkitScheduler scheduler = Bukkit.getScheduler();
                scheduler.runTaskLater(plugin, () -> {
                    Player p = (Player) sender;
                    p.playSound(p, BLOCK_NOTE_BLOCK_PLING, 2, 1);
                    sender.sendMessage("YOU HAVE BEEN REMINDED");
                }, delay /* Delay in ticks */);

                return true;
            }
            sender.sendMessage(ChatColor.RED + "Usage: /remindme <time> <[seconds | minutes]>");
            return false;
        }
        return false;
    }
}
