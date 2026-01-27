# UI Composition Principles in Remixed Dungeon

## Overview

Remixed Dungeon uses a hierarchical component-based UI system inspired by the NOODLE engine. The UI architecture follows a tree structure where scenes contain groups, which in turn contain various UI elements. This document outlines the core principles and patterns used for UI composition in the game.

## Core Architecture

### Inheritance Hierarchy

The UI system is built on a hierarchical inheritance model:

```
Gizmo (Base class for all interactive elements)
├── Visual (Visual elements with position, scale, transform)
│   ├── Group (Container for other Gizmos)
│   │   ├── Scene (Top-level container representing a screen)
│   │   └── Component (UI components with layout capabilities)
│   │       ├── Button (Interactive UI element)
│   │       │   ├── TextButton (Button with text)
│   │       │   │   └── RedButton (Styled button with red theme)
│   │       │   │       ├── SystemRedButton (System-themed red button)
│   │       │   │       └── CheckBox (Checkable button with icon)
│   │       │   ├── DashboardItem (Game-specific dashboard button)
│   │       │   ├── IconButton (Button with icon and text)
│   │       │   ├── ImageButton (Button with only an image)
│   │       │   ├── ImageTextButton (Button with image and text)
│   │       │   ├── SimpleButton (Basic image button)
│   │       │   ├── Tag (Colored tag element)
│   │       │   └── Tool (Toolbar tool button)
│   │       ├── BasicBox (Layout container base class)
│   │       │   ├── VBox (Vertical layout container)
│   │       │   ├── HBox (Horizontal layout container)
│   │       │   └── VHBox (Grid-like layout container)
│   │       ├── ScrollPane (Scrollable container)
│   │       ├── Slider (Interactive slider control)
│   │       ├── GameLog (Game message log)
│   │       ├── StatusPane (Player status display)
│   │       ├── Toolbar (Player action toolbar)
│   │       ├── BuffIndicator (Buff status display)
│   │       ├── HealthIndicator (Health bar overlay)
│   │       ├── ItemSlot (Item display slot)
│   │       └── Window (Modal dialog windows)
│   ├── NinePatch (Scalable UI elements)
│   ├── Image (Visual image elements)
│   └── Text (Text rendering elements)
│       └── SystemText (Platform-specific text rendering)
├── TouchArea (Invisible touch event capture area)
└── Window (Modal dialog windows)
```

### Key UI Classes

#### Scene System
- **Scene**: Base class for all screens, handles lifecycle methods (`create`, `update`, `destroy`)
- **PixelScene**: Extended scene with pixel-perfect rendering and font management
- **TitleScene**: Main menu screen with dashboard items and buttons
- **GameScene**: Main gameplay screen

#### Component System
- **Component**: Base UI component with positioning and sizing capabilities
- **Button**: Interactive component with touch/mouse handling
- **TextButton**: Button with text label
- **RedButton**: Styled button with specific visual theme
- **SystemRedButton**: System-themed red button
- **CheckBox**: Checkable button with icon
- **IconButton**: Button with icon and text
- **ImageButton**: Button with only an image
- **ImageTextButton**: Button with image and text
- **SimpleButton**: Basic image button
- **Tag**: Colored tag element
- **Tool**: Toolbar tool button
- **DashboardItem**: Game-specific dashboard button with icon and label
- **BasicBox**: Abstract base class for layout containers with automatic measurement
- **VBox**: Vertical layout container that arranges children vertically
- **HBox**: Horizontal layout container that arranges children horizontally
- **VHBox**: Grid-like layout container that wraps content
- **ScrollPane**: Scrollable container for large content
- **Slider**: Interactive slider control
- **GameLog**: Game message log
- **StatusPane**: Player status display
- **Toolbar**: Player action toolbar
- **BuffIndicator**: Buff status display
- **HealthIndicator**: Health bar overlay
- **ItemSlot**: Item display slot
- **Window**: Modal dialog windows that overlay scenes
- **Window**: Modal dialog windows that overlay scenes

#### Layout Containers
- **VBox**: Vertical layout container that arranges children vertically
- **HBox**: Horizontal layout container that arranges children horizontally
- **BasicBox**: Abstract base class for layout containers with automatic measurement

## Composition Patterns

### Container-Child Relationship

The UI follows a container-child composition pattern:

```java
// Creating a container with child elements
Group container = new Group();
Button button = new RedButton("Click Me");
container.add(button);
scene.add(container);
```

