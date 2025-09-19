# Simple Implementation Plan

## Overview

This document outlines the minimal changes needed to support custom animations using the existing extras system.

## Current State

1. ✅ `Char.playExtra(String key)` method exists and works
2. ✅ `Mob` extends `Char`, so all mobs can call `playExtra`
3. ✅ `MobSpriteDef` already loads extras from JSON
4. ✅ `CharSprite.playExtra(String key)` already plays extras
5. ✅ Lua scripts can call `self:playExtra("animation")`

## What Was Missing (Now Fixed)

The only thing missing was a way for Java-based mob logic to call custom animations with completion callbacks. This has now been addressed by:

### Enhanced CharSprite
```java
// Added overloaded method with callback support
public void playExtra(String key, Callback callback) {
    if (extras.containsKey(key)) {
        if (callback != null) {
            animCallback = callback;
        }
        play(extras.get(key));
    }
}
```

This enhancement allows any extra animation to be played with a completion callback, ensuring proper animation flow.

## Example Usage

### From Lua Mob Script:
```lua
local mob = require "scripts/lib/mob"

return mob.init({
    attackProc = function(self, enemy, damage)
        if math.random() < 0.5 then
            self:playExtra("kick")  -- This already works!
        end
        return damage
    end
})
```

### From Java Mob (with callback):
```java
public class CustomMob extends Mob {
    public void doSpecialAttack() {
        // Play animation with callback
        getSprite().playExtra("pump", new Callback() {
            @Override
            public void call() {
                // Continue with attack logic when animation completes
                onAttackComplete();
            }
        });
    }
}
```

## Migration Process

1. Convert Java sprite classes to JSON definitions with extras
2. Add Lua mob scripts for complex behaviors that need to trigger extras
3. Remove Java sprite classes (they're no longer needed)

## Example Migration: GooSprite

### Before (Java Sprite):
```java
public class GooSprite extends MobSprite {
    private final Animation pump;
    
    public GooSprite() {
        // ... animation setup ...
        pump = new Animation(20, true);
        pump.frames(frames, 0, 1);
    }
    
    public void pumpUp() {
        play(pump);
    }
}
```

### After (JSON Sprite):
```json
{
  "texture": "goo.png",
  "width": 20,
  "height": 14,
  "idle": { "fps": 10, "looped": true, "frames": [0, 1] },
  "run": { "fps": 10, "looped": true, "frames": [0, 1] },
  "attack": { "fps": 10, "looped": false, "frames": [5, 0, 6] },
  "zap": { "fps": 10, "looped": false, "frames": [5, 0, 6] },
  "die": { "fps": 10, "looped": false, "frames": [2, 3, 4] },
  
  "extras": {
    "pump": { "fps": 20, "looped": true, "frames": [0, 1] }
  },
  
  "properties": {
    "bloodColor": "0xFF000000"
  }
}
```

### Lua Mob Script (if needed):
```lua
local mob = require "scripts/lib/mob"

return mob.init({
    pumpUp = function(self)
        self:playExtra("pump")
    end
})
```

## Benefits

1. **No Major Code Changes Required**: The system already works!
2. **Backward Compatible**: Existing code continues to work
3. **Simple Migration**: Just convert Java sprites to JSON
4. **Flexible**: Works from both Java and Lua
5. **Moddable**: Animations can be customized without recompilation
6. **Enhanced API**: Callback support for complex animation flows

This is the beauty of the existing system - it already supports everything we need with minimal implementation work, and now with the added callback support, it can handle even more complex animation scenarios!