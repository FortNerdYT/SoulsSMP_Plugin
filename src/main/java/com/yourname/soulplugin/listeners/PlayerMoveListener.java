package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.managers.SoulManager;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMoveListener implements Listener {
    
    private final SoulManager soulManager;
    private final SoulPlugin plugin;
    private final Map<UUID, Long> sneakStartTimes = new HashMap<>();
    private final Map<UUID, BukkitRunnable> activeShiftTasks = new HashMap<>();
    
    public PlayerMoveListener(SoulManager soulManager, SoulPlugin plugin) {
        this.soulManager = soulManager;
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (event.isSneaking()) {
            // Player started sneaking
            sneakStartTimes.put(playerId, System.currentTimeMillis());
            
            // Check for regeneration soul healing ability (5 seconds)
            if (soulManager.hasSoul(player, SoulType.REGENERATION)) {
                startRegenerationShiftTask(player);
            }
            
            // Check for phantom soul invisibility ability (3 seconds)
            if (soulManager.hasSoul(player, SoulType.PHANTOM)) {
                startPhantomShiftTask(player);
            }
        } else {
            // Player stopped sneaking
            sneakStartTimes.remove(playerId);
            
            // Cancel any active shift tasks
            BukkitRunnable task = activeShiftTasks.remove(playerId);
            if (task != null) {
                task.cancel();
            }
        }
    }
    
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        double healthAfterDamage = player.getHealth() - event.getFinalDamage();
        double maxHealth = player.getMaxHealth();
        
        // Check if player will be below 50% health after damage
        if (healthAfterDamage <= maxHealth * 0.5) {
            handleLowHealthAbilities(player);
        }
    }
    
    private void handleLowHealthAbilities(Player player) {
        // Ender Dragon Soul - Dragon Rage
        if (soulManager.hasSoul(player, SoulType.ENDER_DRAGON)) {
            if (!soulManager.getCooldownManager().isOnCooldown(player, "dragon_rage")) {
                // Play dragon roar sound
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
                
                // Apply effects
                player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 1)); // 15 seconds Strength II
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 0)); // 15 seconds Regen I
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 0)); // 15 seconds Speed I
                
                player.sendMessage("§5🐉 DRAGON RAGE ACTIVATED! 🐉");
                
                // Set cooldown (5:00 = 300 seconds)
                soulManager.getCooldownManager().setCooldown(player, "dragon_rage", 300);
            }
        }
        
        // Warden Soul - Area debuff
        if (soulManager.hasSoul(player, SoulType.WARDEN)) {
            if (!soulManager.getCooldownManager().isOnCooldown(player, "warden_scream")) {
                // Apply debuffs to all players within 10 blocks
                player.getNearbyEntities(10, 10, 10).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .forEach(nearbyPlayer -> {
                        nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 300, 1)); // 15 seconds Slowness II
                        nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 0)); // 15 seconds Blindness
                        nearbyPlayer.sendMessage("§3💀 The Warden's presence overwhelms you! 💀");
                        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_HEARTBEAT, 1.0f, 1.0f);
                    });
                
                player.sendMessage("§3Your Warden Soul unleashed a terrifying scream!");
                
                // Set cooldown 
                soulManager.getCooldownManager().setCooldown(player, "warden_scream", 60);
            }
        }
    }
    
    private void startRegenerationShiftTask(Player player) {
        if (soulManager.getCooldownManager().isOnCooldown(player, "regeneration_heal")) {
            return;
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isSneaking() || !soulManager.hasSoul(player, SoulType.REGENERATION)) {
                    cancel();
                    return;
                }
                
                // Heal all players within 5 blocks
                player.getNearbyEntities(5, 5, 5).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .forEach(nearbyPlayer -> {
                        nearbyPlayer.setHealth(nearbyPlayer.getMaxHealth());
                        nearbyPlayer.sendMessage("§d✨ You have been healed by " + player.getName() + "'s Regeneration Soul!");
                    });
                
                // Also heal the caster
                player.setHealth(player.getMaxHealth());
                player.sendMessage("§dYou used your Regeneration Soul's healing ability!");
                
                // Set cooldown
                soulManager.getCooldownManager().setCooldown(player, "regeneration_heal", 180); // 3 minutes
                
                cancel();
            }
        };
        
        activeShiftTasks.put(player.getUniqueId(), task);
        task.runTaskLater(plugin, 100L); // 5 seconds
    }
    
    private void startPhantomShiftTask(Player player) {
        if (soulManager.getCooldownManager().isOnCooldown(player, "phantom_invisibility")) {
            return;
        }
        
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isSneaking() || !soulManager.hasSoul(player, SoulType.PHANTOM)) {
                    cancel();
                    return;
                }
                
                // Grant invisibility for 30 seconds
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 600, 0)); // 30 seconds
                player.sendMessage("§8You have become invisible!");
                
                // Set cooldown
                soulManager.getCooldownManager().setCooldown(player, "phantom_invisibility", 180); // 3 minutes
                
                cancel();
            }
        };
        
        activeShiftTasks.put(player.getUniqueId(), task);
        task.runTaskLater(plugin, 60L); // 3 seconds
    }
}