Key characteristics:
- Groups act as containers that can hold multiple child elements
- Child elements inherit transformations from their parents
- Elements can be added, removed, and reordered within containers
- Scene is the root container for all UI elements

### Layout System

The UI uses a manual layout system:

```java
public class MyComponent extends Component {
    private Button button;
    
    @Override
    protected void createChildren() {
        button = new RedButton("OK");
        add(button);
    }
    
    @Override
    protected void layout() {
        // Position the button manually
        button.setPos(x + (width - button.width()) / 2, 
                     y + height - button.height() - MARGIN);
    }
}
```

Layout principles:
- Components define their own `layout()` method for positioning children
- Positioning is done through explicit coordinate setting
- Components can be aligned relative to each other using helper methods
- Layout is typically called when the component is resized or repositioned

### Event Handling

Event handling is implemented through multiple mechanisms:

```java
public class MyButton extends Button {
    @Override
    protected void onClick() {
        // Handle button click
        Game.toast("Button clicked!");
    }
    
    @Override
    protected void onTouchDown() {
        // Visual feedback when pressed
        bg.brightness(1.2f);
    }
    
    @Override
    protected void onTouchUp() {
        // Reset visual feedback
        bg.resetColor();
    }
}
```

Event handling approaches:
- **TouchArea**: Invisible areas that capture touch events
- **Signal.Listener**: Observer pattern for keyboard and system events
- Method overriding for specific interactions (`onClick`, `onTouchDown`, etc.)

## Rendering System

### Visual Elements

Visual elements inherit from the **Visual** class which provides:

- Position (x, y coordinates)
- Size (width, height)
- Scale and rotation transformations
- Color manipulation methods (tint, brightness, alpha)
- Matrix transformations for rendering

### Text Rendering

Text is handled through:

```java
// Creating text elements
Text title = PixelScene.createText("Game Title", GuiProperties.titleFontSize());
Text description = PixelScene.createMultiline("Description text", GuiProperties.regularFontSize());

// Adding markup support
Text highlighted = PixelScene.createMultilineHighlighted("_Highlighted text_", GuiProperties.regularFontSize());
```

Features:
- **Text** class for basic text rendering
- **SystemText** for platform-specific text rendering
- Font management through the **Font** class
- Support for markup in multiline text (italicized with underscores)

### NinePatch System

Scalable UI elements use the NinePatch system:

```java
// Creating scalable UI elements
NinePatch windowBg = Chrome.get(Chrome.Type.WINDOW);
NinePatch buttonBg = Chrome.get(Chrome.Type.BUTTON);

// Setting size maintains corner proportions
windowBg.size(200, 150);
```

Features:
- Divides images into 9 sections (corners, edges, center)
- Allows scaling without distorting corners
- Used for buttons, windows, and other UI chrome
- Defined through margin specifications

## Key UI Components

### Windows

Dialogs are implemented as **Window** components:

```java
public class WndExample extends Window {
    private static final int WIDTH = 120;
    private static final int MARGIN = 4;
    
    public WndExample() {
        super();
        
        // Set window size
        resize(WIDTH, 100);
        
        // Add content
        Text title = PixelScene.createText("Example Window", GuiProperties.titleFontSize());
        title.hardlight(TITLE_COLOR);
        title.setX((WIDTH - title.width()) / 2);
        title.setY(MARGIN);
        add(title);
        
        // Add close button
        add(new TouchArea(chrome) {
            @Override
            protected void onClick(Touch touch) {
                hide();
            }
        });
    }
}
```

Window features:
- Modal overlays that block interaction with underlying UI
- Customizable chrome styling
- Automatic camera management for proper rendering
- Keyboard navigation support

### Buttons

Interactive elements follow this hierarchy:

```java
// Basic button
Button basicButton = new Button() {
    @Override
    protected void onClick() {
        // Handle click
    }
};

// Text button
TextButton textButton = new TextButton("Click Me");

// Styled button
RedButton redButton = new RedButton("Important Action");
```

Button hierarchy:
- **Button**: Base interactive component
- **TextButton**: Button with text label
- **RedButton**: Styled button with specific visual theme
- Support for icons and custom styling

### Dashboard Items

Game-specific UI elements:

