package com.yourname.soulplugin.enums;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

public enum SoulType {
    STRENGTH(ChatColor.RED, Material.RED_DYE, SoulRarity.MYTHIC, "Grants permanent Strength II", 1001),
    MACE(ChatColor.RED, Material.NETHER_STAR, SoulRarity.GOD_TIER, "Grants Strength I and Jump Boost II while holding a mace", 1002),
    SEA(ChatColor.AQUA, Material.LIGHT_BLUE_DYE, SoulRarity.RARE, "Grants Dolphins Grace and 20% chance to inflict Mining Fatigue on hit", 1003),
    RESISTANCE(ChatColor.GRAY, Material.ORANGE_DYE, SoulRarity.COMMON, "Grants permanent Resistance I", 1004),
    REGENERATION(ChatColor.DARK_RED, Material.PINK_DYE, SoulRarity.MYTHIC, "Grants Regeneration II and area healing ability", 1005),
    DASH(ChatColor.DARK_BLUE, Material.BLUE_DYE, SoulRarity.RARE, "Grants Speed I and dash ability", 1006),
    ABSORPTION(ChatColor.YELLOW, Material.PURPLE_DYE, SoulRarity.COMMON, "Grants permanent Absorption I", 1007),
    HASTE(ChatColor.GOLD, Material.ORANGE_DYE, SoulRarity.MYTHIC, "Grants Haste V while holding a pickaxe", 1008),
    FROST(ChatColor.AQUA, Material.LIGHT_BLUE_DYE, SoulRarity.RARE, "10% chance to inflict Slowness IV on hit", 1009),
    VAMPIRE(ChatColor.DARK_RED, Material.NETHER_WART, SoulRarity.RARE, "Heal 30% of damage dealt", 1010),
    PHANTOM(ChatColor.DARK_GRAY, Material.GRAY_DYE, SoulRarity.RARE, "Grants invisibility ability", 1011),
    SATURATION(ChatColor.DARK_GRAY, Material.CAKE, SoulRarity.MYTHIC, "Permanant Full Saturation", 1012),

    // Event-only souls
    ENDER_DRAGON(ChatColor.DARK_PURPLE, Material.DRAGON_EGG, SoulRarity.EVENT, "Dragon Rage: Strength II, Regen I, Speed I when below 50% health", 2001),
    WITHER(ChatColor.BLACK, Material.WITHER_SKELETON_SKULL, SoulRarity.EVENT, "Inflicts Wither and Slowness on hit", 2002),
    WARDEN(ChatColor.DARK_AQUA, Material.ECHO_SHARD, SoulRarity.EVENT, "3 extra hearts + area debuff when below 50% health", 2003);
    
    private final ChatColor color;
    private final Material material;
    private final SoulRarity rarity;
    private final String description;
    private final int customModelData;
    
    SoulType(ChatColor color, Material material, SoulRarity rarity, String description, int customModelData) {
        this.color = color;
        this.material = material;
        this.rarity = rarity;
        this.description = description;
        this.customModelData = customModelData;
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
    
    public int getCustomModelData() {
        return customModelData;
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