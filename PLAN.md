# Minecraft Soul Plugin Development Plan

## Project Overview
A Minecraft plugin that creates collectible souls when players die, each providing unique abilities and effects based on rarity.

## Tech Stack
- **Build Tool:** Maven
- **API:** Paper API 1.21.1
- **Java Version:** 17+
- **Plugin Framework:** Bukkit/Paper

## Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── yourname/
│   │           └── soulplugin/
│   │               ├── SoulPlugin.java (Main plugin class)
│   │               ├── enums/
│   │               │   ├── SoulType.java (Soul definitions)
│   │               │   └── SoulRarity.java (Rarity system)
│   │               ├── listeners/
│   │               │   ├── PlayerDeathListener.java (Handle soul drops)
│   │               │   ├── PlayerInteractListener.java (Soul usage)
│   │               │   ├── PlayerCombatListener.java (Combat effects)
│   │               │   └── PlayerMoveListener.java (Movement abilities)
│   │               ├── managers/
│   │               │   ├── SoulManager.java (Soul effect management)
│   │               │   ├── CooldownManager.java (Ability cooldowns)
│   │               │   └── EffectManager.java (Permanent effects)
│   │               └── utils/
│   │                   ├── SoulItemCreator.java (Create soul items)
│   │                   └── MessageUtil.java (Chat utilities)
│   └── resources/
│       ├── plugin.yml
│       └── config.yml
└── pom.xml
```

## Development Steps

### Phase 1: Project Setup
1. Create Maven project structure
2. Configure pom.xml with Paper API dependency
3. Create main plugin class
4. Set up plugin.yml

### Phase 2: Core Soul System
1. Define soul rarities and drop chances
2. Create SoulType enum with all soul definitions
3. Implement soul item creation utility
4. Create soul manager for effect handling

### Phase 3: Event Listeners
1. Player death listener for soul drops
2. Player interact listener for soul consumption
3. Combat listener for special effects
4. Movement listener for abilities

### Phase 4: Effect Management
1. Permanent effect system
2. Cooldown management
3. Special ability implementations
4. Effect cleanup on player death

### Phase 5: Special Features
1. Dash ability with feather item
2. Healing abilities with area effects
3. Invisibility mechanics
4. Combat-based effects

### Phase 6: Testing & Polish
1. Test all soul types and rarities
2. Balance cooldowns and effects
3. Add configuration options
4. Error handling and optimization

## Soul Definitions

### Rarity System
- **Common:** 40% drop chance
- **Rare:** 25% drop chance  
- **Mythic:** 10% drop chance
- **God Tier:** 1% drop chance

### Soul Types
1. **Strength Soul** (Mythic) - RED_DYE - Permanent Strength II
2. **Mace Soul** (God Tier) - WITHER_STAR - Strength I + Jump Boost II while holding mace
3. **Sea Soul** (Rare) - LIGHT_BLUE_DYE - Dolphins Grace + 20% mining fatigue on hit
4. **Resistance Soul** (Common) - ORANGE_DYE - Permanent Resistance I
5. **Regeneration Soul** (Mythic) - PINK_DYE - Regen II + area heal ability
6. **Dash Soul** (Rare) - BLUE_DYE - Speed I + dash ability with feather
7. **Absorption Soul** (Common) - PURPLE_DYE - Permanent Absorption I
8. **Haste Soul** (Mythic) - ORANGE_DYE - Haste V while holding pickaxe
9. **Frost Soul** (Rare) - LIGHT_BLUE_DYE - 10% slowness chance on hit
10. **Vampire Soul** (Rare) - NETHER_WART - Heal 30% of damage dealt
11. **Phantom Soul** (Rare) - GRAY_DYE - Invisibility ability

## Technical Considerations
- Use persistent data containers for player soul tracking
- Implement proper cleanup on player death
- Handle edge cases (player logout, server restart)
- Optimize for performance with large player counts
- Use proper event priorities to avoid conflicts