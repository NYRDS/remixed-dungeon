# HTML Compilation Fix Plan

This document outlines the specific fixes needed to resolve the HTML compilation errors in Remixed Dungeon.

## Overview

There are 5 compilation errors that need to be fixed:

1. Interface mismatch in `WndInstallingMod` due to unused methods in `AndroidSAF.IListener`
2. Three constructor signature mismatches in `SystemText`

## Fix 1: Simplify AndroidSAF.IListener Interface

### File to Modify
`/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/AndroidSAF.java`

### Issue
The HTML version of `AndroidSAF.IListener` interface includes methods that are not used by `WndInstallingMod`.

### Analysis
The HTML version of `AndroidSAF.IListener` interface (in `/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/storage/AndroidSAF.java`) currently requires the following methods:
- `void onFileSelected(String path);`
- `void onFileSelectionCancelled();`
- `void onMessage(String message);`
- `void onFileCopy(String path);`
- `void onFileSkip(String path);`
- `void onComplete();`
- `void onFileDelete(String entry);`

However, `WndInstallingMod` only implements:
- `void onMessage(String message);`
- `void onFileCopy(String path);`
- `void onFileSkip(String path);`
- `void onComplete();`
- `void onFileDelete(String entry);`

The `onFileSelected` and `onFileSelectionCancelled` methods are never called by `WndInstallingMod`.

### Solution
Simplify the HTML `AndroidSAF.IListener` interface by removing the unused methods:

```java
public interface IListener {
    void onMessage(String message);
    void onFileCopy(String path);
    void onFileSkip(String path);
    void onComplete();
    void onFileDelete(String entry);
}
```

This approach is better because:
1. It removes unused code
2. It eliminates the compilation error without adding unnecessary methods
3. It keeps the interface focused on what's actually needed

## Fix 2-4: SystemText Constructor Signatures

### File to Modify
`/home/mike/StudioProjects/remixed-dungeon_fix/RemixedDungeonHtml/src/html/java/com/nyrds/platform/gfx/SystemText.java`

### Issues
The three constructors that take a String as the first parameter are calling the parent constructor with incorrect signatures:

1. `SystemText(String text, float size, boolean multiline)`
2. `SystemText(String text, float x, float y, int align)`
3. `SystemText(String text, float x, float y, int maxWidth, int align)`

### Root Cause
The parent `Text` class constructor expects `(float x, float y, float width, float height)`, but the HTML `SystemText` constructors are trying to pass the text as the first parameter.

### Solutions

#### Constructor 1: `SystemText(String text, float size, boolean multiline)`
**Current:**
```java
public SystemText(String text, float size, boolean multiline) {
    super(text, 0, 0, 0);
    // ...
}
```

**Fixed:**
```java
public SystemText(String text, float size, boolean multiline) {
    super(0, 0, 0, 0);
    this.text(text);
    // ...
}
```

#### Constructor 2: `SystemText(String text, float x, float y, int align)`
**Current:**
```java
public SystemText(String text, float x, float y, int align) {
    super(text, x, y, align);
    // ...
}
```

**Fixed:**
```java
public SystemText(String text, float x, float y, int align) {
    super(x, y, 0, 0);
    this.text(text);
    // ...
}
```

#### Constructor 3: `SystemText(String text, float x, float y, int maxWidth, int align)`
**Current:**
```java
public SystemText(String text, float x, float y, int maxWidth, int align) {
    super(text, x, y, maxWidth, align);
    // ...
}
```

**Fixed:**
```java
public SystemText(String text, float x, float y, int maxWidth, int align) {
    super(x, y, maxWidth, 0);
    this.text(text);
    // ...
}
```

## Implementation Notes

1. All fixes should be made in the exact locations specified above
2. The text should be set using the `text(String str)` method after calling the parent constructor
3. The existing functionality should be preserved
4. After making these changes, the HTML version should compile successfully

## Testing

After implementing these fixes:
1. Run `./gradlew :RemixedDungeonHtml:compileJava` to verify compilation
2. If successful, proceed with building the full HTML version