```java
public class DashboardItem extends Button {
    public static final float SIZE = 48;
    public static final int IMAGE_SIZE = 32;
    
    private Image image;
    private Text label;
    
    public DashboardItem(String text, int index) {
        super();
        
        // Set up the icon
        image.frame(image.texture.uvRect(index * IMAGE_SIZE, 0, 
                    (index + 1) * IMAGE_SIZE, IMAGE_SIZE));
        label.text(text);
        
        setSize(SIZE, SIZE);
    }
    
    @Override
    protected void createChildren() {
        super.createChildren();
        
        image = new Image(Assets.DASHBOARD);
        add(image);
        
        label = PixelScene.createText(GuiProperties.titleFontSize());
        add(label);
    }
    
    @Override
    protected void layout() {
        super.layout();
        
        // Position elements
        image.setX(PixelScene.align(x + (width - image.width()) / 2));
        image.setY(PixelScene.align(y));
        
        label.setX(PixelScene.align(x + (width - label.width()) / 2));
        label.setY(PixelScene.align(image.getY() + image.height() + 2));
    }
}
```

## Layout Containers (VBox and HBox)

Remixed Dungeon provides specialized layout containers to simplify UI arrangement:

### VBox (Vertical Box)

VBox arranges its children vertically:

```java
// Create a vertical layout container
VBox vbox = new VBox();
vbox.setGap(2); // Set spacing between elements
vbox.setAlign(VBox.Align.Top); // Alignment options: Top, Bottom, Center

// Add UI elements
vbox.add(new RedButton("Button 1"));
vbox.add(new RedButton("Button 2"));
vbox.add(new RedButton("Button 3"));

// Position the container
vbox.setPos(10, 10);
add(vbox);
```

VBox features:
- Automatic vertical arrangement of children
- Configurable gap between elements
- Vertical alignment options (Top, Bottom, Center)
- Automatic size calculation based on children

### HBox (Horizontal Box)

HBox arranges its children horizontally:

```java
// Create a horizontal layout container
HBox hbox = new HBox(200); // Maximum width
hbox.setGap(2); // Set spacing between elements
hbox.setAlign(HBox.Align.Left); // Alignment options: Left, Right, Center, Width

// Add UI elements
hbox.add(new RedButton("Button 1"));
hbox.add(new RedButton("Button 2"));
hbox.add(new RedButton("Button 3"));

// Position the container
hbox.setPos(10, 10);
add(hbox);
```

HBox features:
- Automatic horizontal arrangement of children
- Configurable gap between elements
- Horizontal alignment options (Left, Right, Center, Width)
- Vertical alignment options for children (Top, Bottom, Center)
- Automatic size calculation based on children

### Combined Usage

VBox and HBox can be combined for complex layouts:

```java
// Create a vertical container for the main layout
VBox mainVbox = new VBox();
mainVbox.setGap(4);

// Create horizontal rows
HBox row1 = new HBox(200);
row1.setAlign(HBox.Align.Center);
row1.add(new Image(Assets.ICONS, 0, 0, 16, 16));
row1.add(PixelScene.createText("Title", GuiProperties.titleFontSize()));

HBox row2 = new HBox(200);
row2.setGap(2);
row2.add(new RedButton("OK"));
row2.add(new RedButton("Cancel"));

// Add rows to the vertical container
mainVbox.add(row1);
mainVbox.add(row2);

// Position the layout
mainVbox.setPos(10, 10);
add(mainVbox);
```

### IPlaceable Interface

Both VBox and HBox work with elements that implement the **IPlaceable** interface, which defines the contract for layout-capable UI elements:

```java
public interface IPlaceable {
    float width();
    float height();
    void setPos(float x, float y);
    float getX();
    float getY();
    IPlaceable shadowOf();
}
```

Key points about IPlaceable:
- **width()** and **height()**: Return the dimensions of the element
- **setPos()**: Sets the position of the element
- **getX()** and **getY()**: Return the current position of the element
- **shadowOf()**: Allows elements to "shadow" other elements, essentially acting as a proxy

Classes that implement IPlaceable include:
- All Component subclasses (Button, TextButton, RedButton, etc.)
- VBox and HBox (allowing nested layouts)
- Any custom UI element that properly implements the interface

### BasicBox Base Class

Both VBox and HBox inherit from the **BasicBox** abstract class, which provides common functionality for layout containers:

```java
public abstract class BasicBox extends Component {
    protected boolean dirty = true;
    
    @Override
    public void measure() {
        if (dirty) {
            _measure();
            dirty = false;
        }
    }
    
    @Override
    public Gizmo add(Gizmo g) {
        dirty = true;
        if(g instanceof Component) {
            ((Component)g).measure();
        }
        return super.add(g);
    }
    
    protected abstract void _measure();
}
```

