# Advanced Sprite Configuration Examples

This document provides examples of advanced sprite configuration features that were added during the Java-to-JSON sprite migration.

## Persistent Pouring Particle Emitter (Fetid Rat Example)

This example shows how to create a sprite with a persistent particle emitter that continuously pours particles, similar to the Fetid Rat's paralysis gas effect:

```json
{
  "texture" : "rat.png",
  "width"  : 16,
  "height" : 15,
  "idle"   : { "fps" : 2,  "looped" : true,     "frames" : [0,0,0,1] },
  "run"    : { "fps" : 14, "looped" : true,     "frames" : [6,7,8,9,10] },
  "attack" : { "fps" : 11, "looped" : false,    "frames" : [2,3,4,5,0] },
  "die"    : { "fps" : 11, "looped" : false,    "frames" : [11,12,13,14] },
  
  "particleEmitters": {
    "paralysisCloud": {
      "type": "Emitter",
      "particleType": "Speck.PARALYSIS",
      "pour": true,
      "interval": 0.7,
      "autoKill": false
    }
  }
}
```

**Parameters:**
- `pour`: Set to `true` to enable continuous particle pouring
- `interval`: Time in seconds between particle emissions (default: 1.0)
- `autoKill`: Set to `false` to keep the emitter active throughout the sprite's lifetime

## Physics-Based Coin Animation (Shopkeeper Example)

This example shows how to create a sprite with a physics-based coin animation that occurs during idle animation, similar to the Shopkeeper's coin-tossing effect:

```json
{
  "texture": "keeper.png",
  "width": 14,
  "height": 14,
  "idle": { "fps": 10, "looped": true, "frames": [1, 1, 1, 1, 1, 0, 0, 0, 0] },
  "run": { "fps": 10, "looped": true, "frames": [1, 1, 1, 1, 1, 0, 0, 0, 0] },
  "attack": { "fps": 10, "looped": true, "frames": [1, 1, 1, 1, 1, 0, 0, 0, 0] },
  "die": { "fps": 10, "looped": true, "frames": [1, 1, 1, 1, 1, 0, 0, 0, 0] },
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "idle",
        "actions": [
          {
            "action": "shopkeeperCoin",
            "color": "0xFFFF00",
            "size": 1,
            "lifespan": 0.5,
            "speedY": -40,
            "accY": 160,
            "offsetX": 13,
            "offsetY": 7
          }
        ]
      }
    ]
  }
}
```

**shopkeeperCoin Parameters:**
- `color`: Color of the coin particle (default: "0xFFFF00")
- `size`: Size of the particle (default: 1.0)
- `lifespan`: How long the particle appears (default: 0.5)
- `speedY`: Initial vertical speed (default: -40.0)
- `accY`: Vertical acceleration (gravity effect, default: 160.0)
- `offsetX`: X offset from sprite position (default: 13.0)
- `offsetY`: Y offset from sprite position (default: 7.0)

## Other Advanced Features

The sprite system also supports several other advanced features:

- **Alpha transparency**: `"alpha": 0.5` for semi-transparent sprites
- **Blending modes**: `"blendMode": "srcAlphaOne"` for special rendering effects
- **Advanced event actions**: `ripple`, `cameraShake`, `killAndErase`
- **Custom animations**: Adding extra animations beyond the standard set through the `extras` section