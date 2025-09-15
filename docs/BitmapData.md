# BitmapData Interface Documentation

The BitmapData class provides a cross-platform interface for creating and manipulating bitmap images in Remixed Pixel Dungeon. It can be used from Lua scripts to generate images, draw shapes, and manipulate pixels.

## Creating BitmapData Objects

### Constructor Approach (Recommended)
Creates a new bitmap with the specified dimensions using the class constructor.

**Parameters:**
- `width` (number): Width of the bitmap in pixels
- `height` (number): Height of the bitmap in pixels

**Returns:**
- `BitmapData`: A new bitmap object

**Example:**
```lua
local bitmap = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)
```

### `createBitmap(width, height)` (Deprecated)
Creates a new bitmap with the specified dimensions using the static factory method.

**Parameters:**
- `width` (number): Width of the bitmap in pixels
- `height` (number): Height of the bitmap in pixels

**Returns:**
- `BitmapData`: A new bitmap object

**Example:**
```lua
local bitmapClass = RPD.new("com.nyrds.platform.gfx.BitmapData")
local bitmap = bitmapClass:createBitmap(100, 100)
```

Note: The constructor approach is now recommended as it's more reliable and works consistently across platforms.

## Basic Properties

### `getWidth()`
Returns the width of the bitmap.

**Returns:**
- `number`: Width in pixels

### `getHeight()`
Returns the height of the bitmap.

**Returns:**
- `number`: Height in pixels

## Color Handling

### `color(color)`
Converts an ARGB color value to the platform-specific format.

**Parameters:**
- `color` (number): Color in ARGB format (0xAARRGGBB)

**Returns:**
- `number`: Color in platform-specific format

### `colorFromComponents(alpha, red, green, blue)`
Creates a color from individual components to avoid precision issues with Lua's numeric type.

**Parameters:**
- `alpha` (number): Alpha component (0-255)
- `red` (number): Red component (0-255)
- `green` (number): Green component (0-255)
- `blue` (number): Blue component (0-255)

**Returns:**
- `number`: Color in ARGB format

## Drawing Methods

### `clear(color)`
Fills the entire bitmap with a solid color.

**Parameters:**
- `color` (number): The color to fill with, in ARGB format (0xAARRGGBB)

### `setPixel(x, y, color)`
Sets the color of a single pixel.

**Parameters:**
- `x` (number): X coordinate
- `y` (number): Y coordinate
- `color` (number): Color in ARGB format (0xAARRGGBB)

### `getPixel(x, y)`
Gets the color of a pixel.

**Parameters:**
- `x` (number): X coordinate
- `y` (number): Y coordinate

**Returns:**
- `number`: Color in ARGB format (0xAARRGGBB)

### `drawLine(startX, startY, endX, endY, color)`
Draws a line between two points.

**Parameters:**
- `startX` (number): Starting X coordinate
- `startY` (number): Starting Y coordinate
- `endX` (number): Ending X coordinate
- `endY` (number): Ending Y coordinate
- `color` (number): Line color in ARGB format (0xAARRGGBB)

### `drawRect(left, top, right, bottom, color)`
Draws a rectangle outline.

**Parameters:**
- `left` (number): Left edge coordinate
- `top` (number): Top edge coordinate
- `right` (number): Right edge coordinate
- `bottom` (number): Bottom edge coordinate
- `color` (number): Outline color in ARGB format (0xAARRGGBB)

### `fillRect(left, top, right, bottom, color)`
Draws a filled rectangle.

**Parameters:**
- `left` (number): Left edge coordinate
- `top` (number): Top edge coordinate
- `right` (number): Right edge coordinate
- `bottom` (number): Bottom edge coordinate
- `color` (number): Fill color in ARGB format (0xAARRGGBB)

### `drawCircle(centerX, centerY, radius, color)`
Draws a circle outline.

**Parameters:**
- `centerX` (number): X coordinate of circle center
- `centerY` (number): Y coordinate of circle center
- `radius` (number): Circle radius
- `color` (number): Outline color in ARGB format (0xAARRGGBB)

### `fillCircle(centerX, centerY, radius, color)`
Draws a filled circle.

