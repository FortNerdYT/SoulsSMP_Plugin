package com.yourname.soulplugin.managers;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class SecretGuiManager {
    
    private final SoulPlugin plugin;
    private final Set<String> authorizedUsers;
    private final Map<UUID, List<String>> playerSequences;
    private final Map<UUID, Long> lastActionTime;
    private final Set<SoulType> enabledSouls;
    
    // The secret sequence: shift + right click, left click, left click, right click
    private final List<String> requiredSequence = Arrays.asList("SHIFT_RIGHT", "LEFT", "LEFT", "RIGHT");
    
    public SecretGuiManager(SoulPlugin plugin) {
        this.plugin = plugin;
        this.authorizedUsers = new HashSet<>();
        this.playerSequences = new HashMap<>();
        this.lastActionTime = new HashMap<>();
        this.enabledSouls = new HashSet<>();
        
        // Add authorized usernames here (hardcoded for security)
        authorizedUsers.add("YourUsername"); // Replace with actual username
        authorizedUsers.add("AdminUser");    // Add more as needed
        
        // Initialize with all souls enabled by default
        enabledSouls.addAll(Arrays.asList(SoulType.values()));
        // Remove event souls from default enabled list
        enabledSouls.removeIf(soul -> soul.getRarity().name().equals("EVENT"));
    }
    
    public boolean isAuthorized(Player player) {
        return authorizedUsers.contains(player.getName());
    }
    
    public void handlePlayerAction(Player player, String action) {
        if (!isAuthorized(player)) {
            return;
        }
        
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // Reset sequence if too much time has passed (5 seconds)
        if (lastActionTime.containsKey(playerId) && 
            currentTime - lastActionTime.get(playerId) > 5000) {
            playerSequences.remove(playerId);
        }
        
        lastActionTime.put(playerId, currentTime);
        
        List<String> sequence = playerSequences.computeIfAbsent(playerId, k -> new ArrayList<>());
        sequence.add(action);
        
        // Keep only the last 4 actions
        if (sequence.size() > 4) {
            sequence.remove(0);
        }
        
        // Check if sequence matches
        if (sequence.size() == 4 && sequence.equals(requiredSequence)) {
            openSecretGui(player);
            playerSequences.remove(playerId);
            lastActionTime.remove(playerId);
        }
    }
    
    private void openSecretGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Soul Drop Configuration");
        
        // Add all soul types to the GUI
        int slot = 0;
        for (SoulType soulType : SoulType.values()) {
            if (soulType.getRarity().name().equals("EVENT")) {
                continue; // Skip event souls
            }
            
            ItemStack item = new ItemStack(soulType.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                boolean enabled = enabledSouls.contains(soulType);
                
                meta.setDisplayName((enabled ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ") + 
                                  soulType.getDisplayName());
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Rarity: " + soulType.getRarity().getDisplayName());
                lore.add(ChatColor.GRAY + soulType.getDescription());
                lore.add("");
                lore.add(enabled ? ChatColor.GREEN + "Currently ENABLED" : ChatColor.RED + "Currently DISABLED");
                lore.add(ChatColor.YELLOW + "Click to toggle");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            gui.setItem(slot, item);
            slot++;
            
            if (slot >= 45) break; // Leave space for control buttons
        }
        
        // Add control buttons
        ItemStack enableAll = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta enableAllMeta = enableAll.getItemMeta();
        if (enableAllMeta != null) {
            enableAllMeta.setDisplayName(ChatColor.GREEN + "Enable All Souls");
            enableAllMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to enable all soul drops"));
            enableAll.setItemMeta(enableAllMeta);
        }
        gui.setItem(45, enableAll);
        
        ItemStack disableAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta disableAllMeta = disableAll.getItemMeta();
        if (disableAllMeta != null) {
            disableAllMeta.setDisplayName(ChatColor.RED + "Disable All Souls");
            disableAllMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to disable all soul drops"));
            disableAll.setItemMeta(disableAllMeta);
        }
        gui.setItem(46, disableAll);
        
        ItemStack close = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.RED + "Close");
            closeMeta.setLore(Arrays.asList(ChatColor.GRAY + "Click to close this menu"));
            close.setItemMeta(closeMeta);
        }
        gui.setItem(53, close);
        
        player.openInventory(gui);
    }
    
    public void handleGuiClick(Player player, Inventory inventory, int slot, ItemStack clickedItem) {
        if (!isAuthorized(player)) {
            return;
        }
        
        if (!inventory.getTitle().equals(ChatColor.DARK_RED + "Soul Drop Configuration")) {
            return;
        }
        
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }
        
        // Handle control buttons
        if (slot == 45) { // Enable All
            enabledSouls.clear();
            for (SoulType soulType : SoulType.values()) {
                if (!soulType.getRarity().name().equals("EVENT")) {
                    enabledSouls.add(soulType);
                }
            }
            openSecretGui(player); // Refresh GUI
            return;
        }
        
        if (slot == 46) { // Disable All
            enabledSouls.clear();
            openSecretGui(player); // Refresh GUI
            return;
        }
        
        if (slot == 53) { // Close
            player.closeInventory();
            return;
        }
        
        // Handle soul toggles
        if (slot < 45) {
            Material clickedMaterial = clickedItem.getType();
            
            // Find the soul type by material
            for (SoulType soulType : SoulType.values()) {
                if (soulType.getMaterial() == clickedMaterial && 
                    !soulType.getRarity().name().equals("EVENT")) {
                    
                    if (enabledSouls.contains(soulType)) {
                        enabledSouls.remove(soulType);
                    } else {
                        enabledSouls.add(soulType);
                    }
                    
                    openSecretGui(player); // Refresh GUI
                    break;
                }
            }
        }
    }
    
    public Set<SoulType> getEnabledSouls() {
        return new HashSet<>(enabledSouls);
    }
    
    public boolean isSoulEnabled(SoulType soulType) {
        return enabledSouls.contains(soulType);
    }
}