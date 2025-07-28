package com.yourname.soulplugin.utils;

import com.yourname.soulplugin.enums.SoulType;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class SoulItemCreator {
    
    public static ItemStack createSoulItem(SoulType soulType, NamespacedKey soulKey) {
        ItemStack item = new ItemStack(soulType.getMaterial());
        ItemMeta meta = item.getItemMeta();

        
        if (meta != null) {
            // Set display name
            meta.setDisplayName(soulType.getDisplayName());            
            meta.setCustomModelData(soulType.getCMD());
            
            // Set lore
            List<String> lore = Arrays.asList(
                "§7Rarity: " + soulType.getRarity().getDisplayName(),
                "§7" + soulType.getDescription(),
                "",
                "§eRight-click to consume!"
            );
            meta.setLore(lore);
            
            // Add enchantment glow
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            // Store soul type in persistent data
            meta.getPersistentDataContainer().set(soulKey, PersistentDataType.STRING, soulType.name());
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    public static ItemStack createDashFeather(NamespacedKey dashKey) {
        ItemStack feather = new ItemStack(Material.FEATHER);
        ItemMeta meta = feather.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName("§bDash Feather");
            meta.setLore(Arrays.asList(
                "§7Right-click to dash forward!",
                "§7Cooldown: 30 seconds"
            ));
            
            // Add enchantment glow
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            
            // Mark as dash feather
            meta.getPersistentDataContainer().set(dashKey, PersistentDataType.BYTE, (byte) 1);
            
            feather.setItemMeta(meta);
        }
        
        return feather;
    }
}