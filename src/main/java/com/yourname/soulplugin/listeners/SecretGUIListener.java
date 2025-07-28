package com.yourname.soulplugin.listeners;

import com.yourname.soulplugin.SoulPlugin;
import com.yourname.soulplugin.enums.SoulType;
import com.yourname.soulplugin.gui.SecretSoulConfigGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        
        // Check if player is holding a specific item (nether star) and sneaking
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
        
        if (!event.getView().getTitle().equals("§8§lSecret Soul Configuration")) {
            return;
        }
        
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
        
        // Handle control buttons
        if (displayName.equals("§a§lEnable All Souls")) {
            plugin.getSoulManager().enableAllSouls();
            player.sendMessage("§a§lAll souls have been enabled!");
            secretGUI.openGUI(player); // Refresh GUI
            return;
        }
        
        if (displayName.equals("§c§lDisable All Souls")) {
            plugin.getSoulManager().disableAllSouls();
            player.sendMessage("§c§lAll souls have been disabled!");
            secretGUI.openGUI(player); // Refresh GUI
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
                boolean wasEnabled = plugin.getSoulManager().isSoulEnabled(soulType);
                plugin.getSoulManager().toggleSoul(soulType);
                
                String status = wasEnabled ? "§cdisabled" : "§aenabled";
                player.sendMessage("§7" + soulType.getDisplayName() + " §7has been " + status + "§7!");
                
                secretGUI.openGUI(player); // Refresh GUI
                break;
            }
        }
    }
}