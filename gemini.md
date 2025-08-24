## 10. Student Box Style Overrides UI

### Python Implementation:
The Python app allows users to customize the visual style of individual student boxes, including:
*   Custom width and height.
*   Custom fill color and outline color.
*   Custom font family, size, and color for the student's name.
These overrides are persistent and apply only to the specific student.

### Android Implementation Needed:
The Android app has a `StudentStyleScreen`, which suggests some styling capabilities. To fully mirror the Python app, ensure the following:

*   **UI for Style Overrides:**
    *   The `StudentStyleScreen` should provide controls for:
        *   **Dimensions:** Input fields for width and height.
        *   **Colors:** Color pickers for background fill color and border outline color.
        *   **Font:** Dropdowns for font family, number input for font size, and a color picker for font color.
    *   Ensure these settings are applied to the specific `Student` object and persisted in the database.
*   **Data Model:** The `Student` entity in the Room database needs to have fields or a mechanism to store these style override properties.
*   **Rendering Logic:** The `StudentDraggableIcon` composable needs to read these style override properties from the `StudentUiItem` and apply them dynamically to the student box's appearance.

## 11. Ruler and Guide Management UI

### Python Implementation:
The Python app provides visual rulers along the top and left edges of the canvas, and allows users to add draggable vertical and horizontal guides. These guides can be used for precise placement and alignment of students and furniture.

### Android Implementation Needed:
The Android app has `GridAndRulers` composable, indicating basic ruler functionality. To fully mirror the Python app:

*   **UI for Adding Guides:**
    *   Add buttons in the `TopAppBar` or a dedicated "View" menu for "Add Vertical Guide" and "Add Horizontal Guide."
    *   When a guide is added, it should appear on the canvas and be draggable.
*   **Guide Data Model:** Create a Room database entity (e.g., `Guide`) to store guide properties (ID, type, position).
*   **Rendering Guides:** The `GridAndRulers` composable (or a new `Guides` composable) needs to fetch active guides from a ViewModel and draw them on the canvas.
*   **Guide Interaction:** Implement drag gestures for guides, allowing users to reposition them. Consider adding options to delete guides.
*   **Persistence:** Ensure guides are saved to and loaded from the database.

## 12. "Open Data Folder" / "Open Last Export Folder" Functionality

### Python Implementation:
The Python app provides convenient menu options to:
*   Open the application's data folder in the file explorer.
*   Open the folder where the last Excel export was saved.

### Android Implementation Needed:
These are utility features that enhance user experience.

*   **Open Data Folder:**
    *   **File Access:** Android apps have internal storage and external storage. Directly opening internal app directories in a generic file explorer is often restricted by Android's security model. A more user-friendly approach might be to provide a way to share/export the data files to a user-accessible location, or provide instructions.
*   **Open Last Export Folder:**
    *   **Store Last Path:** When an export is successfully saved, store the `Uri` or path of the exported file in `SharedPreferences` or `SettingsViewModel`.
    *   **Intent for File Explorer:** Use an `Intent` with `ACTION_VIEW` and the `Uri` of the last exported file's directory. This is generally more feasible for files saved via `ActivityResultContracts.CreateDocument`.
*   **UI Integration:** Add menu items in `MainActivity`'s "File" menu for these actions.

## 13. Python-specific Integrations (General Considerations)

### Python Implementation:
The Python app leverages various Python libraries for specific functionalities:
*   `openpyxl`: For Excel file manipulation.
*   `Pillow`: For image processing.
*   `sv_ttk`, `darkdetect`: For UI theming.
*   `win32gui`, `win32ui`, `win32con`: For Windows-specific screenshot capabilities.
*   `cryptography.fernet`: For data encryption/decryption.

### Android Implementation Needed:
While the Android app uses its own native equivalents (Compose UI, AndroidX libraries, Room, etc.), it's crucial to ensure that all functionalities relying on these Python libraries are fully replicated and optimized for the Android environment.

*   **Excel Manipulation (`openpyxl`):** For advanced Excel features (like macro-enabled files or complex formatting), a dedicated Android Excel library (e.g., Apache POI for Android) would be necessary.
*   **Image Processing (`Pillow`):** Android's `Bitmap` and `Canvas` APIs are the primary tools. For advanced image manipulation or specific formats not natively supported, consider libraries like Glide, Picasso, or Coil for image loading/display, and potentially a dedicated image processing library.
*   **UI Theming (`sv_ttk`, `darkdetect`):** The Android app already uses Compose's theming capabilities and `isSystemInDarkTheme()`, which is the correct approach. Ensure all theme-related settings from Python are fully configurable and applied in Android.
*   **Platform-Specific Features (`win32gui`):** Windows-specific features like direct window screenshots would not be directly transferable. The Android equivalent would be capturing the Compose UI as a `Bitmap`.
*   **Data Encryption (`cryptography.fernet`):** The Android app needs to implement a secure encryption mechanism for sensitive data, potentially using the Android Keystore System and `javax.crypto.Cipher` APIs.
*   **File Locking (`FileLockManager`):** Android apps typically run in isolated sandboxes, so explicit file locking for multi-instance prevention within a single device is less common, but if the app supports multiple processes or shared data, a robust locking mechanism might be needed.