Key features of BasicBox:
- **Automatic measurement**: Recalculates dimensions when children are added/removed
- **Dirty flag system**: Optimizes performance by only recalculating when needed
- **Abstract _measure()**: Subclasses implement their specific measurement logic
- **Integration with Component system**: Works seamlessly with the existing UI hierarchy

## Specialized UI Components

Remixed Dungeon includes several specialized UI components for common game UI patterns:

### BuffIndicator

Displays active buffs/debuffs as small icons:

```java
// Create a buff indicator for a character
BuffIndicator buffs = new BuffIndicator(hero);
add(buffs);
```

Features:
- Automatically updates when character's buffs change
- Shows buff icons with fade-in/fade-out animations
- Touch support to view buff details
- Color-coded icons for different buff types

### HealthIndicator

Displays a health bar overlay above a character:

```java
// Create a health indicator
HealthIndicator health = new HealthIndicator();
health.target(enemy); // Attach to a character
add(health);
```

Features:
- Follows character position automatically
- Updates in real-time as health changes
- Color changes based on health percentage
- Automatically hides when character is not visible

### ItemSlot

Displays items with status information:

```java
// Create an item slot
ItemSlot slot = new ItemSlot();
slot.item(sword); // Display a specific item
slot.enable(true); // Enable/disable interaction
add(slot);
```

Features:
- Shows item image with overlays
- Displays upgrade level and status text
- Color-coded text for item quality
- Particle effects for special items
- Configurable display options

### GameLog

Displays game messages in a scrolling log:

```java
// Create a game log
GameLog log = new GameLog();
add(log);
```

Features:
- Automatic message wrapping and formatting
- Color-coded messages (positive, negative, warning, etc.)
- Automatic scrolling to show new messages
- Message history management
- Integration with GLog system

### StatusPane

Displays player status information:

```java
// Create a status pane
StatusPane status = new StatusPane(hero, level);
add(status);
```

Features:
- Health and mana bars
- Level and depth information
- Key count display
- Buff indicator integration
- Danger and loot indicators
- Menu buttons

### Toolbar

Displays player action buttons:

```java
// Create a toolbar
Toolbar toolbar = new Toolbar(hero);
add(toolbar);
```

Features:
- Quickslot buttons
- Action buttons (wait, search, info)
- Inventory button
- Spell button (for spellcasters)
- Configurable layout and positioning
- Hotkey support

### ScrollPane

Provides scrolling for large content:

```java
// Create a scroll pane with content
ScrollPane scroll = new ScrollPane(contentComponent);
scroll.setRect(x, y, width, height);
add(scroll);
```

Features:
- Touch-based scrolling
- Automatic scroll bounds management
- Click handling for content
- Camera management for proper rendering

### Slider

Interactive slider control for numeric values:

```java
// Create a slider
Slider slider = new Slider("Volume", "Low", "High", 0, 100) {
    @Override
    protected void onChange() {
        // Handle value change
        int volume = getSelectedValue();
    }
};
slider.setSelectedValue(50); // Set initial value
add(slider);
```

Features:
- Customizable range and labels
- Touch drag interaction
- Visual feedback during interaction
- Value change callback
- Automatic positioning of slider node

### CheckBox

Checkable button with icon:

```java
// Create a checkbox
CheckBox checkbox = new CheckBox("Enable Sound", true);
checkbox.setChecked(false); // Set initial state
add(checkbox);
```

Features:
- Checked/unchecked state with visual icons
- Automatic icon switching
- Click handling to toggle state
- Label text support

### Tag

Colored tag element for status indicators:

```java
// Create a tag
Tag tag = new Tag(0xFF0000); // Red color
tag.flash(); // Flash animation
add(tag);
```

Features:
- Customizable color
- Flash animation support
- NinePatch background
- Automatic size management

## Icon Management

Remixed Dungeon provides a centralized system for managing UI icons through the **Icons** enum:

```java
// Get an icon
Image icon = Icons.get(Icons.CLOSE);

// Use icons with buttons
IconButton button = new IconButton("Close", Icons.get(Icons.CLOSE));

// Use icons with checkboxes
CheckBox checkbox = new CheckBox("Option", Icons.get(Icons.CHECKED));
```

Key features of the Icons system:
- Centralized icon definitions
- Automatic texture management
- Consistent sizing and positioning
- Support for multiple icon sheets
- Easy to extend with new icons

