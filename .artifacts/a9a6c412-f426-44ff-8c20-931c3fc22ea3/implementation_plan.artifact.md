# Implementation Plan: Fix Email Schedule Scrolling and Behavior Marking Bug

This plan addresses two issues:
1.  **Email Schedule Add Screen Scrolling**: The current `ScheduleEditorDialog` has many fields but is not scrollable, making it unusable on smaller screens or in landscape mode.
2.  **Behavior Marking Bug**: When logging a behavior, encrypted comment strings (like base64 tokens) appear on student cards because the `SeatingChartViewModel` bypasses the repository's decryption layer.

## Proposed Changes

### [Component] Email Schedules

#### [MODIFY] [ScheduleEditorDialog.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/settings/ScheduleEditorDialog.kt)
- Wrap the form fields in a scrollable `Column`.
- Keep the "Save" and "Cancel" buttons at the bottom of the dialog, outside the scrollable area.
- Ensure the dialog has a reasonable maximum height.

### [Component] Seating Chart Data Pipeline

#### [MODIFY] [StudentRepository.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/StudentRepository.kt)
- Add exposed `LiveData` streams for all behavior events, homework logs, and quiz logs that automatically decrypt the results using `SecurityUtil`.
- Ensure all repository-provided `LiveData` and `Flow` streams are properly decrypted.

#### [MODIFY] [SeatingChartViewModel.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/viewmodel/SeatingChartViewModel.kt)
- Update the initialization of `allBehaviorEvents`, `allHomeworkLogs`, and `allQuizLogs` to use the newly created streams from `StudentRepository` instead of direct DAO calls.
- This ensures that the `updateStudentsForDisplay` logic works with decrypted data, preventing encrypted strings from appearing in the UI.

## Verification Plan

### Automated Tests
- No new automated tests are planned for this UI fix.
- Existing tests for `SecurityUtil` and `StudentRepository` should pass.

### Manual Verification
- **Email Schedule**: Open the Email Schedules screen, click to add a new schedule, and verify that the dialog content is scrollable while buttons stay visible at the bottom.
- **Behavior Marking**: Log a behavior for a student on the seating chart and verify that the student card displays the behavior type (and comment, if any) in plain text, not an encrypted token.
