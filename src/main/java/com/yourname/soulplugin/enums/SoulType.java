package com.yourname.soulplugin.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public enum SoulType {
    STRENGTH(ChatColor.RED, Material.RED_DYE, SoulRarity.MYTHIC, "Grants permanent Strength II"),
    MACE(ChatColor.RED, Material.NETHER_STAR, SoulRarity.GOD_TIER, "Grants Strength I and Jump Boost II while holding a mace"),
    SEA(ChatColor.AQUA, Material.LIGHT_BLUE_DYE, SoulRarity.RARE, "Grants Dolphins Grace and 20% chance to inflict Mining Fatigue on hit"),
    RESISTANCE(ChatColor.GRAY, Material.ORANGE_DYE, SoulRarity.COMMON, "Grants permanent Resistance I"),
    REGENERATION(ChatColor.DARK_RED, Material.PINK_DYE, SoulRarity.MYTHIC, "Grants Regeneration II and area healing ability"),
    DASH(ChatColor.DARK_BLUE, Material.BLUE_DYE, SoulRarity.RARE, "Grants Speed I and dash ability"),
    ABSORPTION(ChatColor.YELLOW, Material.PURPLE_DYE, SoulRarity.COMMON, "Grants permanent Absorption I"),
    HASTE(ChatColor.GOLD, Material.ORANGE_DYE, SoulRarity.MYTHIC, "Grants Haste V while holding a pickaxe"),
    FROST(ChatColor.AQUA, Material.LIGHT_BLUE_DYE, SoulRarity.RARE, "10% chance to inflict Slowness IV on hit"),
    VAMPIRE(ChatColor.DARK_RED, Material.NETHER_WART, SoulRarity.RARE, "Heal 30% of damage dealt"),
    PHANTOM(ChatColor.DARK_GRAY, Material.GRAY_DYE, SoulRarity.RARE, "Grants invisibility ability"),
    SATURATION(ChatColor.DARK_GRAY, Material.CAKE, SoulRarity.MYTHIC, "Permanant Full Saturation"),

    // Event-only souls
    ENDER_DRAGON(ChatColor.DARK_PURPLE, Material.DRAGON_EGG, SoulRarity.EVENT, "Dragon Rage: Strength II, Regen I, Speed I when below 50% health"),
    WITHER(ChatColor.BLACK, Material.WITHER_SKELETON_SKULL, SoulRarity.EVENT, "Inflicts Wither and Slowness on hit"),
    WARDEN(ChatColor.DARK_AQUA, Material.ECHO_SHARD, SoulRarity.EVENT, "3 extra hearts + area debuff when below 50% health");
    
    private final ChatColor color;
    private final Material material;
    private final SoulRarity rarity;
    private final String description;
    
    SoulType(ChatColor color, Material material, SoulRarity rarity, String description) {
        this.color = color;
        this.material = material;
        this.rarity = rarity;
        this.description = description;
    }
    
    public ChatColor getColor() {
        return color;
    }
    
    public Material getMaterial() {
        return material;
    }
    
    public SoulRarity getRarity() {
        return rarity;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getDisplayName() {
        return color + name() + " SOUL";
    }
    
    public PotionEffectType getPermanentEffect() {
        return switch (this) {
            case STRENGTH -> PotionEffectType.STRENGTH;
            case SEA -> PotionEffectType.DOLPHINS_GRACE;
            case RESISTANCE -> PotionEffectType.RESISTANCE;
            case REGENERATION -> PotionEffectType.REGENERATION;
            case DASH -> PotionEffectType.SPEED;
            case ABSORPTION -> PotionEffectType.ABSORPTION;
            case WARDEN -> PotionEffectType.HEALTH_BOOST; // 3 extra hearts
            case SATURATION -> PotionEffectType.SATURATION; // 3 extra hearts
            default -> null;
        };
    }
    
    public int getEffectAmplifier() {
        return switch (this) {
            case STRENGTH -> 1; // Strength II
            case WARDEN -> 1; // Health Boost III (3 extra hearts)
            default -> 0; // Level I for others
        };
    }
}