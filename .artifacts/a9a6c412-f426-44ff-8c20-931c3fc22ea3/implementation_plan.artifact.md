# Implementation Plan - Fix Database Migration Error

The application is crashing with `java.lang.IllegalStateException: Migration didn't properly handle: students`. This is caused by a discrepancy between the `students` table in the database and the `Student` entity class.

## Discrepancies Identified
1.  **`temporaryTask` Column**: Exists in the database (added in version 22) but was removed from the `Student` entity in a recent refactor.
2.  **`isPinned` Default Value**: Migration 33-34 added this column with `DEFAULT 0`. However, the `Student` entity does not specify this default value, so Room expects `undefined`.

## Proposed Changes

### 1. Student Entity

#### [MODIFY] [Student.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/Student.kt)
- Add `@ColumnInfo(defaultValue = "0")` to the `isPinned: Boolean` field.
- This will align the Room-generated schema with the actual database state for the `isPinned` column.

### 2. Database Migrations

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/yaako/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/data/AppDatabase.kt)
- Increment `@Database` version to `36`.
- Implement `MIGRATION_35_36` to remove the `temporaryTask` column from the `students` table.
- **Migration Logic**:
    1.  Create `students_new` table without `temporaryTask`.
    2.  Copy data from `students` to `students_new`.
    3.  Drop `students` table.
    4.  Rename `students_new` to `students`.
    5.  Recreate the index on `groupId`.

> [!IMPORTANT]
> Recreating the `students` table requires careful handling of foreign keys. `PRAGMA foreign_keys = OFF` should be used during the migration if done manually, but since this is within a `Migration` object in Room, it's generally safer to recreate the table and rely on Room's handling of the schema if possible. However, manual SQL is required here.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to ensure compilation.

### Manual Verification
- Deploy to a device and confirm the `IllegalStateException` is resolved.
- Verify student data is intact.
- Verify that the "Pinned" status of students is preserved.
