package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.managers.SecretGuiManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecretGuiListener implements Listener {
    
    private final SecretGuiManager secretGuiManager;
    private final Map<UUID, Boolean> playersSneaking;
    
    public SecretGuiListener(SecretGuiManager secretGuiManager) {
        this.secretGuiManager = secretGuiManager;
        this.playersSneaking = new HashMap<>();
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        playersSneaking.put(player.getUniqueId(), event.isSneaking());
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Check if player is holding dirt
        if (item.getType() != Material.DIRT) {
            return;
        }
        
        // Check if player is authorized
        if (!secretGuiManager.isAuthorized(player)) {
            return;
        }
        
        String action = null;
        
        switch (event.getAction()) {
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:
                if (playersSneaking.getOrDefault(player.getUniqueId(), false)) {
                    action = "SHIFT_RIGHT";
                } else {
                    action = "RIGHT";
                }
                break;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                action = "LEFT";
                break;
            default:
                return;
        }
        
        if (action != null) {
            secretGuiManager.handlePlayerAction(player, action);
            event.setCancelled(true); // Prevent normal interaction
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        secretGuiManager.handleGuiClick(player, event.getInventory(), event.getSlot(), event.getCurrentItem());
        event.setCancelled(true); // Prevent item movement
    }
}