The Icons enum includes common UI symbols like:
- Navigation controls (CLOSE, BACK, etc.)
- Status indicators (WARNING, ALERT, etc.)
- Game-specific icons (SKULL, COMPASS, etc.)
- Hero class icons (WARRIOR, MAGE, etc.)
- Social media icons (VK, FB, TG, DISCORD)
- UI elements (CHECKED, UNCHECKED, etc.)

## Tabbed Interfaces

Remixed Dungeon provides a tabbed interface system for organizing content in dialogs:

```java
// Create a tabbed window
public class MyTabbedWindow extends WndTabbed {
    public MyTabbedWindow() {
        super();
        
        // Create tabs
        Tab tab1 = new LabeledTab(this, "Tab 1");
        Tab tab2 = new LabeledTab(this, "Tab 2");
        
        // Add tabs to window
        add(tab1);
        add(tab2);
        
        // Select initial tab
        select(0);
    }
}
```

Key components of the tabbed interface system:
- **WndTabbed**: Base class for tabbed windows
- **Tab**: Base class for tab buttons
- **LabeledTab**: Tab with text label
- **TabContent**: Base class for tab content areas

Features:
- Automatic tab positioning and sizing
- Visual selection feedback
- Content switching with proper layout
- Integration with layout containers
- Scrollable content support

## Advanced UI Patterns

### Scrollable Lists

For displaying large lists of items, Remixed Dungeon provides scrollable list components:

```java
// Create a scrollable list
Component content = new Component();
ScrollPane scrollList = new ScrollableList(content);

// Add items to the list
for (int i = 0; i < items.size(); i++) {
    ListItem item = new ListItem(items.get(i));
    item.setRect(0, y, width, ITEM_HEIGHT);
    content.add(item);
    y += ITEM_HEIGHT;
}

// Set content size for proper scrolling
content.setSize(width, y);
add(scrollList);
```

Features:
- Touch-based scrolling
- Automatic bounds management
- Click handling for list items
- Integration with IClickable interface
- Performance optimization for large lists

### Dialog Windows

The Window system provides a foundation for creating modal dialogs:

```java
// Create a custom dialog
public class MyDialog extends Window {
    public MyDialog() {
        super();
        
        // Add content
        Text title = PixelScene.createText("My Dialog", GuiProperties.titleFontSize());
        add(title);
        
        // Resize window to fit content
        resize(WIDTH, HEIGHT);
    }
}
```

Key features of the Window system:
- Modal behavior with background blocking
- Customizable chrome styling
- Automatic camera management
- Keyboard navigation support
- Proper cleanup on destroy

### Event Propagation

The UI system supports event propagation through interfaces like IClickable:

```java
// Implement click handling in custom components
public class MyListItem extends Component implements IClickable {
    @Override
    public boolean onClick(float x, float y) {
        if (inside(x, y)) {
            // Handle click
            return true;
        }
        return false;
    }
}
```

Benefits of event propagation:
- Consistent event handling across components
- Proper event bubbling through container hierarchy
- Support for complex interaction patterns
- Integration with scrollable containers

## Styling and Theming

### Chrome System

UI styling is managed through the **Chrome** class:

```java
// Different chrome types for different UI elements
NinePatch windowChrome = Chrome.get(Chrome.Type.WINDOW);
NinePatch buttonChrome = Chrome.get(Chrome.Type.BUTTON);
NinePatch toastChrome = Chrome.get(Chrome.Type.TOAST);
```

Chrome types:
- WINDOW: Standard dialog windows
- BUTTON: Interactive buttons
- TOAST: Notification-style messages
- TAG: Attachable UI elements
- GEM: Circular UI elements
- TAB_*: Tabbed interface elements

### Color Management

Color manipulation is handled through:

```java
// Direct color methods
element.tint(0xFF0000, 0.5f);  // Red tint with 50% alpha
element.hardlight(0x00FF00);   // Green hard light
element.color(0x0000FF);       // Blue color
element.alpha(0.8f);           // 80% opacity
```

Methods:
- Direct color methods (tint, hardlight, color)
- Alpha blending for transparency
- Brightness and lightness adjustments

## Scene Composition Example

Looking at the **TitleScene**, we can see how these principles work together:

```java
public class TitleScene extends PixelScene {
    @Override
    public void create() {
        super.create();
        
        // Create background elements
        Archs archs = new Archs();
        archs.setSize(Camera.main.width, Camera.main.height);
        add(archs);
        
        // Create UI elements
        DashboardItem btnStart = new DashboardItem("Start", 0);
        btnStart.setPos(centerX - btnStart.width(), centerY - dashboardPos);
        add(btnStart);
        
        // Add exit button
        ExitButton btnExit = new ExitButton();
        btnExit.setPos(Camera.main.width - btnExit.width(), 0);
        add(btnExit);
        
        fadeIn();
    }
}
```

