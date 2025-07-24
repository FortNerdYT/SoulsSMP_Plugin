package com.yourname.soulplugin;

import com.yourname.soulplugin.listeners.*;
import com.yourname.soulplugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class SoulPlugin extends JavaPlugin {
    
    private SoulManager soulManager;
    private CooldownManager cooldownManager;
    private EffectManager effectManager;
    private ResourcePackManager resourcePackManager;
    
    @Override
    public void onEnable() {
        // Save default config
        saveDefaultConfig();
        
        // Initialize managers
        cooldownManager = new CooldownManager();
        effectManager = new EffectManager(this);
        soulManager = new SoulManager(this, effectManager, cooldownManager);
        resourcePackManager = new ResourcePackManager(this);
        
        // Register event listeners
        registerListeners();
        
        // Register commands
        getCommand("souls").setExecutor(new SoulCommand(this, soulManager));
        
        getLogger().info("SoulPlugin has been enabled!");
        
        if (resourcePackManager.hasResourcePack()) {
            getLogger().info("Resource pack configured and ready!");
        } else {
            getLogger().warning("No resource pack URL configured. Soul textures will use default Minecraft textures.");
        }
    }
    
    @Override
    public void onDisable() {
        // Clean up any running tasks
        if (effectManager != null) {
            effectManager.cleanup();
        }
        
        getLogger().info("SoulPlugin has been disabled!");
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(soulManager), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(soulManager, this), this);
        getServer().getPluginManager().registerEvents(new PlayerCombatListener(soulManager), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(soulManager, this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinQuitListener(effectManager), this);
        getServer().getPluginManager().registerEvents(resourcePackManager, this);
    }
    
    public SoulManager getSoulManager() {
        return soulManager;
    }
    
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
    
    public EffectManager getEffectManager() {
        return effectManager;
    }
    
    public ResourcePackManager getResourcePackManager() {
        return resourcePackManager;
    }
}