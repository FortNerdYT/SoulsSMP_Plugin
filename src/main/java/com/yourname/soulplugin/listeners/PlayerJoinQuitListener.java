package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.managers.EffectManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {
    
    private final EffectManager effectManager;
    
    public PlayerJoinQuitListener(EffectManager effectManager) {
        this.effectManager = effectManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        // Apply permanent effects when player joins
        effectManager.applyPermanentEffects(player);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        // Clean up any temporary data
        effectManager.removePermanentEffects(player);
    }
}