Scene composition steps:
1. **Scene Setup**: Creates a PixelScene with appropriate camera setup
2. **Element Addition**: Adds UI elements to the scene using `add()`
3. **Positioning**: Manually positions elements using `setPos()`
4. **Layout Adjustments**: Adapts layout for different screen orientations
5. **Event Handling**: Implements button click handlers for navigation
6. **Visual Effects**: Adds background elements and animations

## Best Practices

### Component Design

1. **Separation of Concerns**: Keep UI logic separate from game logic
2. **Reusability**: Design components to be reusable across different contexts
3. **Composition Over Inheritance**: Favor combining simple components rather than deep inheritance hierarchies

### Performance Considerations

1. **Lazy Initialization**: Create UI elements only when needed
2. **Memory Management**: Properly destroy components to prevent memory leaks
3. **Batch Updates**: Group UI updates to minimize redraws

### Platform Adaptation

1. **Responsive Design**: Adapt layouts for different screen sizes and orientations
2. **Input Methods**: Handle both touch and keyboard input appropriately
3. **Platform-Specific Features**: Leverage platform capabilities when available

## Advanced UI Patterns

### Scrollable Lists

For displaying large lists of items, Remixed Dungeon provides scrollable list components:

```java
// Create a scrollable list
Component content = new Component();
ScrollPane scrollList = new ScrollableList(content);

// Add items to the list
for (int i = 0; i < items.size(); i++) {
    ListItem item = new ListItem(items.get(i));
    item.setRect(0, y, width, ITEM_HEIGHT);
    content.add(item);
    y += ITEM_HEIGHT;
}

// Set content size for proper scrolling
content.setSize(width, y);
add(scrollList);
```

Features:
- Touch-based scrolling
- Automatic bounds management
- Click handling for list items
- Integration with IClickable interface
- Performance optimization for large lists

### Dialog Windows

The Window system provides a foundation for creating modal dialogs:

```java
// Create a custom dialog
public class MyDialog extends Window {
    public MyDialog() {
        super();
        
        // Add content
        Text title = PixelScene.createText("My Dialog", GuiProperties.titleFontSize());
        add(title);
        
        // Resize window to fit content
        resize(WIDTH, HEIGHT);
    }
}
```

Key features of the Window system:
- Modal behavior with background blocking
- Customizable chrome styling
- Automatic camera management
- Keyboard navigation support
- Proper cleanup on destroy

### Tabbed Interfaces

Remixed Dungeon provides a tabbed interface system for organizing content in dialogs:

```java
// Create a tabbed window
public class MyTabbedWindow extends WndTabbed {
    public MyTabbedWindow() {
        super();
        
        // Create tabs
        Tab tab1 = new LabeledTab(this, "Tab 1");
        Tab tab2 = new LabeledTab(this, "Tab 2");
        
        // Add tabs to window
        add(tab1);
        add(tab2);
        
        // Select initial tab
        select(0);
    }
}
```

Key components of the tabbed interface system:
- **WndTabbed**: Base class for tabbed windows
- **Tab**: Base class for tab buttons
- **LabeledTab**: Tab with text label
- **TabContent**: Base class for tab content areas

Features:
- Automatic tab positioning and sizing
- Visual selection feedback
- Content switching with proper layout
- Integration with layout containers
- Scrollable content support

### Event Propagation

The UI system supports event propagation through interfaces like IClickable:

```java
// Implement click handling in custom components
public class MyListItem extends Component implements IClickable {
    @Override
    public boolean onClick(float x, float y) {
        if (inside(x, y)) {
            // Handle click
            return true;
        }
        return false;
    }
}
```

Benefits of event propagation:
- Consistent event handling across components
- Proper event bubbling through container hierarchy
- Support for complex interaction patterns
- Integration with scrollable containers

## Enhanced Best Practices

### Component Design

1. **Separation of Concerns**: Keep UI logic separate from game logic
2. **Reusability**: Design components to be reusable across different contexts
3. **Composition Over Inheritance**: Favor combining simple components rather than deep inheritance hierarchies
4. **Use Layout Containers**: Leverage VBox, HBox, and VHBox for complex arrangements
5. **Implement IPlaceable**: For custom components that need layout support
6. **Leverage Tabbed Interfaces**: Use WndTabbed for organizing complex dialog content
7. **Handle Events Properly**: Implement IClickable for custom interactive components

