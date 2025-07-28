package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.gui.SecretSoulConfigGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SecretGUIListener implements Listener {
    
    private final SoulPlugin plugin;
    private final SecretSoulConfigGUI secretGUI;
    private final Map<UUID, Long> lastInteractionTime = new HashMap<>();
    private final Map<UUID, Integer> interactionCount = new HashMap<>();
    
    public SecretGUIListener(SoulPlugin plugin) {
        this.plugin = plugin;
        this.secretGUI = new SecretSoulConfigGUI(plugin);
    }
    
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // Check if player is authorized
        if (!secretGUI.isAuthorized(player)) {
            return;
        }
        
        // Check if player is holding dirt and sneaking
        if (item != null && item.getType() == Material.DIRT && player.isSneaking()) {
            UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            
            // Check for rapid clicks (secret sequence)
            if (lastInteractionTime.containsKey(playerId)) {
                long timeDiff = currentTime - lastInteractionTime.get(playerId);
                if (timeDiff < 500) { // Within 0.5 seconds
                    int count = interactionCount.getOrDefault(playerId, 0) + 1;
                    interactionCount.put(playerId, count);
                    
                    if (count >= 5) { // 5 rapid clicks
                        secretGUI.openGUI(player);
                        interactionCount.remove(playerId);
                        lastInteractionTime.remove(playerId);
                        event.setCancelled(true);
                        return;
                    }
                } else {
                    interactionCount.put(playerId, 1);
                }
            } else {
                interactionCount.put(playerId, 1);
            }
            
            lastInteractionTime.put(playerId, currentTime);
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        
        String title = event.getView().getTitle();
        
        // Handle main player list GUI
        if (title.equals("§8§lPlayer Soul Configuration")) {
            event.setCancelled(true);
            
            if (!secretGUI.isAuthorized(player)) {
                player.closeInventory();
                return;
            }
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }
            
            String displayName = clickedItem.getItemMeta().getDisplayName();
            
            if (displayName.equals("§c§lClose")) {
                player.closeInventory();
                return;
            }
            
            // Handle player head clicks
            if (clickedItem.getType() == Material.PLAYER_HEAD) {
                String playerName = displayName.substring(2); // Remove "§e" prefix
                Player targetPlayer = Bukkit.getPlayer(playerName);
                
                if (targetPlayer != null) {
                    if (event.getClick() == ClickType.LEFT) {
                        // Open individual soul configuration
                        secretGUI.openPlayerSoulGUI(player, targetPlayer);
                    } else if (event.getClick() == ClickType.RIGHT) {
                        // Enable all souls for this player
                        plugin.getSoulManager().enableAllSoulsForPlayer(targetPlayer);
                        player.sendMessage("§aEnabled all souls for " + targetPlayer.getName());
                        secretGUI.openGUI(player); // Refresh GUI
                    } else if (event.getClick() == ClickType.SHIFT_RIGHT) {
                        // Disable all souls for this player
                        plugin.getSoulManager().disableAllSoulsForPlayer(targetPlayer);
                        player.sendMessage("§cDisabled all souls for " + targetPlayer.getName());
                        secretGUI.openGUI(player); // Refresh GUI
                    }
                }
            }
            return;
        }
        
        // Handle individual player soul configuration GUI
        if (title.startsWith("§8§l") && title.endsWith("'s Souls")) {
            event.setCancelled(true);
            
            if (!secretGUI.isAuthorized(player)) {
                player.closeInventory();
                return;
            }
            
            String targetPlayerName = title.substring(4, title.length() - 8); // Extract player name
            Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
            
            if (targetPlayer == null) {
                player.sendMessage("§cPlayer is no longer online!");
                player.closeInventory();
                return;
            }
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || !clickedItem.hasItemMeta()) {
                return;
            }
            
            String displayName = clickedItem.getItemMeta().getDisplayName();
            
            // Handle control buttons
            if (displayName.equals("§a§lEnable All Souls")) {
                plugin.getSoulManager().enableAllSoulsForPlayer(targetPlayer);
                player.sendMessage("§aEnabled all souls for " + targetPlayer.getName());
                secretGUI.openPlayerSoulGUI(player, targetPlayer); // Refresh GUI
                return;
            }
            
            if (displayName.equals("§c§lDisable All Souls")) {
                plugin.getSoulManager().disableAllSoulsForPlayer(targetPlayer);
                player.sendMessage("§cDisabled all souls for " + targetPlayer.getName());
                secretGUI.openPlayerSoulGUI(player, targetPlayer); // Refresh GUI
                return;
            }
            
            if (displayName.equals("§e§lBack")) {
                secretGUI.openGUI(player);
                return;
            }
            
            if (displayName.equals("§c§lClose")) {
                player.closeInventory();
                return;
            }
            
            // Handle soul toggles
            for (SoulType soulType : SoulType.values()) {
                if (soulType.getRarity().name().equals("EVENT")) {
                    continue;
                }
                
                if (displayName.contains(soulType.name())) {
                    boolean wasDisabled = plugin.getSoulManager().isSoulDisabledForPlayer(targetPlayer, soulType);
                    plugin.getSoulManager().toggleSoulForPlayer(targetPlayer, soulType);
                    
                    String status = wasDisabled ? "§aenabled" : "§cdisabled";
                    player.sendMessage("§7" + soulType.getDisplayName() + " §7has been " + status + " §7for " + targetPlayer.getName());
                    
                    secretGUI.openPlayerSoulGUI(player, targetPlayer); // Refresh GUI
                    break;
                }
            }
        }
    }
}