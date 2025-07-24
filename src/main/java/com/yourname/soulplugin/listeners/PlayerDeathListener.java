package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.managers.SoulManager;
import com.yourname.soulplugin.utils.SoulItemCreator;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class PlayerDeathListener implements Listener {
    
    private final SoulManager soulManager;
    
    public PlayerDeathListener(SoulManager soulManager) {
        this.soulManager = soulManager;
    }
    
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Set<SoulType> playerSouls = soulManager.getPlayerSouls(deadPlayer);
        
        // Check if player has an event soul equipped
        boolean hasEventSoul = playerSouls.stream().anyMatch(soulManager::isEventSoul);
        
        if (hasEventSoul) {
            // Drop the equipped event soul (since there's only one of each on the server)
            for (SoulType soulType : playerSouls) {
                if (soulManager.isEventSoul(soulType)) {
                    NamespacedKey soulKey = new NamespacedKey(soulManager.getPlugin(), "soul_type");
                    ItemStack eventSoulItem = SoulItemCreator.createSoulItem(soulType, soulKey);
                    deadPlayer.getLocation().getWorld().dropItemNaturally(deadPlayer.getLocation(), eventSoulItem);
                    break; // Only one soul equipped at a time
                }
            }
        } else {
            // Only drop a random soul if the player was killed by another player AND doesn't have event soul
            if (deadPlayer.getKiller() instanceof Player) {
                // Drop a new random soul at death location (not the player's equipped soul)
                soulManager.dropRandomSoul(deadPlayer.getLocation());
            }
        }
        
        // Always clear the player's current equipped soul on death
        soulManager.clearPlayerSouls(deadPlayer);
    }
}