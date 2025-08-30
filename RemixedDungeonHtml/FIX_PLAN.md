# HTML Compilation Errors Fix Plan

This document outlines the specific steps needed to fix the HTML compilation errors in Remixed Dungeon.

## Overview

There are 5 compilation errors in the HTML version that need to be addressed. All fixes will be made only in the HTML platform code without affecting other platforms.

## Error 1: AndroidSAF.IListener Interface Incompatibility

### File to Modify
`/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/AndroidSAF.java`

### Current Implementation
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

### Fix
Simplify the interface to only include methods that are actually used by `WndInstallingMod`:

```java
public interface IListener {
    void onMessage(String message);
    void onFileCopy(String path);
    void onFileSkip(String path);
    void onComplete();
    void onFileDelete(String entry);
}
```

### Reasoning
This is a better solution than adding unused method implementations to `WndInstallingMod` because:
1. It removes unused code
2. It follows the Interface Segregation Principle
3. It maintains backward compatibility for classes that actually use these methods
4. It simplifies the interface to only what's needed

### Implementation Steps
1. Remove `onFileSelected(String path)` method from the interface
2. Remove `onFileSelectionCancelled()` method from the interface

## Error 2-4: SystemText Constructor Signature Mismatches

### File to Modify
`/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/gfx/SystemText.java`

### Current Implementation
```java
public SystemText(String text, float size, boolean multiline) {
    super(text, 0, 0, 0);  // Error
    // ...
}

public SystemText(String text, float x, float y, int align) {
    super(text, x, y, align);  // Error
    // ...
}

public SystemText(String text, float x, float y, int maxWidth, int align) {
    super(text, x, y, maxWidth, align);  // Error
    // ...
}
```

### Fix
Correct the constructor calls to match the parent Text class constructor signature:

```java
public SystemText(String text, float size, boolean multiline) {
    super(0, 0, 0, 0);  // Correct parameter types
    // Set text content after calling parent constructor
    // ...
}

public SystemText(String text, float x, float y, int align) {
    super(x, y, 0, 0);  // Correct parameter types
    // Set text content after calling parent constructor
    // ...
}

public SystemText(String text, float x, float y, int maxWidth, int align) {
    super(x, y, maxWidth, 0);  // Correct parameter types
    // Set text content after calling parent constructor
    // ...
}
```

### Implementation Steps
1. Modify the first constructor to call `super(0, 0, 0, 0)` instead of `super(text, 0, 0, 0)`
2. Modify the second constructor to call `super(x, y, 0, 0)` instead of `super(text, x, y, align)`
3. Modify the third constructor to call `super(x, y, maxWidth, 0)` instead of `super(text, x, y, maxWidth, align)`
4. Ensure text content is properly set after calling the parent constructor

## Error 5: IIapCallback Functional Interface Incompatibility

### File to Modify
`/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeon/src/main/java/com/watabou/pixeldungeon/windows/WndHatInfo.java`

### Current Implementation
```java
RemixedDungeon.instance().iap.doPurchase(accessory, () -> {
    // Lambda implementation
});
```

### Fix
Replace the lambda expression with an anonymous class implementation:

```java
RemixedDungeon.instance().iap.doPurchase(accessory, new IIapCallback() {
    @Override
    public void onPurchaseOk() {
        item.ownIt(true);
        item.equip(false);
        onBackPressed();
        Window.hideParentWindow(this);
        if(!Game.isPaused()) {
            GameScene.show(new WndHats());
        }
    }
    
    @Override
    public void onPurchaseFail() {
        // Default empty implementation or appropriate error handling
    }
});
```

### Implementation Steps
1. Replace the lambda expression with an anonymous class implementation
2. Move the existing lambda body to the `onPurchaseOk()` method
3. Provide a default implementation for `onPurchaseFail()`

## Verification Steps

After implementing all fixes:
1. Run `./gradlew :RemixedDungeonHtml:compileJava` to verify compilation
2. Check that no compilation errors remain
3. Ensure that the fixes don't break existing functionality

## Risk Assessment

The proposed fixes are low risk because:
1. They only affect the HTML platform code
2. They maintain backward compatibility
3. They follow established patterns in the codebase
4. They don't change the core application logic

## Testing

After implementing the fixes, we should:
1. Verify that the HTML version compiles successfully
2. Test basic functionality of the HTML version if possible
3. Ensure that other platforms are not affected by the changes