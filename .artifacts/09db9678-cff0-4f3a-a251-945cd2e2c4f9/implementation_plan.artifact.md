# Implementation Plan - Remove Ghost/Neural Features

The goal is to remove all experimental "Ghost Lab" and "Neural" related features from the application. These features are primarily contained within the `labs.ghost` package and integrated into the core Seating Chart experience as optional analytical and visual layers.

## Proposed Changes

### 1. Deletions (Source & Tests)

#### [DELETE] `com.example.myapplication.labs.ghost` (package)
- Delete the entire package and all its contents (engines, layers, shaders, and activities).

#### [DELETE] Ghost-related test files
- Delete all unit tests in `src/test/java/com/example/myapplication/labs/ghost`.

---

### 2. Android Manifest

#### [MODIFY] [AndroidManifest.xml](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/AndroidManifest.xml)
- Remove the following Activities:
    - `GhostCortexActivity`
    - `GhostRayActivity`
    - `GhostVisionActivity`
    - `GhostStrategistActivity`
    - `GhostPreferencesActivity`
    - `GhostFilterActivity`
    - `GhostMorphActivity`
    - `GhostPaletteActivity`
    - `GhostPipActivity`
- Remove the following Services:
    - `GhostHudTileService`
    - `GhostQuickLogTileService`

---

### 3. Data Models & Logic

#### [MODIFY] [Student.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/Student.kt)
- Remove the `temporaryTask` property.

#### [MODIFY] [StudentRepository.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/StudentRepository.kt)
- Remove encryption/decryption logic for `temporaryTask`.

#### [MODIFY] [JsonImporter.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/importer/JsonImporter.kt) & [PythonDto.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/importer/PythonDto.kt)
- Remove `temporaryTask` from import mappings and DTOs.

---

### 4. Seating Chart UI & ViewModel

#### [MODIFY] [SeatingChartScreen.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/screens/SeatingChartScreen.kt)
- **Imports**: Remove all `com.example.myapplication.labs.ghost.*` imports.
- **State**: Remove nearly 100 `remember { mutableStateOf(...) }` variables and `collectAsState()` calls related to ghost layers.
- **Effects**: Remove `LaunchedEffect` blocks for Ghost engines (e.g., heartbeat, EKG, etc.).
- **Content**: Clean up the `Box` layout in `SeatingChartContent` to remove all ghost-themed background and overlay layers (Nebula, Aurora, Neural Map, etc.).
- **Menu**: Remove all Ghost/Neural options from the `SeatingChartTopAppBar` "More" menu and FAB menus.

#### [MODIFY] [SeatingChartViewModel.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/viewmodel/SeatingChartViewModel.kt)
- Remove all ghost engine instances and state flows.
- Remove the background update pipeline for ghost metrics.
- Remove ghost-related methods (e.g., `toggleStudentPin` if only used for Ghost, neural haptic triggers, etc.).

#### [MODIFY] [StudentDraggableIcon.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/components/StudentDraggableIcon.kt)
- Remove visual overlays for Iris, Helix, and other neural signatures.
- Remove the display of `temporaryTask`.

---

### 5. Documentation

#### [MODIFY] [README.md](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/README.md)
- Remove references to Ghost Lab and "advanced neural metrics".

#### [MODIFY] [DEVELOPER_INSIGHTS.md](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/DEVELOPER_INSIGHTS.md)
- Remove sections detailing Ghost engine physics and AGSL shader coordinate mapping.

## User Review Required

> [!CAUTION]
> This is a major destructive refactor. It will remove approximately 30-40% of the project's current line count. I will proceed with deleting the code only after your explicit approval.

## Open Questions
- Are there any specific "Ghost" features you consider *non-neural* that should be kept? (e.g., the Navigator/Mini-map or the basic Silhouette drag placeholders). Current plan assumes removing the entire `labs.ghost` package.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure no broken references remain.
- Run unit tests to verify core seating chart logic.

### Manual Verification
- Verify the Seating Chart loads and operates smoothly.
- Check the "More" menu to ensure it only contains standard non-experimental options.
- Ensure student icons are rendered cleanly without experimental overlays.
