package com.gmail.anthonythegu.crayolaplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakListener implements Listener {

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {

        final Player p = event.getPlayer();

        if (!p.isSneaking()) { // Player is now sneaking
            new Prayer(p);
        }
    }
}