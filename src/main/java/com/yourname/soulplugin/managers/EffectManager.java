package com.yourname.soulplugin.managers;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class EffectManager {
    
    private final SoulPlugin plugin;
    
    public EffectManager(SoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    public void applyPermanentEffects(Player player) {
        removePermanentEffects(player);
        
        Set<SoulType> playerSouls = plugin.getSoulManager().getPlayerSouls(player);
        
        for (SoulType soulType : playerSouls) {
            PotionEffectType effectType = soulType.getPermanentEffect();
            if (effectType != null) {
                int amplifier = soulType.getEffectAmplifier();
                player.addPotionEffect(new PotionEffect(effectType, Integer.MAX_VALUE, amplifier, false, false));
            }
        }
        
        // Start task for conditional effects
        startConditionalEffectTask(player);
    }
    
    public void removePermanentEffects(Player player) {
        // Remove all permanent effects that souls can give
        for (SoulType soulType : SoulType.values()) {
            PotionEffectType effectType = soulType.getPermanentEffect();
            if (effectType != null && player.hasPotionEffect(effectType)) {
                player.removePotionEffect(effectType);
            }
        }
        
        // Remove conditional effects
        player.removePotionEffect(PotionEffectType.STRENGTH);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.HASTE);
        
        // Remove any dash feathers from inventory
        removeAllDashFeathers(player);
    }
    
    private void removeAllDashFeathers(Player player) {
        // Remove all dash feathers from player's inventory
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && isDashFeather(item)) {
                player.getInventory().setItem(i, null);
            }
        }
    }
    
    private boolean isDashFeather(ItemStack item) {
        if (item.getType() != org.bukkit.Material.FEATHER || !item.hasItemMeta()) {
            return false;
        }
        
        org.bukkit.NamespacedKey dashKey = new org.bukkit.NamespacedKey(plugin, "dash_feather");
        return item.getItemMeta().getPersistentDataContainer().has(dashKey, org.bukkit.persistence.PersistentDataType.BYTE);
    }
    
    private void startConditionalEffectTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }
                
                Set<SoulType> playerSouls = plugin.getSoulManager().getPlayerSouls(player);
                
                // Mace Soul - Strength I and Jump Boost II while holding mace
                if (playerSouls.contains(SoulType.MACE)) {
                    if (player.getInventory().getItemInMainHand().getType() == Material.MACE) {
                        if (!player.hasPotionEffect(PotionEffectType.STRENGTH) || 
                            player.getPotionEffect(PotionEffectType.STRENGTH).getAmplifier() < 0) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 100, 0));
                        }
                        if (!player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 1));
                        }
                    } else {
                        // Remove these specific effects if not holding mace
                        if (player.hasPotionEffect(PotionEffectType.STRENGTH) && 
                            player.getPotionEffect(PotionEffectType.STRENGTH).getAmplifier() == 0) {
                            player.removePotionEffect(PotionEffectType.STRENGTH);
                        }
                        if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
                            player.removePotionEffect(PotionEffectType.JUMP_BOOST);
                        }
                    }
                }
                
                // Haste Soul - Haste V while holding pickaxe
                if (playerSouls.contains(SoulType.HASTE)) {
                    Material mainHand = player.getInventory().getItemInMainHand().getType();
                    if (isPickaxe(mainHand)) {
                        if (!player.hasPotionEffect(PotionEffectType.HASTE) || 
                            player.getPotionEffect(PotionEffectType.HASTE).getAmplifier() < 4) {
                            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 100, 4));
                        }
                    } else {
                        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
                            player.removePotionEffect(PotionEffectType.HASTE);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // Run every second
    }
    
    private boolean isPickaxe(Material material) {
        return material == Material.WOODEN_PICKAXE ||
               material == Material.STONE_PICKAXE ||
               material == Material.IRON_PICKAXE ||
               material == Material.GOLDEN_PICKAXE ||
               material == Material.DIAMOND_PICKAXE ||
               material == Material.NETHERITE_PICKAXE;
    }
    
    public void cleanup() {
        // Cancel any running tasks if needed
    }
}