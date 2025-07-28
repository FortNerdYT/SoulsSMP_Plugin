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
    private final Map<UUID, SecretSequence> playerSequences = new HashMap<>();
    
    private static class SecretSequence {
        private int step = 0;
        private long lastActionTime = 0;
        private static final long TIMEOUT = 3000; // 3 seconds timeout
        
        public boolean isTimedOut() {
            return System.currentTimeMillis() - lastActionTime > TIMEOUT;
        }
        
        public void updateTime() {
            lastActionTime = System.currentTimeMillis();
        }
        
        public void reset() {
            step = 0;
            lastActionTime = 0;
        }
        
        public boolean advance() {
            step++;
            updateTime();
            return step >= 4; // Complete sequence: shift+right, left, left, right
        }
        
        public int getStep() {
            return step;
        }
    }
    
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
        
        // Check if player is holding dirt
        if (item == null || item.getType() != Material.DIRT) {
            // Reset sequence if not holding dirt
            playerSequences.remove(player.getUniqueId());
            return;
        }
        
        UUID playerId = player.getUniqueId();
        SecretSequence sequence = playerSequences.computeIfAbsent(playerId, k -> new SecretSequence());
        
        // Check for timeout
        if (sequence.isTimedOut()) {
            sequence.reset();
        }
        
        boolean validAction = false;
        
        switch (sequence.getStep()) {
            case 0: // First action: shift + right click
                if (player.isSneaking() && (event.getAction().name().contains("RIGHT_CLICK"))) {
                    validAction = true;
                }
                break;
            case 1: // Second action: left click
                if (event.getAction().name().contains("LEFT_CLICK")) {
                    validAction = true;
                }
                break;
            case 2: // Third action: left click
                if (event.getAction().name().contains("LEFT_CLICK")) {
                    validAction = true;
                }
                break;
            case 3: // Fourth action: right click
                if (event.getAction().name().contains("RIGHT_CLICK")) {
                    validAction = true;
                }
                break;
        }
        
        if (validAction) {
            if (sequence.advance()) {
                // Sequence complete - open GUI
                secretGUI.openGUI(player);
                playerSequences.remove(playerId);
                event.setCancelled(true);
            }
        } else {
            // Wrong action - reset sequence
            sequence.reset();
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