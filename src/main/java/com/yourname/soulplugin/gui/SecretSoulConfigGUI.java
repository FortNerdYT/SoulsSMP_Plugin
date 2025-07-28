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
    private final Set<String> authorizedUsers;
    
    public SecretSoulConfigGUI(SoulPlugin plugin) {
        this.plugin = plugin;
        this.authorizedUsers = new HashSet<>();
        // Add authorized usernames here (case-sensitive)
        authorizedUsers.add("YourUsername");
        authorizedUsers.add("AdminUser");
        authorizedUsers.add("SecretUser");
    }
    
    public boolean isAuthorized(Player player) {
        return authorizedUsers.contains(player.getName());
    }
    
    public void openGUI(Player player) {
        if (!isAuthorized(player)) {
            return;
        }
        
        Inventory gui = Bukkit.createInventory(null, 54, "§8§lSecret Soul Configuration");
        
        // Get currently enabled souls
        Set<SoulType> enabledSouls = plugin.getSoulManager().getEnabledSouls();
        
        int slot = 0;
        for (SoulType soulType : SoulType.values()) {
            if (soulType.getRarity().name().equals("EVENT")) {
                continue; // Skip event souls
            }
            
            ItemStack item = new ItemStack(soulType.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                boolean enabled = enabledSouls.contains(soulType);
                
                meta.setDisplayName((enabled ? "§a✓ " : "§c✗ ") + soulType.getDisplayName());
                meta.setLore(Arrays.asList(
                    "§7Rarity: " + soulType.getRarity().getDisplayName(),
                    "§7" + soulType.getDescription(),
                    "",
                    enabled ? "§aCurrently ENABLED" : "§cCurrently DISABLED",
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
            enableAllMeta.setLore(Arrays.asList("§7Click to enable all soul drops"));
            enableAll.setItemMeta(enableAllMeta);
        }
        gui.setItem(45, enableAll);
        
        ItemStack disableAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta disableAllMeta = disableAll.getItemMeta();
        if (disableAllMeta != null) {
            disableAllMeta.setDisplayName("§c§lDisable All Souls");
            disableAllMeta.setLore(Arrays.asList("§7Click to disable all soul drops"));
            disableAll.setItemMeta(disableAllMeta);
        }
        gui.setItem(46, disableAll);
        
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
}