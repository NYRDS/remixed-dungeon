# Java-Implemented Mob Sprites

This document lists all mob sprites that are currently implemented in Java rather than JSON.

## Currently Java-Implemented Sprites

1. **BlacksmithSprite** - `BlacksmithSprite.java`
2. **DM300Sprite** - `DM300Sprite.java`
3. **GhostSprite** - `GhostSprite.java`
4. **ImpSprite** - `ImpSprite.java`
5. **LarvaSprite** - `LarvaSprite.java`
6. **PiranhaSprite** - `PiranhaSprite.java`
7. **RottingFistSprite** - `RottingFistSprite.java`
8. **ShopkeeperSprite** - `ShopkeeperSprite.java`
9. **YogSprite** - `YogSprite.java`

## JSON-Implemented Sprites

The following sprites have been migrated to JSON format:

1. **GooSprite** - Uses `spritesDesc/Goo.json` with extras
2. **MonkSprite** - Uses `spritesDesc/Monk.json` with extras
3. **SeniorSprite** - Uses `spritesDesc/Senior.json` with extras
4. **SheepSprite** - Uses `spritesDesc/Sheep.json` with standard animations
5. **RatSprite** - Uses `spritesDesc/Rat.json` with standard animations

## Sprites Using Simple Extras Migration

The following sprites were successfully migrated using only the extras system:

1. **GooSprite** - Custom `pump` animation
2. **MonkSprite** - Custom `kick` animation
3. **SeniorSprite** - Custom `kick` animation
4. **SheepSprite** - Standard animations only

## Migration Status

- ✅ GooSprite - Migrated to JSON with extras
- ✅ MonkSprite - Migrated to JSON with extras
- ✅ SeniorSprite - Migrated to JSON with extras
- ✅ SheepSprite - Migrated to JSON with standard animations
- ✅ RatSprite - Example provided in migration guide
- ⬜ Others - Pending migration

To migrate any of these sprites, follow the process outlined in `sprite_migration.md`.