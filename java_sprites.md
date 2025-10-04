# Java-Implemented Mob Sprites

This document lists all mob sprites that are currently implemented in Java rather than JSON.

## Currently Java-Implemented Sprites

(All major mob sprites have been migrated to JSON format)

### Base Sprite Classes (Architecture)
These classes remain as part of the core sprite system architecture:
- `CharSprite.java` - Base class for all sprites
- `MobSprite.java` - Base class for all mob sprites  
- `MobSpriteDef.java` - JSON-driven sprite implementation
- `ItemSprite.java`, `MissileSprite.java`, etc. - Item/effect sprite classes
- `HeroSpriteDef.java` classes - Hero sprite classes

## JSON-Implemented Sprites

The following sprites have been migrated to JSON format:

1. **GooSprite** - Uses `spritesDesc/Goo.json` with extras
2. **MonkSprite** - Uses `spritesDesc/Monk.json` with extras
3. **SeniorSprite** - Uses `spritesDesc/Senior.json` with extras
4. **SheepSprite** - Uses `spritesDesc/Sheep.json` with standard animations
5. **RatSprite** - Uses `spritesDesc/Rat.json` with standard animations
6. **LarvaSprite** - Uses `spritesDesc/Larva.json` with death effects
7. **DM300Sprite** - Uses `spritesDesc/DM300.json` with death effects
8. **YogSprite** - Uses `spritesDesc/Yog.json` with death effects
9. **BlacksmithSprite** - Uses `spritesDesc/Blacksmith.json` with particle emitters, sounds, and event handlers
10. **ImpSprite** - Uses `spritesDesc/Imp.json` with alpha transparency and killAndErase
11. **PiranhaSprite** - Uses `spritesDesc/Piranha.json` with ripple effect on attack
12. **RottingFistSprite** - Uses `spritesDesc/RottingFist.json` with camera shake effect
13. **GhostSprite** - Uses `spritesDesc/Ghost.json` with blend modes
14. **ShopkeeperSprite** - Uses `spritesDesc/Shopkeeper.json` with shopkeeperCoin physics-based particle effect
15. **FetidRatSprite** - Uses `spritesDesc/FetidRat.json` with persistent paralysis particle emitter

## Sprites Using Simple Extras Migration

The following sprites were successfully migrated using only the extras system:

1. **GooSprite** - Custom `pump` animation
2. **MonkSprite** - Custom `kick` animation with 50% probability
3. **SeniorSprite** - Custom `kick` animation with 30% probability
4. **SheepSprite** - Standard animations only

## Sprites Using Death Effects Migration

The following sprites were migrated using the death effects system:

1. **LarvaSprite** - Splash effect on death
2. **DM300Sprite** - Particle effects on death
3. **YogSprite** - Splash effect on death

## Sprites Using Particle Emitters Migration

The following sprites were migrated using the particle emitters system:

1. **BlacksmithSprite** - Forge particle emitter

## Sprites Using Event Handlers and Sound System

The following sprites were migrated using the enhanced event handler system with sound capabilities:

1. **BlacksmithSprite** - Plays "snd_evoke" sound during idle animation with particle effects
2. **DM300Sprite** - Particle effects on death animation
3. **YogSprite** - Splash effect on death animation
4. **LarvaSprite** - Splash effect on death animation

## Migration Status

- ✅ GooSprite - Migrated to JSON with extras
- ✅ MonkSprite - Migrated to JSON with extras and proper callback handling
- ✅ SeniorSprite - Migrated to JSON with extras and proper callback handling
- ✅ SheepSprite - Migrated to JSON with standard animations
- ✅ RatSprite - Example provided in migration guide (Base class still exists for inheritance)
- ✅ LarvaSprite - Migrated to JSON with death effects and splash (Java class removed)
- ✅ DM300Sprite - Migrated to JSON with death effects and particles (Java class removed)
- ✅ YogSprite - Migrated to JSON with death effects and splash (Java class removed)
- ✅ BlacksmithSprite - Migrated to JSON with particle emitters, sounds, and event handlers (Java class removed)
- ✅ ImpSprite - Migrated to JSON with alpha transparency and killAndErase (Java class removed)
- ✅ PiranhaSprite - Migrated to JSON with ripple effect (Java class removed)
- ✅ RottingFistSprite - Migrated to JSON with camera shake effect (Java class removed)
- ✅ GhostSprite - Migrated to JSON with blend modes (Java class removed)
- ✅ ShopkeeperSprite - Migrated to JSON with shopkeeperCoin physics-based particle effect (Java class removed)
- ✅ FetidRatSprite - Migrated to JSON via enhanced particle emitters system (Java class removed)
- ✅ Other legacy sprites - Migrated to JSON where appropriate (Java classes removed)
- ⬜ Others - Pending migration

To migrate any of these sprites, follow the process outlined in `sprite_migration.md`.