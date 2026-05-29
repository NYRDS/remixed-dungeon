# Mob Animation Viewer

A web application to load and play mob animations from Remixed Dungeon using existing sprite JSONs and textures.

## Features

- **Browse Mobs**: Select from a list of available mobs (Bat, Brute, Crab, Eye, Ghost, etc.)
- **Animation Playback**: View different animations (idle, run, attack, die)
- **Controls**:
  - Play/Pause animation
  - Toggle looping
  - Adjust scale (1x - 8x)
  - Adjust playback speed (0.25x - 4x)
- **Search**: Filter mobs by name
- **Sprite Info**: View sprite metadata (name, size, texture, available animations)

## Running the Application

### Option 1: Python HTTP Server (Recommended)

```bash
cd /workspace
python3 -m http.server 8080
```

Then open your browser to: `http://localhost:8080/mob-viewer/index.html`

### Option 2: Any Static File Server

You can use any static file server (nginx, Apache, Node.js http-server, etc.) to serve the `/workspace` directory.

## How It Works

The viewer loads:
1. **Sprite JSON files** from `RemixedDungeon/src/main/assets/spritesDesc/`
2. **Texture PNG files** from `RemixedDungeon/src/main/assets/`

Each sprite JSON defines:
- Texture filename
- Sprite dimensions (width x height)
- Animation definitions with:
  - FPS (frames per second)
  - Loop flag
  - Frame sequence (indexes into the sprite sheet)

## Supported Mobs

The viewer includes a predefined list of mobs. You can extend this list by adding more mob names to the `mobNames` array in `app.js`.

Available mobs include:
- Bat, Brute, Crab, Eye, Ghost, Gnoll, Golem, Goo
- King, Rat, Skeleton, SpiderQueen, Tengu, Zombie
- Sheep, Hedgehog, Bee, Wraith, Shadow, Mimic
- Lich, Monk, Thief, Shaman, Scorpio, DM300
- And many more (see `RemixedDungeon/src/main/assets/spritesDesc/`)

## Technical Details

- Pure HTML/CSS/JavaScript (no build step required)
- Uses Canvas API for rendering
- RequestAnimationFrame for smooth animation
- Pixel-perfect rendering with `image-rendering: pixelated`
- Responsive design with dark theme

## File Structure

```
/workspace/mob-viewer/
├── index.html      # Main HTML page
├── app.js          # Application logic
└── README.md       # This file
```

## Browser Compatibility

Works in all modern browsers that support:
- ES6 classes
- Fetch API
- Canvas API
- RequestAnimationFrame

Tested on Chrome, Firefox, and Edge.
