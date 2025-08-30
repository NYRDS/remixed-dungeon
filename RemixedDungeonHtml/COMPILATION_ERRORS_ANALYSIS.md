# HTML Compilation Errors Analysis

This document provides a detailed technical analysis of the compilation errors in the HTML version of Remixed Dungeon.

## Error 1: AndroidSAF.IListener Interface Incompatibility

### Files Involved
- `/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeon/src/main/java/com/nyrds/pixeldungeon/windows/WndInstallingMod.java`
- `/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/AndroidSAF.java`

### Technical Details
The `WndInstallingMod` class implements the `AndroidSAF.IListener` interface but only implements a subset of the methods defined in the HTML version of this interface.

#### HTML AndroidSAF.IListener Interface
```java
public interface IListener {
    void onFileSelected(String path);
    void onFileSelectionCancelled();
    void onMessage(String message);
    void onFileCopy(String path);
    void onFileSkip(String path);
    void onComplete();
    void onFileDelete(String entry);
}
```

#### WndInstallingMod Implementation
The `WndInstallingMod` class only implements:
- `onMessage(String message)`
- `onFileCopy(String path)`
- `onFileSkip(String path)`
- `onComplete()`
- `onFileDelete(String entry)`

It's missing implementations for:
- `onFileSelected(String path)`
- `onFileSelectionCancelled()`

### Root Cause
The HTML version of the `AndroidSAF` class has additional methods in its `IListener` interface that are not needed by `WndInstallingMod`. This is a design issue where the interface has been expanded with methods that aren't used by all implementing classes.

### Better Solution
Rather than adding unused method implementations to `WndInstallingMod`, we should simplify the HTML `AndroidSAF.IListener` interface to only include the methods that are actually used. This approach:
1. Reduces code complexity
2. Eliminates unused code
3. Maintains backward compatibility for classes that use the interface
4. Follows the Interface Segregation Principle

## Error 2-4: SystemText Constructor Signature Mismatches

### File Involved
`/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/gfx/SystemText.java`

### Technical Details
The `SystemText` class extends `com.watabou.noosa.Text` and its constructors are incorrectly calling the parent constructor.

#### Parent Text Class Constructor
```java
protected Text(float x, float y, float width, float height) {
    super(x, y, width, height);
}
```

#### Problematic Constructors in SystemText
1. ```java
   public SystemText(String text, float size, boolean multiline) {
       super(text, 0, 0, 0);  // Error: String cannot be converted to float
       // ...
   }
   ```

2. ```java
   public SystemText(String text, float x, float y, int align) {
       super(text, x, y, align);  // Error: String cannot be converted to float
       // ...
   }
   ```

3. ```java
   public SystemText(String text, float x, float y, int maxWidth, int align) {
       super(text, x, y, maxWidth, align);  // Error: Wrong number of parameters
       // ...
   }
   ```

### Root Cause
The `SystemText` constructors are attempting to pass the text string as the first parameter to the parent constructor, but the parent expects float values for positioning and sizing.

### Solution
The constructors should:
1. Call the parent constructor with appropriate float values for positioning and sizing
2. Set the text content separately after calling the parent constructor

Corrected constructors should look like:
1. ```java
   public SystemText(String text, float size, boolean multiline) {
       super(0, 0, 0, 0);  // Correct parameter types
       // Then set text content
       // ...
   }
   ```

2. ```java
   public SystemText(String text, float x, float y, int align) {
       super(x, y, 0, 0);  // Correct parameter types
       // Then set text content
       // ...
   }
   ```

3. ```java
   public SystemText(String text, float x, float y, int maxWidth, int align) {
       super(x, y, maxWidth, 0);  // Correct parameter types
       // Then set text content
       // ...
   }
   ```

## Error 5: IIapCallback Functional Interface Incompatibility

### Files Involved
- `/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeon/src/main/java/com/watabou/pixeldungeon/windows/WndHatInfo.java`
- `/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/market_none/java/com/nyrds/platform/support/IIapCallback.java`

### Technical Details
The `WndHatInfo` class uses a lambda expression with the `IIapCallback` interface, but this interface has multiple abstract methods, making it incompatible with lambda expressions.

#### HTML IIapCallback Interface
```java
public interface IIapCallback {
    void onPurchaseOk();
    void onPurchaseFail();
}
```

#### Problematic Code in WndHatInfo
```java
RemixedDungeon.instance().iap.doPurchase(accessory, () -> {
    // Lambda implementation
});
```

### Root Cause
Lambda expressions can only be used with functional interfaces (interfaces with exactly one abstract method). The `IIapCallback` interface has two abstract methods, so it's not a functional interface.

### Solution
Replace the lambda expression with an anonymous class implementation that explicitly implements both methods:

```java
RemixedDungeon.instance().iap.doPurchase(accessory, new IIapCallback() {
    @Override
    public void onPurchaseOk() {
        // Implementation for successful purchase
    }
    
    @Override
    public void onPurchaseFail() {
        // Implementation for failed purchase
    }
});
```

However, since the current code only provides one lambda, we need to determine which method it's intended to implement and provide a default implementation for the other.

## Summary

These compilation errors represent common issues when porting Android applications to HTML:
1. Interface expansion without considering all implementations
2. Constructor signature mismatches between parent and child classes
3. Incorrect use of lambda expressions with non-functional interfaces

The solutions focus on making minimal changes to the HTML platform code while maintaining compatibility with the core application logic.