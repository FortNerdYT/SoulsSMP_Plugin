package com.yourname.soulplugin.enums;

import org.bukkit.ChatColor;

public enum SoulRarity {
    COMMON(ChatColor.WHITE, 20.0),
    RARE(ChatColor.BLUE, 35.0),
    MYTHIC(ChatColor.DARK_PURPLE, 20.0),
    GOD_TIER(ChatColor.GOLD, 5.0),
    EVENT(ChatColor.LIGHT_PURPLE, 0.0); // Event souls don't drop naturally
    
    private final ChatColor color;
    private final double dropChance;
    
    SoulRarity(ChatColor color, double dropChance) {
        this.color = color;
        this.dropChance = dropChance;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public double getDropChance() {
        return dropChance;
    }
    
    public String getDisplayName() {
        return color + name().replace("_", " ");
    }
}