# Java-Implemented Mob Sprites

This document lists all mob sprites that are currently implemented in Java rather than JSON.

## Currently Java-Implemented Sprites

1. **GhostSprite** - `GhostSprite.java`
2. **ImpSprite** - `ImpSprite.java`
3. **PiranhaSprite** - `PiranhaSprite.java`
4. **RottingFistSprite** - `RottingFistSprite.java`
5. **ShopkeeperSprite** - `ShopkeeperSprite.java`

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
9. **BlacksmithSprite** - Uses `spritesDesc/Blacksmith.json` with particle emitters

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

## Migration Status

- ✅ GooSprite - Migrated to JSON with extras
- ✅ MonkSprite - Migrated to JSON with extras and proper callback handling
- ✅ SeniorSprite - Migrated to JSON with extras and proper callback handling
- ✅ SheepSprite - Migrated to JSON with standard animations
- ✅ RatSprite - Example provided in migration guide
- ✅ LarvaSprite - Migrated to JSON with death effects
- ✅ DM300Sprite - Migrated to JSON with death effects
- ✅ YogSprite - Migrated to JSON with death effects
- ✅ BlacksmithSprite - Migrated to JSON with particle emitters
- ⬜ Others - Pending migration

To migrate any of these sprites, follow the process outlined in `sprite_migration.md`.