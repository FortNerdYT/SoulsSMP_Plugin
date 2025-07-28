package com.yourname.soulplugin.gui;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SecretSoulConfigGUI {
    
    private final SoulPlugin plugin;
    
    public SecretSoulConfigGUI(SoulPlugin plugin) {
        this.plugin = plugin;
    }
    
    public boolean isAuthorized(Player player) {
        String authorizedUsers = plugin.getConfig().getString("secret-gui.authorized-users", "");
        if (authorizedUsers.isEmpty()) {
            return false;
        }
        
        String[] usernames = authorizedUsers.split(",");
        for (String username : usernames) {
            if (username.trim().equals(player.getName())) {
                return true;
            }
        }
        return false;
    }
    
    public void openGUI(Player player) {
        if (!isAuthorized(player)) {
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§8§lPlayer Soul Configuration");
        
        int slot = 0;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (slot >= 45) break; // Leave space for control items
            
            ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerHead.getItemMeta();
            
            if (meta != null) {
                Set<SoulType> disabledSouls = plugin.getSoulManager().getDisabledSoulsForPlayer(onlinePlayer);
                int disabledCount = disabledSouls.size();
                int totalSouls = (int) Arrays.stream(SoulType.values())
                    .filter(soul -> soul.getRarity().name().equals("EVENT") == false)
                    .count();
                int enabledCount = totalSouls - disabledCount;
                
                meta.setDisplayName("§e" + onlinePlayer.getName());
                meta.setLore(Arrays.asList(
                    "§7Enabled Souls: §a" + enabledCount + "§7/§a" + totalSouls,
                    "§7Disabled Souls: §c" + disabledCount,
                    "",
                    "§eLeft-click to configure souls",
                    "§eRight-click to enable all souls",
                    "§eShift+Right-click to disable all souls"
                ));
                
                playerHead.setItemMeta(meta);
            }
            
            gui.setItem(slot, playerHead);
            slot++;
        }
        
        // Add control items
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose");
            closeMeta.setLore(Arrays.asList("§7Click to close this menu"));
            close.setItemMeta(closeMeta);
        }
        gui.setItem(53, close);
        
        player.openInventory(gui);
    }
    
    public void openPlayerSoulGUI(Player admin, Player targetPlayer) {
        if (!isAuthorized(admin)) {
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§8§l" + targetPlayer.getName() + "'s Souls");
        
        Set<SoulType> disabledSouls = plugin.getSoulManager().getDisabledSoulsForPlayer(targetPlayer);
        
        int slot = 0;
        for (SoulType soulType : SoulType.values()) {
            if (soulType.getRarity().name().equals("EVENT")) {
                continue; // Skip event souls
            }
            
            ItemStack item = new ItemStack(soulType.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                boolean enabled = !disabledSouls.contains(soulType);
                
                meta.setDisplayName((enabled ? "§a✓ " : "§c✗ ") + soulType.getDisplayName());
                meta.setLore(Arrays.asList(
                    "§7Rarity: " + soulType.getRarity().getDisplayName(),
                    "§7" + soulType.getDescription(),
                    "",
                    enabled ? "§aCurrently ENABLED for " + targetPlayer.getName() : "§cCurrently DISABLED for " + targetPlayer.getName(),
                    "§eClick to toggle!"
                ));
                
                meta.setCustomModelData(soulType.getCustomModelData());
                item.setItemMeta(meta);
            }
            
            gui.setItem(slot, item);
            slot++;
            
            if (slot >= 45) break; // Leave space for control items
        }
        
        // Add control items
        ItemStack enableAll = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta enableAllMeta = enableAll.getItemMeta();
        if (enableAllMeta != null) {
            enableAllMeta.setDisplayName("§a§lEnable All Souls");
            enableAllMeta.setLore(Arrays.asList("§7Click to enable all souls for " + targetPlayer.getName()));
            enableAll.setItemMeta(enableAllMeta);
        }
        gui.setItem(45, enableAll);
        
        ItemStack disableAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta disableAllMeta = disableAll.getItemMeta();
        if (disableAllMeta != null) {
            disableAllMeta.setDisplayName("§c§lDisable All Souls");
            disableAllMeta.setLore(Arrays.asList("§7Click to disable all souls for " + targetPlayer.getName()));
            disableAll.setItemMeta(disableAllMeta);
        }
        gui.setItem(46, disableAll);
        
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§e§lBack");
            backMeta.setLore(Arrays.asList("§7Return to player list"));
            back.setItemMeta(backMeta);
        }
        gui.setItem(52, back);
        
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName("§c§lClose");
            closeMeta.setLore(Arrays.asList("§7Click to close this menu"));
            close.setItemMeta(closeMeta);
        }
        gui.setItem(53, close);
        
        admin.openInventory(gui);
    }
}