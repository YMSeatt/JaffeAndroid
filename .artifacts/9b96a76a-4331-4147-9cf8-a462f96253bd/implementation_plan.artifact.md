# Fix Student Action Menu Positioning

The student action menu (context menu) currently appears at the far right of the screen instead of next to the student box or mouse click position. This is due to an incorrect conversion of pixel coordinates to DP in `SeatingChartScreen.kt`.

## Proposed Changes

### [UI Components]

#### [MODIFY] [SeatingChartScreen.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/screens/SeatingChartScreen.kt)
- Import `CustomDropdownMenu` from `com.example.myapplication.ui.components`.
- Replace both standard `DropdownMenu` instances (main student action menu and "Assign to Group" sub-menu) with `CustomDropdownMenu`.
- Use `longPressPosition` as the offset for both menus to ensure they appear at the interaction point.
- Pass the `noAnimations` preference (derived from `userPreferences`) to both `CustomDropdownMenu` instances.
- Calculate `noAnimations` in the main `SeatingChartScreen` body.

## Verification Plan

### Manual Verification
- Long-press on a student box.
- Verify that the menu appears exactly at the click position.
- Verify that the menu is correctly sized and readable (the screenshot shows it might be stretched if the offset is wrong).
- Check the "Assign to Group" sub-menu to ensure it is still accessible and correctly positioned (it might need a similar fix if it's currently appearing at top-left).

> [!NOTE]
> The `CustomDropdownMenu` uses `Popup` with `IntOffset`, which is perfect for placing a menu at specific pixel coordinates. The current implementation of `SeatingChartScreen` was passing pixel values disguised as DPs to a standard `DropdownMenu`, causing it to be scaled by the device density and pushed off-screen.