### Performance Considerations

1. **Lazy Initialization**: Create UI elements only when needed
2. **Memory Management**: Properly destroy components to prevent memory leaks
3. **Batch Updates**: Group UI updates to minimize redraws
4. **Dirty Flag Pattern**: Use dirty flags to avoid unnecessary calculations
5. **Recycling**: Reuse components when possible using the recycle() method
6. **Efficient Scrolling**: Use ScrollableList for large datasets
7. **Optimize Layout**: Minimize layout() calls by caching dimensions when possible

### Platform Adaptation

1. **Responsive Design**: Adapt layouts for different screen sizes and orientations
2. **Input Methods**: Handle both touch and keyboard input appropriately
3. **Platform-Specific Features**: Leverage platform capabilities when available
4. **Scalable Assets**: Use NinePatch for scalable UI elements
5. **Text Rendering**: Use SystemText for platform-optimized text rendering
6. **Screen Density**: Account for different pixel densities in positioning
7. **Orientation Changes**: Handle landscape and portrait layouts properly

## UI Testing and Debugging

### Visual Debugging

Remixed Dungeon includes several tools for UI debugging:

```java
// Enable UI camera visibility for debugging
PixelScene.uiCamera.setVisible(true);

// Use alignment helpers for pixel-perfect positioning
float alignedX = PixelScene.align(x);
float alignedY = PixelScene.align(y);

// Visualize component boundaries during development
// (This would typically be conditional compilation)
```

### Common UI Issues and Solutions

1. **Layout Problems**:
   - Ensure proper measurement in `_measure()` methods
   - Call `layout()` after changing component properties
   - Use `PixelScene.align()` for pixel-perfect positioning

2. **Performance Issues**:
   - Minimize layout recalculations with dirty flags
   - Avoid creating components in draw/update loops
   - Use component recycling when possible

3. **Touch/Input Issues**:
   - Verify TouchArea boundaries
   - Check component visibility before handling events
   - Ensure proper event propagation through container hierarchy

### Testing Strategies

1. **Manual Testing**:
   - Test on different screen sizes and densities
   - Verify both portrait and landscape orientations
   - Check touch interactions on various devices

2. **Automated Testing**:
   - Unit test layout calculations
   - Verify component creation and destruction
   - Test event handling logic

## New UI Components and Patterns

### Dynamic Indicators in Item Slots

The `ItemButton` class in `ItemButton.java` was enhanced with dynamic indicator capabilities:

```java
// ItemButton now supports dynamic indicators
public class ItemButton extends ItemSlot {
    private BitmapText alchemyIndicator; // Dynamic indicator text

    // Shows contextual indicators based on item properties
    // Positioned in top-right corner of item slot
    // Conditionally visible based on context
}
```

Concrete implementation:
- **ItemButton.java**: The enhanced item slot component
- **BitmapText**: Used for the indicator text
- **PixelScene.font**: Font used for the indicator
- **setVisible()**: Method to control indicator visibility
- **WndBag.Mode**: Determines when indicators are shown

Pattern features:
- **Dynamic Indicators**: Visual indicators can be added to item slots
- **Context-Aware**: Visibility determined by current UI context (WndBag.Mode.ALL, WndBag.Mode.QUICKSLOT)
- **Visual Positioning**: Indicators placed in consistent positions (top-right corner)
- **Conditional Logic**: Indicators appear based on item properties or game state
- **Extensible Design**: Framework for adding various types of indicators

### Single-Line Data Display

A pattern for displaying complex data in a single-line format, implemented in `WndItemAlchemy.java`:

```java
// Format complex data relationships in a single line
StringBuilder display = new StringBuilder();
display.append("Prefix: ");

// Add first group of items
boolean first = true;
for (Map.Entry<String, Integer> entry : dataMap.entrySet()) {
    if (!first) {
        display.append(" + ");
    }
    first = false;

    String item = entry.getKey();
    int count = entry.getValue();

    if (count > 1) {
        display.append(item).append(" x").append(count);
    } else {
        display.append(item);
    }
}

display.append(" → "); // Separator between input and output

// Add second group of items with similar pattern
```

Concrete implementation:
- **WndItemAlchemy.java**: Contains the single-line display implementation
- **StringBuilder**: Used for efficient string construction
- **HashMap**: For counting item occurrences
- **ItemFactory.itemByName()**: For retrieving item names
- **Text**: For displaying the formatted string

