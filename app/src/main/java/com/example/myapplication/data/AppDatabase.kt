package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Student::class, BehaviorEvent::class, HomeworkLog::class, Furniture::class, QuizLog::class, StudentGroup::class, LayoutTemplate::class, ConditionalFormattingRule::class, CustomBehavior::class, CustomHomeworkType::class, CustomHomeworkStatus::class, QuizTemplate::class, HomeworkTemplate::class, QuizMarkType::class], version = 10, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun behaviorEventDao(): BehaviorEventDao
    abstract fun homeworkLogDao(): HomeworkLogDao
    abstract fun furnitureDao(): FurnitureDao
    abstract fun quizLogDao(): QuizLogDao
    abstract fun studentGroupDao(): StudentGroupDao
    abstract fun layoutTemplateDao(): LayoutTemplateDao
    abstract fun conditionalFormattingRuleDao(): ConditionalFormattingRuleDao
    abstract fun customBehaviorDao(): CustomBehaviorDao
    abstract fun customHomeworkTypeDao(): CustomHomeworkTypeDao
    abstract fun customHomeworkStatusDao(): CustomHomeworkStatusDao
    abstract fun quizTemplateDao(): QuizTemplateDao
    abstract fun homeworkTemplateDao(): HomeworkTemplateDao
    abstract fun quizMarkTypeDao(): QuizMarkTypeDao

    companion object {
        const val DATABASE_NAME = "seating_chart_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) { /* ... existing migration ... */
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE students_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        xPosition REAL NOT NULL DEFAULT 0.0,
                        yPosition REAL NOT NULL DEFAULT 0.0
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO students_new (id, firstName, lastName)
                    SELECT id, firstName, lastName FROM students
                """.trimIndent())
                database.execSQL("DROP TABLE students")
                database.execSQL("ALTER TABLE students_new RENAME TO students")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) { /* ... existing migration ... */
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE homework_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        assignmentName TEXT NOT NULL,
                        status TEXT NOT NULL,
                        loggedAt INTEGER NOT NULL DEFAULT 0,
                        comment TEXT,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_homework_logs_studentId ON homework_logs(studentId)")
            }
        }

        // Migration from version 3 to 4: Adds custom display fields to Student table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE students ADD COLUMN customWidth INTEGER")
                database.execSQL("ALTER TABLE students ADD COLUMN customHeight INTEGER")
                database.execSQL("ALTER TABLE students ADD COLUMN customBackgroundColor TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN customOutlineColor TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN customTextColor TEXT")
            }
        }

        // Migration from version 4 to 5: Adds initials field to Student table
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE students ADD COLUMN initials TEXT")
            }
        }

        // Migration from version 5 to 6: Adds new fields to Student and creates Furniture table
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to students table
                database.execSQL("ALTER TABLE students ADD COLUMN stringId TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN nickname TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN gender TEXT NOT NULL DEFAULT 'Boy'")
                database.execSQL("ALTER TABLE students ADD COLUMN groupId TEXT") // Will be updated to Long in MIGRATION_6_7

                // Create new furniture table
                database.execSQL("""
                    CREATE TABLE furniture (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        stringId TEXT,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        xPosition REAL NOT NULL,
                        yPosition REAL NOT NULL,
                        width INTEGER NOT NULL,
                        height INTEGER NOT NULL,
                        fillColor TEXT,
                        outlineColor TEXT
                    )
                """.trimIndent())
            }
        }

        // Migration from version 6 to 7: Adds new tables and updates HomeworkLog and Student
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create QuizLog table
                database.execSQL("""
                    CREATE TABLE quiz_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        quizName TEXT NOT NULL,
                        markValue REAL,
                        markType TEXT,
                        maxMarkValue REAL,
                        loggedAt INTEGER NOT NULL,
                        comment TEXT,
                        marksData TEXT NOT NULL DEFAULT '{}',
                        numQuestions INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_logs_studentId ON quiz_logs(studentId)")

                // Create StudentGroup table
                database.execSQL("""
                    CREATE TABLE student_groups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create LayoutTemplate table
                database.execSQL("""
                    CREATE TABLE layout_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        layoutJson TEXT NOT NULL
                    )
                """.trimIndent())

                // Create ConditionalFormattingRule table
                database.execSQL("""
                    CREATE TABLE conditional_formatting_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        conditionJson TEXT NOT NULL,
                        formatJson TEXT NOT NULL,
                        targetType TEXT NOT NULL,
                        priority INTEGER NOT NULL
                    )
                """.trimIndent())

                // Update HomeworkLog table
                database.execSQL("ALTER TABLE homework_logs ADD COLUMN markValue REAL")
                database.execSQL("ALTER TABLE homework_logs ADD COLUMN markType TEXT")
                database.execSQL("ALTER TABLE homework_logs ADD COLUMN maxMarkValue REAL")

                // Recreate Students table to change groupId type (SQLite doesn't directly support ALTER COLUMN TYPE)
                database.execSQL("""
                    CREATE TABLE students_temp_for_groupid_update (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        stringId TEXT,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        nickname TEXT,
                        initials TEXT,
                        gender TEXT NOT NULL DEFAULT 'Boy',
                        xPosition REAL NOT NULL DEFAULT 0.0,
                        yPosition REAL NOT NULL DEFAULT 0.0,
                        customWidth INTEGER,
                        customHeight INTEGER,
                        customBackgroundColor TEXT,
                        customOutlineColor TEXT,
                        customTextColor TEXT,
                        groupId INTEGER,  -- Changed to INTEGER for Long
                        FOREIGN KEY(groupId) REFERENCES student_groups(id) ON DELETE SET NULL
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO students_temp_for_groupid_update (id, stringId, firstName, lastName, nickname, initials, gender, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor, groupId)
                    SELECT id, stringId, firstName, lastName, nickname, initials, gender, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor, CASE WHEN groupId GLOB '[0-9]*' THEN CAST(groupId AS INTEGER) ELSE NULL END FROM students
                """.trimIndent())
                database.execSQL("DROP TABLE students")
                database.execSQL("ALTER TABLE students_temp_for_groupid_update RENAME TO students")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_students_groupId ON students(groupId)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create a new table with the correct id type (INTEGER for Long)
                database.execSQL("""
                    CREATE TABLE students_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        stringId TEXT,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        nickname TEXT,
                        gender TEXT NOT NULL DEFAULT 'Boy',
                        groupId INTEGER,
                        initials TEXT,
                        xPosition REAL NOT NULL DEFAULT 0.0,
                        yPosition REAL NOT NULL DEFAULT 0.0,
                        customWidth INTEGER,
                        customHeight INTEGER,
                        customBackgroundColor TEXT,
                        customOutlineColor TEXT,
                        customTextColor TEXT
                    )
                """.trimIndent())

                // Copy data from the old table to the new table
                database.execSQL("""
                    INSERT INTO students_new (id, stringId, firstName, lastName, nickname, gender, groupId, initials, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor)
                    SELECT id, stringId, firstName, lastName, nickname, gender, groupId, initials, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor
                    FROM students
                """.trimIndent())

                // Drop the old table
                database.execSQL("DROP TABLE students")

                // Rename the new table to the original name
                database.execSQL("ALTER TABLE students_new RENAME TO students")

                // Recreate indexes and foreign keys if necessary (Room handles this for entities)
                // For foreign keys, Room usually recreates them on table recreation.
                // For indexes, if any were explicitly defined on the old table, they might need to be recreated.
                // In this case, the only index on students is for groupId, which is handled by the new table definition.

                // Update foreign key constraints for tables referencing students.id
                // HomeworkLog
                database.execSQL("""
                    CREATE TABLE homework_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        assignmentName TEXT NOT NULL,
                        status TEXT NOT NULL,
                        loggedAt INTEGER NOT NULL DEFAULT 0,
                        comment TEXT,
                        markValue REAL,
                        markType TEXT,
                        maxMarkValue REAL,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO homework_logs_new (id, studentId, assignmentName, status, loggedAt, comment, markValue, markType, maxMarkValue)
                    SELECT id, studentId, assignmentName, status, loggedAt, comment, markValue, markType, maxMarkValue
                    FROM homework_logs
                """.trimIndent())
                database.execSQL("DROP TABLE homework_logs")
                database.execSQL("ALTER TABLE homework_logs_new RENAME TO homework_logs")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_homework_logs_studentId ON homework_logs(studentId)")

                // QuizLog
                database.execSQL("""
                    CREATE TABLE quiz_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        quizName TEXT NOT NULL,
                        markValue REAL,
                        markType TEXT,
                        maxMarkValue REAL,
                        loggedAt INTEGER NOT NULL,
                        comment TEXT,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO quiz_logs_new (id, studentId, quizName, markValue, markType, maxMarkValue, loggedAt, comment)
                    SELECT id, studentId, quizName, markValue, markType, maxMarkValue, loggedAt, comment
                    FROM quiz_logs
                """.trimIndent())
                database.execSQL("DROP TABLE quiz_logs")
                database.execSQL("ALTER TABLE quiz_logs_new RENAME TO quiz_logs")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_logs_studentId ON quiz_logs(studentId)")

                // BehaviorEvent
                database.execSQL("""
                    CREATE TABLE behavior_events_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        comment TEXT,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("""
                    INSERT INTO behavior_events_new (id, studentId, type, timestamp, comment)
                    SELECT id, studentId, type, timestamp, comment
                    FROM behavior_events
                """.trimIndent())
                database.execSQL("DROP TABLE behavior_events")
                database.execSQL("ALTER TABLE behavior_events_new RENAME TO behavior_events")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_behavior_events_studentId ON behavior_events(studentId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `custom_behaviors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `custom_homework_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `custom_homework_statuses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `quiz_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `marksData` TEXT NOT NULL)")
                database.execSQL("CREATE TABLE IF NOT EXISTS `homework_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `marksData` TEXT NOT NULL)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `quiz_mark_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `defaultPoints` REAL NOT NULL, `contributesToTotal` INTEGER NOT NULL, `isExtraCredit` INTEGER NOT NULL)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10) // Added MIGRATION_9_10
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}