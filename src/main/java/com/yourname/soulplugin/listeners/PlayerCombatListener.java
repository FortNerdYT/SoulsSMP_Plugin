package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.managers.SoulManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.Set;

public class PlayerCombatListener implements Listener {
    
    private final SoulManager soulManager;
    private final Random random = new Random();
    
    public PlayerCombatListener(SoulManager soulManager) {
        this.soulManager = soulManager;
    }
    
    @EventHandler
    public void onPlayerDamagePlayer(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) {
            return;
        }
        
        Set<SoulType> attackerSouls = soulManager.getPlayerSouls(attacker);
        
        // Sea Soul - 20% chance to give mining fatigue
        if (attackerSouls.contains(SoulType.SEA) && random.nextDouble() < 0.2) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 1200, 0)); // 1 minute
            attacker.sendMessage("§bYour Sea Soul inflicted Mining Fatigue!");
            victim.sendMessage("§7You have been afflicted with Mining Fatigue!");
        }
        
        // Frost Soul - 10% chance to give slowness
        if (attackerSouls.contains(SoulType.FROST) && random.nextDouble() < 0.1) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 3)); // 4 seconds of Slowness IV
            attacker.sendMessage("§3Your Frost Soul froze your enemy!");
            victim.sendMessage("§7You have been frozen!");
        }
        
        // Vampire Soul - heal 40% of damage dealt
        if (attackerSouls.contains(SoulType.VAMPIRE)) {
            double damage = event.getFinalDamage();
            double healAmount = damage * 0.4;
            
            double newHealth = Math.min(attacker.getMaxHealth(), attacker.getHealth() + healAmount);
            attacker.setHealth(newHealth);
            
            attacker.sendMessage("§4Your Vampire Soul healed you for " + String.format("%.1f", healAmount) + " hearts!");
        }
        
        // Wither Soul - inflict wither and slowness on hit
        if (attackerSouls.contains(SoulType.WITHER)) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 60, 0)); // 3 second Wither I
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1)); // 3 second Slowness I
        }
    }
}