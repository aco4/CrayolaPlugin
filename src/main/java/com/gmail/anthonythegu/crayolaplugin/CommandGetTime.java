package com.gmail.anthonythegu.crayolaplugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CommandGetTime implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        LocalTime timeNow = LocalTime.now();

        DateTimeFormatter digiClock = DateTimeFormatter.ofPattern("h:mm a");
        String clockTime = timeNow.format(digiClock);

        sender.sendMessage(clockTime);
        return true;
    }
}