Pattern components:
- **Compact Layout**: Single line reduces vertical space usage
- **Group Separation**: Clear distinction between different data groups
- **Quantity Display**: Efficient notation for repeated items ("x2", "x3", etc.)
- **Connector Symbols**: Visual separators ("+", "→") for different relationships
- **Conditional Formatting**: Adjusts display based on data properties

### Flexible Layout Spacing

A technique for positioning UI elements at specific locations within containers, implemented in `WndItemAlchemy.java`:

```java
// Add flexible spacer to push elements to specific positions
float totalHeight = container.bottom();
float targetY = windowHeight - margin - buttons.height();
float availableSpace = targetY - totalHeight;

if (availableSpace > 0) {
    // Add empty space to push buttons to the bottom
    Component spacer = new Component() {
        @Override
        public void layout() {
            height = availableSpace;
        }
    };
    container.add(spacer);
}
```

Concrete implementation:
- **WndItemAlchemy.java**: Uses flexible spacing to position buttons
- **Component**: Custom component for flexible spacing
- **VBox**: Layout container that manages the spacing
- **layout()**: Method overridden to set dynamic height
- **add()**: Method to insert spacer in the container hierarchy

Technique benefits:
- **Bottom Alignment**: Positions elements at bottom of container
- **Flexible Spacing**: Dynamically adjusts to fill available space
- **Responsive Layout**: Adapts to different content sizes
- **Clean Positioning**: Maintains consistent element placement
- **Container Independence**: Works with various container types

### Contextual Item Filtering

A pattern for filtering and displaying items based on contextual requirements, implemented in `ItemButton.java`:

```java
// Filter items based on current context and player state
Map<String, Integer> playerInventory = new HashMap<>();
for (Item inventoryItem : Dungeon.hero.getBelongings().backpack.items) {
    String itemName = inventoryItem.getEntityKind();
    playerInventory.put(itemName, playerInventory.getOrDefault(itemName, 0) + inventoryItem.quantity());
}

// Apply filtering logic based on context
List<FilteredItem> filteredItems = new ArrayList<>();
for (Item candidate : allItems) {
    if (shouldShowItem(candidate, playerInventory, context)) {
        filteredItems.add(candidate);
    }
}
```

Concrete implementation:
- **ItemButton.java**: Contains the filtering logic
- **AlchemyRecipes.getRecipesContainingItem()**: Gets recipes for specific items
- **AlchemyRecipes.hasRequiredIngredients()**: Checks if player has required ingredients
- **Dungeon.hero.getBelongings()**: Accesses player's inventory
- **Item.getEntityKind()**: Gets the internal name of items

Pattern features:
- **Dynamic Filtering**: Items filtered based on current context
- **State Awareness**: Considers player state and inventory
- **Efficient Processing**: Uses maps for quick lookups
- **Context Sensitivity**: Behavior adapts to different UI modes
- **Scalable Design**: Can handle varying numbers of items

### Responsive Window Design

Principles for creating windows that adapt to different content and screen sizes, implemented in `WndItemAlchemy.java`:

- **WndItemAlchemy.java**: Implements responsive window design
- **VBox**: Vertical layout container with adjustable spacing
- **HBox**: Horizontal container for buttons
- **PixelScene.uiCamera**: References screen dimensions
- **resize()**: Method to adjust window size
- **layout()**: Method to recalculate positions

Specific implementations:
- **Tighter Margins**: Reduced margins (from 20px to 10px) for more efficient space usage
- **Consistent Gaps**: Uniform spacing using SMALL_GAP constant (1px)
- **Adaptive Sizing**: Elements adjust to available window space
- **Flexible Components**: UI elements that can expand or contract as needed
- **Content Prioritization**: Important elements remain visible regardless of space constraints
- **Layout Hierarchy**: Proper ordering of elements for optimal visual flow

### Integration with Existing UI Systems

New components integrate seamlessly with existing UI patterns:

- **WndBag Compatibility**: Works with existing inventory selection system (WndBag.Mode.ALL, WndBag.Mode.QUICKSLOT)
- **Component Hierarchy**: Follows established inheritance patterns (ItemButton extends ItemSlot)
- **Window Management**: Conforms to standard window creation (extends Window)
- **Layout Containers**: Uses VBox and HBox for consistent layout management
- **Event Handling**: Integrates with existing touch and click handling systems
- **Theming Consistency**: Maintains visual consistency with existing UI elements

This architecture provides a flexible yet lightweight UI system that's well-suited for a mobile game, with clear separation of concerns between visual rendering, interaction handling, and game logic integration.