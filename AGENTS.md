# Agent Instructions

This document outlines the plan for porting features from the Python Tkinter application to the Android application. The goal is to achieve feature parity, focusing on creating a powerful and customizable classroom management tool for tablets.

## Feature Implementation Priority

The following is a prioritized list of features to be implemented. Work should proceed in this order, as each step builds upon the previous one.

1.  **Core Data Model Expansion:** The foundation for all other features.
    *   Expand the Room database with entities for `QuizLog`, `StudentGroup`, `LayoutTemplate`, and `ConditionalFormattingRule`.
    *   Update the `Student` entity to support group associations.
    *   Enhance the `HomeworkLog` entity to support detailed, mark-based logging.
    *   Implement the corresponding DAOs and Repository methods.

2.  **Advanced Logging UI:** Implement the core user interactions for data entry.
    *   Create a "Log Quiz Score" dialog with support for different mark types.
    *   Create an "Advanced Homework Log" dialog.
    *   Integrate these into the main UI, likely via the student context menu.

3.  **Student Groups Management:** A key organizational feature.
    *   Create a "Manage Student Groups" screen (add, edit with color, delete).
    *   Update the "Edit Student" dialog to allow group assignment.

4.  **Comprehensive Settings Screen:** Expose customization options to the user.
    *   Create UI to manage custom lists (Behavior types, Homework types, Quiz mark types).
    *   Add switches and preferences for all new features as they are developed.

5.  **Conditional Formatting:** A major visual feedback feature.
    *   Create a "Manage Rules" screen.
    *   Implement the logic in the `SeatingChartViewModel` to apply these rules to student items.
    *   The `StudentUiItem` will be updated to reflect the dynamic styling.

6.  **Live Sessions (Quiz & Homework):** A powerful interactive tool.
    *   Add UI controls to start/end live sessions.
    *   Implement the logic for real-time marking and visual feedback on the canvas.
    *   Ensure session data is correctly logged upon completion.

7.  **Layout and Data Management:**
    *   Implement saving and loading of classroom layouts (Layout Templates).
    *   Greatly enhance the export feature to include all logs with date/student/type filters.
    *   Implement student import from Excel.
    *   Implement full data backup and restore.

8.  **Security Features:**
    *   Implement password protection for the app.
    *   (Optional Stretch Goal) Implement database encryption.

9.  **Undo/Redo System:** A major quality-of-life improvement.
    *   Refactor operations into a command pattern.
    *   Manage undo/redo stacks.
