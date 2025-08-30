# Strategy for Fixing HTML Compilation Errors

This document outlines a systematic approach to fix the HTML compilation errors in Remixed Dungeon.

## Phase 1: Fix Abstract Method Implementations

### 1. WndInstallingMod.java
- Implement the missing `onFileSelectionCancelled()` method in the HTML version
- This method should be added to the `AndroidSAF.IListener` interface implementation

## Phase 2: Fix Constructor Signature Issues

### 1. SystemText.java
- Fix constructor parameter mismatches:
  - `super(text, 0, 0, 0)` should be `super(0, 0, 0, 0)` 
  - `super(text, x, y, align)` should be `super(x, y, 0, 0)` 
  - `super(text, x, y, maxWidth, align)` should be `super(x, y, maxWidth, 0)`

## Phase 3: Fix Functional Interface Issues

### 1. IIapCallback.java
- Review the interface design to ensure it's compatible with lambda expressions
- Either reduce to a single abstract method or provide proper method implementations

## Implementation Order

1. Start with abstract method implementations (Phase 1)
2. Fix constructor signatures (Phase 2)
3. Fix functional interface issues (Phase 3)

## Testing Approach

1. After each phase, run the compilation to verify fixes
2. Focus on one error category at a time
3. Use the desktop implementation as a reference for method signatures and behavior
4. Ensure HTML-specific implementations respect browser limitations

## Expected Challenges

1. Some Android-specific functionality may not have direct equivalents in HTML
2. Graphics rendering differences between Android and HTML backends
3. File system limitations in the browser environment
4. Input event handling differences between platforms