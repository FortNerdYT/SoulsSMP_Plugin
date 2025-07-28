package com.yourname.soulplugin.managers;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulRarity;
import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.utils.SoulItemCreator;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class SoulManager {
    
    private final SoulPlugin plugin;
    private final EffectManager effectManager;
    private final CooldownManager cooldownManager;
    private final NamespacedKey soulKey;
    private final NamespacedKey playerSoulsKey;
    private final Random random;
    
    public SoulManager(SoulPlugin plugin, EffectManager effectManager, CooldownManager cooldownManager) {
        this.plugin = plugin;
        this.effectManager = effectManager;
        this.cooldownManager = cooldownManager;
        this.soulKey = new NamespacedKey(plugin, "soul_type");
        this.playerSoulsKey = new NamespacedKey(plugin, "player_souls");
        this.random = new Random();
    }
    
    public void dropRandomSoul(Location location) {
        SoulType soulType = getRandomSoulType();
        if (soulType != null) {
            ItemStack soulItem = SoulItemCreator.createSoulItem(soulType, soulKey);
            location.getWorld().dropItemNaturally(location, soulItem);
        }
    }
    
    public void dropRandomSoulForPlayer(Location location, String killerName) {
        SoulType soulType = getRandomEnabledSoulForPlayer(killerName);
        if (soulType != null) {
            ItemStack soulItem = SoulItemCreator.createSoulItem(soulType, soulKey);
            location.getWorld().dropItemNaturally(location, soulItem);
        }
    }
    
    private SoulType getRandomEnabledSoulForPlayer(String killerName) {
        // Get enabled souls for this specific player
        Set<SoulType> enabledSouls = plugin.getSecretGuiManager().getEnabledSoulsForPlayer(killerName);
        double dropMultiplier = plugin.getSecretGuiManager().getDropMultiplierForPlayer(killerName);
        
        if (enabledSouls.isEmpty()) {
            return null; // No souls enabled for this player
        }
        
        // Apply the player's drop multiplier to increase chances
        double roll = random.nextDouble() * (100.0 / dropMultiplier);
        
        // Calculate total drop chance from enabled souls
        double totalDropChance = enabledSouls.stream()
            .mapToDouble(soul -> soul.getRarity().getDropChance())
            .sum();
        
        if (roll <= totalDropChance) {
            // Convert enabled souls to list and pick randomly
            List<SoulType> enabledSoulsList = new ArrayList<>(enabledSouls);
            if (!enabledSoulsList.isEmpty()) {
                return enabledSoulsList.get(random.nextInt(enabledSoulsList.size()));
            }
        }
        
        return null; // No soul dropped
    }
    
    public boolean consumeSoul(Player player, ItemStack item) {
        if (!isSoulItem(item)) {
            return false;
        }
        
        String soulTypeName = item.getItemMeta().getPersistentDataContainer()
            .get(soulKey, PersistentDataType.STRING);
        
        if (soulTypeName == null) {
            return false;
        }
        
        try {
            SoulType soulType = SoulType.valueOf(soulTypeName);
            
            // First, completely clear all existing souls and effects
            clearPlayerSouls(player);
            
            // Then add the new soul
            addSoulToPlayer(player, soulType);
            effectManager.applyPermanentEffects(player);
            
            // Special items for certain souls
            if (soulType == SoulType.DASH) {
                giveDashFeather(player);
            }
            
            player.sendMessage("§7You have consumed a " + soulType.getRarity().getColor() + 
                             soulType.name() + " Soul§7!");
            
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    public boolean isSoulItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        
        return item.getItemMeta().getPersistentDataContainer().has(soulKey, PersistentDataType.STRING);
    }
    
    public void addSoulToPlayer(Player player, SoulType soulType) {
        // Only allow one soul at a time - replace any existing soul
        PersistentDataContainer playerData = player.getPersistentDataContainer();
        playerData.set(playerSoulsKey, PersistentDataType.STRING, soulType.name());
    }
    
    public Set<SoulType> getPlayerSouls(Player player) {
        PersistentDataContainer playerData = player.getPersistentDataContainer();
        String soulsString = playerData.get(playerSoulsKey, PersistentDataType.STRING);
        
        Set<SoulType> souls = new HashSet<>();
        if (soulsString != null && !soulsString.isEmpty() && !soulsString.contains(",")) {
            // Only one soul allowed now
            try {
                souls.add(SoulType.valueOf(soulsString));
            } catch (IllegalArgumentException ignored) {
                // Invalid soul type, ignore
            }
        }
        
        return souls;
    }
    
    public void clearPlayerSouls(Player player) {
        player.getPersistentDataContainer().remove(playerSoulsKey);
        effectManager.removePermanentEffects(player);
    }
    
    public boolean hasSoul(Player player, SoulType soulType) {
        return getPlayerSouls(player).contains(soulType);
    }
    
    public boolean isEventSoul(SoulType soulType) {
        return soulType.getRarity() == SoulRarity.EVENT;
    }
    
    private void giveDashFeather(Player player) {
        ItemStack dashFeather = SoulItemCreator.createDashFeather(new NamespacedKey(plugin, "dash_feather"));
        player.getInventory().addItem(dashFeather);
        player.sendMessage("§7You received a §bDash Feather§7! Right-click to dash!");
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    public SoulPlugin getPlugin() {
        return plugin;
    }
}