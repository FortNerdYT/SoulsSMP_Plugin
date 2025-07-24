package com.yourname.soulplugin.managers;

import com.yourname.soulplugin.SoulPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackManager implements Listener {
    
    private final SoulPlugin plugin;
    private String resourcePackUrl;
    private String resourcePackHash;
    private boolean forceResourcePack;
    
    public ResourcePackManager(SoulPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }
    
    private void loadConfig() {
        this.resourcePackUrl = plugin.getConfig().getString("resource-pack.url", "");
        this.resourcePackHash = plugin.getConfig().getString("resource-pack.hash", "");
        this.forceResourcePack = plugin.getConfig().getBoolean("resource-pack.force", false);
    }
    
    public void reloadConfig() {
        loadConfig();
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!resourcePackUrl.isEmpty()) {
            Player player = event.getPlayer();
            
            // Delay the resource pack request slightly to ensure player is fully loaded
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (resourcePackHash.isEmpty()) {
                    player.setResourcePack(resourcePackUrl);
                } else {
                    player.setResourcePack(resourcePackUrl, resourcePackHash);
                }
            }, 20L); // 1 second delay
        }
    }
    
    @EventHandler
    public void onResourcePackStatus(PlayerResourcePackStatusEvent event) {
        Player player = event.getPlayer();
        
        switch (event.getStatus()) {
            case SUCCESSFULLY_LOADED:
                player.sendMessage("§a✓ Soul Plugin resource pack loaded successfully!");
                break;
                
            case DECLINED:
                if (forceResourcePack) {
                    player.kickPlayer("§cYou must accept the resource pack to play on this server!");
                } else {
                    player.sendMessage("§eResource pack declined. Some soul textures may not display correctly.");
                }
                break;
                
            case FAILED_DOWNLOAD:
                player.sendMessage("§cFailed to download resource pack. Some soul textures may not display correctly.");
                break;
                
            case ACCEPTED:
                player.sendMessage("§eDownloading Soul Plugin resource pack...");
                break;
        }
    }
    
    public void sendResourcePack(Player player) {
        if (!resourcePackUrl.isEmpty()) {
            if (resourcePackHash.isEmpty()) {
                player.setResourcePack(resourcePackUrl);
            } else {
                player.setResourcePack(resourcePackUrl, resourcePackHash);
            }
        }
    }
    
    public boolean hasResourcePack() {
        return !resourcePackUrl.isEmpty();
    }
}