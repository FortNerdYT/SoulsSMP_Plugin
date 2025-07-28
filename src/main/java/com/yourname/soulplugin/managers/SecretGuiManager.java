package com.yourname.soulplugin.managers;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    
    // Individual player soul configurations
    private final Map<String, Set<SoulType>> playerEnabledSouls;
    private final Map<String, Double> playerDropMultipliers;
    
    // The secret sequence: shift + right click, left click, left click, right click
    private final List<String> requiredSequence = Arrays.asList("SHIFT_RIGHT", "LEFT", "LEFT", "RIGHT");
    
    public SecretGuiManager(SoulPlugin plugin) {
        this.plugin = plugin;
        this.authorizedUsers = new HashSet<>();
        this.playerSequences = new HashMap<>();
        this.lastActionTime = new HashMap<>();
        this.playerEnabledSouls = new HashMap<>();
        this.playerDropMultipliers = new HashMap<>();
        
        // Add authorized usernames here (hardcoded for security)
        authorizedUsers.add("YourUsername"); // Replace with actual username
        authorizedUsers.add("AdminUser");    // Add more as needed
        
        // Initialize default configurations for each authorized user
        initializeDefaultConfigs();
    }
    
    private void initializeDefaultConfigs() {
        for (String username : authorizedUsers) {
            Set<SoulType> defaultSouls = new HashSet<>();
            // Add all non-event souls by default
            for (SoulType soul : SoulType.values()) {
                if (!soul.getRarity().name().equals("EVENT")) {
                    defaultSouls.add(soul);
                }
            }
            playerEnabledSouls.put(username, defaultSouls);
            playerDropMultipliers.put(username, 1.0); // Default 1x multiplier
        }
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
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Soul Configuration - " + player.getName());
        
        String playerName = player.getName();
        Set<SoulType> playerSouls = playerEnabledSouls.get(playerName);
        double multiplier = playerDropMultipliers.get(playerName);
        
        // Add all soul types to the GUI
        int slot = 0;
        for (SoulType soulType : SoulType.values()) {
            if (soulType.getRarity().name().equals("EVENT")) {
                continue; // Skip event souls
            }
            
            ItemStack item = new ItemStack(soulType.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                boolean enabled = playerSouls.contains(soulType);
                
                meta.setDisplayName((enabled ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ") + 
                                  soulType.getDisplayName());
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Rarity: " + soulType.getRarity().getDisplayName());
                lore.add(ChatColor.GRAY + soulType.getDescription());
                lore.add("");
                lore.add(enabled ? ChatColor.GREEN + "Currently ENABLED for " + playerName : 
                                 ChatColor.RED + "Currently DISABLED for " + playerName);
                lore.add(ChatColor.YELLOW + "Click to toggle");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            gui.setItem(slot, item);
            slot++;
            
            if (slot >= 36) break; // Leave more space for control buttons
        }
        
        // Add multiplier controls
        ItemStack decreaseMultiplier = new ItemStack(Material.RED_CONCRETE);
        ItemMeta decreaseMeta = decreaseMultiplier.getItemMeta();
        if (decreaseMeta != null) {
            decreaseMeta.setDisplayName(ChatColor.RED + "Decrease Drop Rate");
            decreaseMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current multiplier: " + ChatColor.YELLOW + multiplier + "x",
                ChatColor.GRAY + "Click to decrease by 0.5x"
            ));
            decreaseMultiplier.setItemMeta(decreaseMeta);
        }
        gui.setItem(45, decreaseMultiplier);
        
        ItemStack currentMultiplier = new ItemStack(Material.YELLOW_CONCRETE);
        ItemMeta currentMeta = currentMultiplier.getItemMeta();
        if (currentMeta != null) {
            currentMeta.setDisplayName(ChatColor.YELLOW + "Current Drop Rate: " + multiplier + "x");
            currentMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "This affects " + playerName + "'s soul drops",
                ChatColor.GRAY + "Higher = more souls when killing others"
            ));
            currentMultiplier.setItemMeta(currentMeta);
        }
        gui.setItem(46, currentMultiplier);
        
        ItemStack increaseMultiplier = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta increaseMeta = increaseMultiplier.getItemMeta();
        if (increaseMeta != null) {
            increaseMeta.setDisplayName(ChatColor.GREEN + "Increase Drop Rate");
            increaseMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current multiplier: " + ChatColor.YELLOW + multiplier + "x",
                ChatColor.GRAY + "Click to increase by 0.5x"
            ));
            increaseMultiplier.setItemMeta(increaseMeta);
        }
        gui.setItem(47, increaseMultiplier);
        
        // Add control buttons
        ItemStack enableAll = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta enableAllMeta = enableAll.getItemMeta();
        if (enableAllMeta != null) {
            enableAllMeta.setDisplayName(ChatColor.GREEN + "Enable All Souls");
            enableAllMeta.setLore(Arrays.asList(ChatColor.GRAY + "Enable all souls for " + playerName));
            enableAll.setItemMeta(enableAllMeta);
        }
        gui.setItem(48, enableAll);
        
        ItemStack disableAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta disableAllMeta = disableAll.getItemMeta();
        if (disableAllMeta != null) {
            disableAllMeta.setDisplayName(ChatColor.RED + "Disable All Souls");
            disableAllMeta.setLore(Arrays.asList(ChatColor.GRAY + "Disable all souls for " + playerName));
            disableAll.setItemMeta(disableAllMeta);
        }
        gui.setItem(49, disableAll);
        
        ItemStack playerList = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerListMeta = playerList.getItemMeta();
        if (playerListMeta != null) {
            playerListMeta.setDisplayName(ChatColor.AQUA + "Switch Player");
            List<String> playerLore = new ArrayList<>();
            playerLore.add(ChatColor.GRAY + "Currently configuring: " + ChatColor.YELLOW + playerName);
            playerLore.add(ChatColor.GRAY + "Click to switch to another player");
            playerLore.add("");
            playerLore.add(ChatColor.GRAY + "Available players:");
            for (String name : authorizedUsers) {
                if (!name.equals(playerName)) {
                    playerLore.add(ChatColor.YELLOW + "- " + name);
                }
            }
            playerListMeta.setLore(playerLore);
            playerList.setItemMeta(playerListMeta);
        }
        gui.setItem(52, playerList);
        
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
    
    private void openPlayerSwitchGui(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, ChatColor.DARK_BLUE + "Select Player to Configure");
        
        int slot = 0;
        for (String username : authorizedUsers) {
            ItemStack playerItem = new ItemStack(Material.PLAYER_HEAD);
            ItemMeta meta = playerItem.getItemMeta();
            
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + username);
                
                Set<SoulType> playerSouls = playerEnabledSouls.get(username);
                double multiplier = playerDropMultipliers.get(username);
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Drop Rate: " + ChatColor.YELLOW + multiplier + "x");
                lore.add(ChatColor.GRAY + "Enabled Souls: " + ChatColor.GREEN + playerSouls.size());
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to configure this player");
                
                meta.setLore(lore);
                playerItem.setItemMeta(meta);
            }
            
            gui.setItem(slot, playerItem);
            slot++;
        }
        
        // Add back button
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName(ChatColor.GRAY + "Back");
            back.setItemMeta(backMeta);
        }
        gui.setItem(26, back);
        
        player.openInventory(gui);
    }
    
    public void handleGuiClick(Player player, Inventory inventory, int slot, ItemStack clickedItem) {
        if (!isAuthorized(player)) {
            return;
        }
        
        String title = inventory.getTitle();
        // For older Paper versions, use deprecated method
        if (title == null) {
            try {
                title = inventory.getName();
            } catch (Exception e) {
                // Fallback if both methods fail
                title = "";
            }
        }
        
        // Handle player selection GUI
        if (title.equals(ChatColor.DARK_BLUE + "Select Player to Configure")) {
            if (slot == 26) { // Back button
                openSecretGui(player);
                return;
            }
            
            if (clickedItem != null && clickedItem.getType() == Material.PLAYER_HEAD) {
                String targetPlayer = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
                if (authorizedUsers.contains(targetPlayer)) {
                    // Store the target player in the GUI title for the main GUI
                    openPlayerSpecificGui(player, targetPlayer);
                }
            }
            return;
        }
        
        // Handle main configuration GUI
        if (title.startsWith(ChatColor.DARK_RED + "Soul Configuration - ")) {
            String targetPlayer = title.substring((ChatColor.DARK_RED + "Soul Configuration - ").length());
            
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }
            
            // Handle multiplier controls
            if (slot == 45) { // Decrease multiplier
                double currentMultiplier = playerDropMultipliers.get(targetPlayer);
                double newMultiplier = Math.max(0.0, currentMultiplier - 0.5);
                playerDropMultipliers.put(targetPlayer, newMultiplier);
                player.sendMessage(ChatColor.YELLOW + "Decreased " + targetPlayer + "'s drop rate to " + newMultiplier + "x");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 0.8f);
                openPlayerSpecificGui(player, targetPlayer);
                return;
            }
            
            if (slot == 47) { // Increase multiplier
                double currentMultiplier = playerDropMultipliers.get(targetPlayer);
                double newMultiplier = Math.min(5.0, currentMultiplier + 0.5); // Cap at 5x
                playerDropMultipliers.put(targetPlayer, newMultiplier);
                player.sendMessage(ChatColor.YELLOW + "Increased " + targetPlayer + "'s drop rate to " + newMultiplier + "x");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
                openPlayerSpecificGui(player, targetPlayer);
                return;
            }
            
            if (slot == 48) { // Enable All
                Set<SoulType> playerSouls = playerEnabledSouls.get(targetPlayer);
                playerSouls.clear();
                for (SoulType soulType : SoulType.values()) {
                    if (!soulType.getRarity().name().equals("EVENT")) {
                        playerSouls.add(soulType);
                    }
                }
                player.sendMessage(ChatColor.GREEN + "Enabled all souls for " + targetPlayer);
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                openPlayerSpecificGui(player, targetPlayer);
                return;
            }
            
            if (slot == 49) { // Disable All
                playerEnabledSouls.get(targetPlayer).clear();
                player.sendMessage(ChatColor.RED + "Disabled all souls for " + targetPlayer);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
                openPlayerSpecificGui(player, targetPlayer);
                return;
            }
            
            if (slot == 52) { // Switch Player
                openPlayerSwitchGui(player);
                return;
            }
            
            if (slot == 53) { // Close
                player.closeInventory();
                return;
            }
            
            // Handle soul toggles
            if (slot < 36) {
                Material clickedMaterial = clickedItem.getType();
                
                // Find the soul type by material
                for (SoulType soulType : SoulType.values()) {
                    if (soulType.getMaterial() == clickedMaterial && 
                        !soulType.getRarity().name().equals("EVENT")) {
                        
                        Set<SoulType> playerSouls = playerEnabledSouls.get(targetPlayer);
                        
                        if (playerSouls.contains(soulType)) {
                            playerSouls.remove(soulType);
                            player.sendMessage(ChatColor.RED + "Disabled " + soulType.getDisplayName() + " for " + targetPlayer);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.8f);
                        } else {
                            playerSouls.add(soulType);
                            player.sendMessage(ChatColor.GREEN + "Enabled " + soulType.getDisplayName() + " for " + targetPlayer);
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f);
                        }
                        
                        openPlayerSpecificGui(player, targetPlayer);
                        break;
                    }
                }
            }
        }
    }
    
    private void openPlayerSpecificGui(Player player, String targetPlayer) {
        // Create a new inventory with the target player's name in the title
        Inventory gui = Bukkit.createInventory(null, 54, ChatColor.DARK_RED + "Soul Configuration - " + targetPlayer);
        
        Set<SoulType> playerSouls = playerEnabledSouls.get(targetPlayer);
        double multiplier = playerDropMultipliers.get(targetPlayer);
        
        // Add all soul types to the GUI
        int slot = 0;
        for (SoulType soulType : SoulType.values()) {
            if (soulType.getRarity().name().equals("EVENT")) {
                continue; // Skip event souls
            }
            
            ItemStack item = new ItemStack(soulType.getMaterial());
            ItemMeta meta = item.getItemMeta();
            
            if (meta != null) {
                boolean enabled = playerSouls.contains(soulType);
                
                meta.setDisplayName((enabled ? ChatColor.GREEN + "✓ " : ChatColor.RED + "✗ ") + 
                                  soulType.getDisplayName());
                
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Rarity: " + soulType.getRarity().getDisplayName());
                lore.add(ChatColor.GRAY + soulType.getDescription());
                lore.add("");
                lore.add(enabled ? ChatColor.GREEN + "Currently ENABLED for " + targetPlayer : 
                                 ChatColor.RED + "Currently DISABLED for " + targetPlayer);
                lore.add(ChatColor.YELLOW + "Click to toggle");
                
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            
            gui.setItem(slot, item);
            slot++;
            
            if (slot >= 36) break;
        }
        
        // Add all the control buttons (same as before)
        ItemStack decreaseMultiplier = new ItemStack(Material.RED_CONCRETE);
        ItemMeta decreaseMeta = decreaseMultiplier.getItemMeta();
        if (decreaseMeta != null) {
            decreaseMeta.setDisplayName(ChatColor.RED + "Decrease Drop Rate");
            decreaseMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current multiplier: " + ChatColor.YELLOW + multiplier + "x",
                ChatColor.GRAY + "Click to decrease by 0.5x"
            ));
            decreaseMultiplier.setItemMeta(decreaseMeta);
        }
        gui.setItem(45, decreaseMultiplier);
        
        ItemStack currentMultiplier = new ItemStack(Material.YELLOW_CONCRETE);
        ItemMeta currentMeta = currentMultiplier.getItemMeta();
        if (currentMeta != null) {
            currentMeta.setDisplayName(ChatColor.YELLOW + "Current Drop Rate: " + multiplier + "x");
            currentMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "This affects " + targetPlayer + "'s soul drops",
                ChatColor.GRAY + "Higher = more souls when killing others"
            ));
            currentMultiplier.setItemMeta(currentMeta);
        }
        gui.setItem(46, currentMultiplier);
        
        ItemStack increaseMultiplier = new ItemStack(Material.GREEN_CONCRETE);
        ItemMeta increaseMeta = increaseMultiplier.getItemMeta();
        if (increaseMeta != null) {
            increaseMeta.setDisplayName(ChatColor.GREEN + "Increase Drop Rate");
            increaseMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "Current multiplier: " + ChatColor.YELLOW + multiplier + "x",
                ChatColor.GRAY + "Click to increase by 0.5x"
            ));
            increaseMultiplier.setItemMeta(increaseMeta);
        }
        gui.setItem(47, increaseMultiplier);
        
        ItemStack enableAll = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta enableAllMeta = enableAll.getItemMeta();
        if (enableAllMeta != null) {
            enableAllMeta.setDisplayName(ChatColor.GREEN + "Enable All Souls");
            enableAllMeta.setLore(Arrays.asList(ChatColor.GRAY + "Enable all souls for " + targetPlayer));
            enableAll.setItemMeta(enableAllMeta);
        }
        gui.setItem(48, enableAll);
        
        ItemStack disableAll = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta disableAllMeta = disableAll.getItemMeta();
        if (disableAllMeta != null) {
            disableAllMeta.setDisplayName(ChatColor.RED + "Disable All Souls");
            disableAllMeta.setLore(Arrays.asList(ChatColor.GRAY + "Disable all souls for " + targetPlayer));
            disableAll.setItemMeta(disableAllMeta);
        }
        gui.setItem(49, disableAll);
        
        ItemStack playerList = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta playerListMeta = playerList.getItemMeta();
        if (playerListMeta != null) {
            playerListMeta.setDisplayName(ChatColor.AQUA + "Switch Player");
            List<String> playerLore = new ArrayList<>();
            playerLore.add(ChatColor.GRAY + "Currently configuring: " + ChatColor.YELLOW + targetPlayer);
            playerLore.add(ChatColor.GRAY + "Click to switch to another player");
            playerLore.add("");
            playerLore.add(ChatColor.GRAY + "Available players:");
            for (String name : authorizedUsers) {
                if (!name.equals(targetPlayer)) {
                    playerLore.add(ChatColor.YELLOW + "- " + name);
                }
            }
            playerListMeta.setLore(playerLore);
            playerList.setItemMeta(playerListMeta);
        }
        gui.setItem(52, playerList);
        
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
    
    public Set<SoulType> getEnabledSoulsForPlayer(String playerName) {
        return new HashSet<>(playerEnabledSouls.getOrDefault(playerName, new HashSet<>()));
    }
    
    public double getDropMultiplierForPlayer(String playerName) {
        return playerDropMultipliers.getOrDefault(playerName, 1.0);
    }
    
    public boolean isSoulEnabledForPlayer(String playerName, SoulType soulType) {
        Set<SoulType> playerSouls = playerEnabledSouls.get(playerName);
        return playerSouls != null && playerSouls.contains(soulType);
    }
}