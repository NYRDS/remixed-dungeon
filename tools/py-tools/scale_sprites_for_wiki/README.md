# Sprite Scaling Tool for Remixed Dungeon Wiki

This tool scales raw sprites from the `sprites/` directory for better visualization in the Remixed Dungeon wiki.

## Features

- Scales sprites 8x using nearest neighbor interpolation
- Extends canvas by 1 transparent pixel in each direction before scaling (to prevent edge artifacts)
- Adds a background and frame for better visualization
- Preserves transparency
- Processes all supported image formats in the input directory

## Usage

```bash
# Basic usage
python tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-sprites/

# Custom scale factor
python tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-sprites/ -s 10

# Custom canvas extension (add transparent pixels before scaling)
python tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-sprites/ -c 2

# Custom colors (R G B values 0-255 each)
python tools/py-tools/scale_sprites_for_wiki.py -i sprites/ -o wiki-sprites/ --bg-color 255 255 255 --frame-color 0 0 0
```

## Options

- `-i, --input`: Input directory containing raw sprites (required)
- `-o, --output`: Output directory for enhanced sprites (required)
- `-s, --scale`: Scale factor (default: 8)
- `-c, --canvas-extension`: Number of transparent pixels to add in each direction before scaling (default: 1)
- `--bg-color`: Background color as R G B values (default: 240 240 240)
- `--frame-color`: Frame color as R G B values (default: 100 100 100)

## Output

The tool creates enhanced versions of all sprites with the following characteristics:

- **Canvas Extension**: 1 transparent pixel added in each direction before scaling (to prevent edge artifacts)
- **Scaling**: 8x (or custom scale factor) using nearest neighbor interpolation
- **Background**: Light gray background (customizable)
- **Frame**: Dark gray frame, 2 pixels thick (customizable)
- **Transparency**: Preserved from original sprites
- **Format**: PNG with transparency support