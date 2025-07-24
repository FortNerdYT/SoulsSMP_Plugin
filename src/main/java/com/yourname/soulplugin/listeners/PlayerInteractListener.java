package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.managers.SoulManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class PlayerInteractListener implements Listener {
    
    private final SoulManager soulManager;
    private final SoulPlugin plugin;
    
    public PlayerInteractListener(SoulManager soulManager, SoulPlugin plugin) {
        this.soulManager = soulManager;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null) {
            return;
        }
        
        // Handle soul consumption
        if (soulManager.isSoulItem(item)) {
            if (soulManager.consumeSoul(player, item)) {
                item.setAmount(item.getAmount() - 1);
                event.setCancelled(true);
            }
            return;
        }
        
        // Handle dash feather
        if (isDashFeather(item)) {
            handleDashAbility(player);
            event.setCancelled(true);
        }
    }
    
    private boolean isDashFeather(ItemStack item) {
        if (item.getType() != Material.FEATHER || !item.hasItemMeta()) {
            return false;
        }
        
        NamespacedKey dashKey = new NamespacedKey(plugin, "dash_feather");
        return item.getItemMeta().getPersistentDataContainer().has(dashKey, PersistentDataType.BYTE);
    }
    
    private void handleDashAbility(Player player) {
        if (!soulManager.hasSoul(player, SoulType.DASH)) {
            player.sendMessage("§cYou don't have the Dash Soul!");
            return;
        }
        
        if (soulManager.getCooldownManager().isOnCooldown(player, "dash")) {
            long remaining = soulManager.getCooldownManager().getRemainingCooldown(player, "dash");
            player.sendMessage("§cDash is on cooldown for " + remaining + " seconds!");
            return;
        }
        
        // Perform dash
        Vector direction = player.getLocation().getDirection();
        direction.setY(0.2); // Add slight upward momentum
        direction.multiply(3.25); // Dash distance multiplier
        
        player.setVelocity(direction);
        player.sendMessage("§aDash!");
        
        soulManager.getCooldownManager().setCooldown(player, "dash", 30);
    }
}