**Parameters:**
- `centerX` (number): X coordinate of circle center
- `centerY` (number): Y coordinate of circle center
- `radius` (number): Circle radius
- `color` (number): Fill color in ARGB format (0xAARRGGBB)

### `copyRect(srcBitmap, srcX, srcY, width, height, dstX, dstY)`
Copies a rectangular region from another bitmap to this one.

**Parameters:**
- `srcBitmap` (BitmapData): Source bitmap to copy from
- `srcX` (number): X coordinate of source rectangle
- `srcY` (number): Y coordinate of source rectangle
- `width` (number): Width of rectangle to copy
- `height` (number): Height of rectangle to copy
- `dstX` (number): X coordinate of destination
- `dstY` (number): Y coordinate of destination

### `rectCopy(srcBitmap, srcX, srcY, width, height, dstX, dstY)`
Copies a rectangular region from another bitmap to this one, respecting source alpha.
This method blends the source pixels with the destination based on the source pixel's alpha channel.

**Parameters:**
- `srcBitmap` (BitmapData): Source bitmap to copy from
- `srcX` (number): X coordinate of source rectangle
- `srcY` (number): Y coordinate of source rectangle
- `width` (number): Width of rectangle to copy
- `height` (number): Height of rectangle to copy
- `dstX` (number): X coordinate of destination
- `dstY` (number): Y coordinate of destination

## Utility Methods

### `isEmptyPixel(x, y)`
Checks if a pixel is transparent (alpha = 0).

**Parameters:**
- `x` (number): X coordinate
- `y` (number): Y coordinate

**Returns:**
- `boolean`: True if pixel is transparent

### `savePng(path)`
Saves the bitmap to a PNG file.

**Parameters:**
- `path` (string): File path to save to

### `dispose()`
Releases the bitmap resources. Note: Currently this just sets the reference to null to avoid native crashes.

## Color Format

All colors are specified in ARGB format:
- `0xAARRGGBB` where:
  - `AA` is the alpha channel (00 = transparent, FF = opaque)
  - `RR` is the red component
  - `GG` is the green component
  - `BB` is the blue component

## Common Colors

- Opaque red: `0xFFFF0000`
- Opaque green: `0xFF00FF00`
- Opaque blue: `0xFF0000FF`
- Opaque white: `0xFFFFFFFF`
- Transparent: `0x00000000`

## Example Usage

```lua
-- Create a new bitmap using the recommended constructor approach
local bitmap = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)

-- Fill with blue background
bitmap:clear(0xFF0000FF)

-- Draw a red rectangle
bitmap:fillRect(10, 10, 90, 90, 0xFFFF0000)

-- Draw a green circle
bitmap:fillCircle(50, 50, 30, 0xFF00FF00)

-- Draw a white line
bitmap:drawLine(0, 0, 100, 100, 0xFFFFFFFF)

-- Create a second bitmap
local bitmap2 = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)

-- Fill second bitmap with yellow background
bitmap2:clear(0xFFFFFF00)

-- Copy a region from bitmap to bitmap2
bitmap2:copyRect(bitmap, 25, 25, 50, 50, 25, 25)

-- Create a third bitmap with semi-transparent content for alpha blending
local alphaBitmap = RPD.new("com.nyrds.platform.gfx.BitmapData", 50, 50)
alphaBitmap:clear(0x80FF0000)  -- 50% transparent red

-- Create a fourth bitmap to demonstrate alpha blending
local bitmap4 = RPD.new("com.nyrds.platform.gfx.BitmapData", 100, 100)
bitmap4:clear(0xFF0000FF)  -- Blue background

-- Copy with alpha blending
bitmap4:rectCopy(alphaBitmap, 0, 0, 50, 50, 25, 25)

-- Save to files
bitmap:savePng("output.png")
bitmap2:savePng("output_copy.png")
bitmap4:savePng("output_rectcopy.png")

-- Clean up
bitmap:dispose()
bitmap2:dispose()
alphaBitmap:dispose()
bitmap4:dispose()
```

## Platform Notes

The BitmapData interface works consistently across both Android and desktop platforms, with proper handling of alpha blending and cross-platform file saving. The constructor approach (`RPD.new("com.nyrds.platform.gfx.BitmapData", width, height)`) is the recommended way to create BitmapData objects as it's more reliable and works on both platforms.