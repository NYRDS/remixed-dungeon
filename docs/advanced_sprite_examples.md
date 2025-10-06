# Advanced Sprite Configuration Examples

This document provides examples of advanced sprite configuration features that were added during the Java-to-JSON sprite migration.

## Persistent Pouring Particle Emitter (Fetid Rat Example)

This example shows how to create a sprite with a persistent particle emitter that continuously pours particles, similar to the Fetid Rat's paralysis gas effect. All particle emitters now continuously pour by default.

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
      "particleType": "Speck.PARALYSIS",
      "interval": 0.7
    }
  }
}
```

**Parameters:**
- `particleType`: Type of particles to emit (e.g., "Speck.PARALYSIS", "Speck.WOOL", etc.)
- `interval`: Time in seconds between particle emissions (default: 1.0)
- `position`: Optional position offset for the emitter { "x": X, "y": Y }

**Important Notes:**
- All particle emitters now pour continuously by default (no need for "pour" parameter)
- All particle emitters have autoKill=false by default (they persist for the sprite's lifetime)
- The "type" and "autoKill" parameters are no longer used in JSON descriptors

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

## Water Ripple Effect (Piranha Example)

This example shows how to create a sprite that triggers a water ripple effect when performing certain animations, similar to the Piranha's attack effect:

```json
{
  "texture": "piranha.png",
  "width": 12,
  "height": 16,
  "idle": { "fps": 8, "looped": true, "frames": [0, 1, 2, 1] },
  "run": { "fps": 20, "looped": true, "frames": [0, 1, 2, 1] },
  "attack": { "fps": 20, "looped": false, "frames": [3, 4, 5, 6, 7, 8, 9, 10, 11] },
  "die": { "fps": 4, "looped": false, "frames": [12, 13, 14] },
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "attack",
        "actions": [
          {
            "action": "ripple"
          }
        ]
      }
    ]
  }
}
```

**Parameters:**
- `action`: Must be "ripple" to trigger water ripple effect
- No additional parameters needed

## Camera Shake Effect (Rotting Fist Example)

This example shows how to create a sprite that triggers a camera shake effect during specific animations, like the Rotting Fist's attack:

```json
{
  "texture": "rotting_fist.png",
  "width": 24,
  "height": 17,
  "idle": { "fps": 2, "looped": true, "frames": [0, 0, 1] },
  "run": { "fps": 3, "looped": true, "frames": [0, 1] },
  "attack": { "fps": 2, "looped": false, "frames": [0] },
  "die": { "fps": 10, "looped": false, "frames": [0, 2, 3, 4] },
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "attack",
        "actions": [
          {
            "action": "cameraShake",
            "intensity": 4,
            "duration": 0.2
          }
        ]
      }
    ]
  }
}
```

**cameraShake Parameters:**
- `intensity`: Strength of the shake (default: 4)
- `duration`: Duration of the shake in seconds (default: 0.2)

## Immediate Removal Effect (Imp Example)

This example shows how to create a sprite that removes itself immediately upon certain animations, like the Imp's death sequence:

```json
{
  "texture": "demon.png",
  "width": 12,
  "height": 14,
  "idle": { "fps": 10, "looped": true, "frames": [0, 1, 2, 3, 0, 1, 2, 3, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 3, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4] },
  "attack": { "fps": 10, "looped": true, "frames": [0] },
  "run": { "fps": 20, "looped": true, "frames": [0] },
  "die": { "fps": 10, "looped": false, "frames": [0, 3, 2, 1, 0, 3, 2, 1, 0] },
  
  "alpha": 0.4,
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "die",
        "actions": [
          {
            "action": "emitParticles",
            "particleType": "Speck.WOOL",
            "count": 15
          },
          {
            "action": "killAndErase"
          }
        ]
      }
    ]
  },
  
  "bloodColor": "0xFFFFFF"
}
```

**killAndErase Parameters:**
- No additional parameters needed
- Immediately removes the sprite from the game world

## Alpha Transparency and Blending (Ghost and Imp Examples)

These examples show how to use transparency and blending effects for special visual appearances:

### Semi-Transparent Sprite (Imp):
```json
{
  "texture": "demon.png",
  "width": 12,
  "height": 14,
  "alpha": 0.4,
  "idle": { "fps": 10, "looped": true, "frames": [0, 1, 2, 3] },
  "die": { "fps": 10, "looped": false, "frames": [0, 3, 2, 1, 0] },
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "die",
        "actions": [
          {
            "action": "killAndErase"
          }
        ]
      }
    ]
  }
}
```

### Additive Blending Sprite (Ghost):
```json
{
  "texture": "ghost.png",
  "width": 14,
  "height": 15,
  "idle": { "fps": 5, "looped": true, "frames": [0, 1] },
  "run": { "fps": 10, "looped": true, "frames": [0, 1] },
  "die": { "fps": 20, "looped": false, "frames": [0] },
  
  "blendMode": "srcAlphaOne",
  
  "eventHandlers": {
    "onComplete": [
      {
        "animation": "die",
        "actions": [
          {
            "action": "emitParticles",
            "particleType": "Speck.LIGHT",
            "count": 4
          },
          {
            "action": "emitParticles",
            "particleType": "Speck.SHAFT",
            "count": 3
          }
        ]
      }
    ]
  },
  
  "bloodColor": "0xFFFFFF"
}
```

## Other Advanced Features

The sprite system also supports several other advanced features:

- **Alpha transparency**: `"alpha": 0.5` for semi-transparent sprites
- **Blending modes**: `"blendMode": "srcAlphaOne"` for special rendering effects
- **Advanced event actions**: `ripple`, `cameraShake`, `killAndErase`
- **Custom animations**: Adding extra animations beyond the standard set through the `extras` section