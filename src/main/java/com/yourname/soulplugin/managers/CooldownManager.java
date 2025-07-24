package com.yourname.soulplugin.managers;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    
    public void setCooldown(Player player, String ability, long cooldownSeconds) {
        UUID playerId = player.getUniqueId();
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>())
                 .put(ability, System.currentTimeMillis() + (cooldownSeconds * 1000));
    }
    
    public boolean isOnCooldown(Player player, String ability) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        
        if (playerCooldowns == null || !playerCooldowns.containsKey(ability)) {
            return false;
        }
        
        long cooldownEnd = playerCooldowns.get(ability);
        if (System.currentTimeMillis() >= cooldownEnd) {
            playerCooldowns.remove(ability);
            return false;
        }
        
        return true;
    }
    
    public long getRemainingCooldown(Player player, String ability) {
        UUID playerId = player.getUniqueId();
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        
        if (playerCooldowns == null || !playerCooldowns.containsKey(ability)) {
            return 0;
        }
        
        long cooldownEnd = playerCooldowns.get(ability);
        long remaining = (cooldownEnd - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }
}