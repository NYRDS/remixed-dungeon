# Implementing ShopkeeperCoin Effect in MobSpriteDef

## Current ShopkeeperSprite Implementation Analysis

The current ShopkeeperSprite.java creates a custom PixelParticle with physics:
```java
private PixelParticle coin;

@Override
public void onComplete(Animation anim) {
    super.onComplete(anim);

    ch.ifPresent((chr) -> {
        if (getVisible() && anim == idle && !chr.isParalysed()) {
            if (coin == null) {
                coin = new PixelParticle() {
                    @Override
                    public void reset(float x, float y, int color, float size, float lifespan) {
                        super.reset(x, y, color, size, lifespan);
                        setIsometricShift(true);
                    }
                };
                GameScene.addToMobLayer(coin);
            }
            coin.reset(getX() + (flipHorizontal ? 0 : 13), getY() + 7, 0xFFFF00, 1, 0.5f);
            coin.speed.y = -40;
            coin.acc.y = +160;
        }
    });
}
```

## Proposed Implementation in MobSpriteDef

To implement this as a generic "ShopkeeperCoin" action in MobSpriteDef:

### 1. Add ShopkeeperCoin Action Type
```java
// In executeActions() method of MobSpriteDef
case "shopkeeperCoin":
    String coinTexture = action.optString("texture", "coin"); // Default texture
    int color = (int) Long.decode(action.optString("color", "0xFFFF00")).longValue();
    float size = (float) action.optDouble("size", 1.0f);
    float lifespan = (float) action.optDouble("lifespan", 0.5f);
    float speedY = (float) action.optDouble("speedY", -40.0f);
    float accY = (float) action.optDouble("accY", 160.0f);
    float offsetX = (float) action.optDouble("offsetX", 13.0f);
    float offsetY = (float) action.optDouble("offsetY", 7.0f);
    
    createShopkeeperCoin(color, size, lifespan, speedY, accY, offsetX, offsetY);
    break;
```

### 2. Create the ShopkeeperCoin Method
```java
private void createShopkeeperCoin(int color, float size, float lifespan, float speedY, float accY, float offsetX, float offsetY) {
    // Create or reuse existing coin particle
    if (coinParticle == null) {
        coinParticle = new PixelParticle() {
            @Override
            public void reset(float x, float y, int color, float size, float lifespan) {
                super.reset(x, y, color, size, lifespan);
                setIsometricShift(true);
            }
        };
        GameScene.addToMobLayer(coinParticle);
    }
    
    // Position based on sprite flip state and offsets
    float coinX = getX() + (flipHorizontal ? 0 : offsetX);
    float coinY = getY() + offsetY;
    
    coinParticle.reset(coinX, coinY, color, size, lifespan);
    coinParticle.speed.y = speedY;
    coinParticle.acc.y = accY;
}

// Add field to MobSpriteDef class
private PixelParticle coinParticle;
```

### 3. JSON Configuration Example
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
        "condition": "!ch.isParalysed()",
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

## Benefits of This Approach

1. **Reusability**: Other sprites can now use the shopkeeper coin effect
2. **Configurability**: All parameters can be customized via JSON
3. **Consistency**: Maintains the same JSON-based approach as other sprites
4. **Maintainability**: Centralizes the coin logic in MobSpriteDef

## Challenges to Consider

1. **Conditional Logic**: The current implementation has a complex condition (`getVisible() && anim == idle && !chr.isParalysed()`)
2. **State Management**: Need to implement a particle reuse system to avoid creating new particles each time
3. **Performance**: Need to ensure particle cleanup to prevent memory leaks
4. **Flip Handling**: Need to properly handle sprite flipping in the JSON system

This approach would allow migrating ShopkeeperSprite to JSON while preserving its complex coin animation behavior.