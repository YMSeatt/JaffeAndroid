package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Student::class, BehaviorEvent::class, HomeworkLog::class, Furniture::class, QuizLog::class, StudentGroup::class, LayoutTemplate::class, ConditionalFormattingRule::class, CustomBehavior::class, CustomHomeworkType::class, CustomHomeworkStatus::class, QuizTemplate::class, HomeworkTemplate::class, QuizMarkType::class, Guide::class, SystemBehavior::class, Reminder::class, EmailSchedule::class], version = 24, exportSchema = false)
@TypeConverters(Converters::class)
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
    abstract fun guideDao(): GuideDao
    abstract fun systemBehaviorDao(): SystemBehaviorDao
    abstract fun reminderDao(): ReminderDao
    abstract fun emailScheduleDao(): EmailScheduleDao

    companion object {
        const val DATABASE_NAME = "seating_chart_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) { /* ... existing migration ... */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE students_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        firstName TEXT NOT NULL,
                        lastName TEXT NOT NULL,
                        xPosition REAL NOT NULL DEFAULT 0.0,
                        yPosition REAL NOT NULL DEFAULT 0.0
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO students_new (id, firstName, lastName)
                    SELECT id, firstName, lastName FROM students
                """.trimIndent())
                db.execSQL("DROP TABLE students")
                db.execSQL("ALTER TABLE students_new RENAME TO students")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) { /* ... existing migration ... */
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
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
                db.execSQL("CREATE INDEX IF NOT EXISTS index_homework_logs_studentId ON homework_logs(studentId)")
            }
        }

        // Migration from version 3 to 4: Adds custom display fields to Student table
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN customWidth INTEGER")
                db.execSQL("ALTER TABLE students ADD COLUMN customHeight INTEGER")
                db.execSQL("ALTER TABLE students ADD COLUMN customBackgroundColor TEXT")
                db.execSQL("ALTER TABLE students ADD COLUMN customOutlineColor TEXT")
                db.execSQL("ALTER TABLE students ADD COLUMN customTextColor TEXT")
            }
        }

        // Migration from version 4 to 5: Adds initials field to Student table
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN initials TEXT")
            }
        }

        // Migration from version 5 to 6: Adds new fields to Student and creates Furniture table
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to students table
                db.execSQL("ALTER TABLE students ADD COLUMN stringId TEXT")
                db.execSQL("ALTER TABLE students ADD COLUMN nickname TEXT")
                db.execSQL("ALTER TABLE students ADD COLUMN gender TEXT NOT NULL DEFAULT 'Boy'")
                db.execSQL("ALTER TABLE students ADD COLUMN groupId TEXT") // Will be updated to Long in MIGRATION_6_7

                // Create new furniture table
                db.execSQL("""
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
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create QuizLog table
                db.execSQL("""
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
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_logs_studentId ON quiz_logs(studentId)")

                // Create StudentGroup table
                db.execSQL("""
                    CREATE TABLE student_groups (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER NOT NULL
                    )
                """.trimIndent())

                // Create LayoutTemplate table
                db.execSQL("""
                    CREATE TABLE layout_templates (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        layoutJson TEXT NOT NULL
                    )
                """.trimIndent())

                // Create ConditionalFormattingRule table
                db.execSQL("""
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
                db.execSQL("ALTER TABLE homework_logs ADD COLUMN markValue REAL")
                db.execSQL("ALTER TABLE homework_logs ADD COLUMN markType TEXT")
                db.execSQL("ALTER TABLE homework_logs ADD COLUMN maxMarkValue REAL")

                // Recreate Students table to change groupId type (SQLite doesn't directly support ALTER COLUMN TYPE)
                db.execSQL("""
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
                db.execSQL("""
                    INSERT INTO students_temp_for_groupid_update (id, stringId, firstName, lastName, nickname, initials, gender, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor, groupId)
                    SELECT id, stringId, firstName, lastName, nickname, initials, gender, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor, CASE WHEN groupId GLOB '[0-9]*' THEN CAST(groupId AS INTEGER) ELSE NULL END FROM students
                """.trimIndent())
                db.execSQL("DROP TABLE students")
                db.execSQL("ALTER TABLE students_temp_for_groupid_update RENAME TO students")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_students_groupId ON students(groupId)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new table with the correct id type (INTEGER for Long)
                db.execSQL("""
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
                db.execSQL("""
                    INSERT INTO students_new (id, stringId, firstName, lastName, nickname, gender, groupId, initials, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor)
                    SELECT id, stringId, firstName, lastName, nickname, gender, groupId, initials, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor
                    FROM students
                """.trimIndent())

                // Drop the old table
                db.execSQL("DROP TABLE students")

                // Rename the new table to the original name
                db.execSQL("ALTER TABLE students_new RENAME TO students")

                // Recreate indexes and foreign keys if necessary (Room handles this for entities)
                // For foreign keys, Room usually recreates them on table recreation.
                // For indexes, if any were explicitly defined on the old table, they might need to be recreated.
                // In this case, the only index on students is for groupId, which is handled by the new table definition.

                // Update foreign key constraints for tables referencing students.id
                // HomeworkLog
                db.execSQL("""
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
                db.execSQL("""
                    INSERT INTO homework_logs_new (id, studentId, assignmentName, status, loggedAt, comment, markValue, markType, maxMarkValue)
                    SELECT id, studentId, assignmentName, status, loggedAt, comment, markValue, markType, maxMarkValue
                    FROM homework_logs
                """.trimIndent())
                db.execSQL("DROP TABLE homework_logs")
                db.execSQL("ALTER TABLE homework_logs_new RENAME TO homework_logs")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_homework_logs_studentId ON homework_logs(studentId)")

                // QuizLog
                db.execSQL("""
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
                db.execSQL("""
                    INSERT INTO quiz_logs_new (id, studentId, quizName, markValue, markType, maxMarkValue, loggedAt, comment)
                    SELECT id, studentId, quizName, markValue, markType, maxMarkValue, loggedAt, comment
                    FROM quiz_logs
                """.trimIndent())
                db.execSQL("DROP TABLE quiz_logs")
                db.execSQL("ALTER TABLE quiz_logs_new RENAME TO quiz_logs")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_logs_studentId ON quiz_logs(studentId)")

                // BehaviorEvent
                db.execSQL("""
                    CREATE TABLE behavior_events_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        comment TEXT,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO behavior_events_new (id, studentId, type, timestamp, comment)
                    SELECT id, studentId, type, timestamp, comment
                    FROM behavior_events
                """.trimIndent())
                db.execSQL("DROP TABLE behavior_events")
                db.execSQL("ALTER TABLE behavior_events_new RENAME TO behavior_events")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_behavior_events_studentId ON behavior_events(studentId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `custom_behaviors` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `custom_homework_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `custom_homework_statuses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `quiz_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `marksData` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `homework_templates` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `marksData` TEXT NOT NULL)") //Fixed: Removed an extra ')' that was causing a syntax error.
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) { // Renamed 'database' to 'db'
                db.execSQL("CREATE TABLE IF NOT EXISTS `quiz_mark_types` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `defaultPoints` REAL NOT NULL, `contributesToTotal` INTEGER NOT NULL, `isExtraCredit` INTEGER NOT NULL)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new table with the desired schema
                db.execSQL("""
                    CREATE TABLE homework_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        assignmentName TEXT NOT NULL,
                        status TEXT NOT NULL,
                        loggedAt INTEGER NOT NULL,
                        comment TEXT,
                        marksData TEXT,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Copy data from the old table to the new one, transforming the marks columns into a single JSON string.
                db.execSQL("""
                    INSERT INTO homework_logs_new (id, studentId, assignmentName, status, loggedAt, comment, marksData)
                    SELECT 
                        id, 
                        studentId, 
                        assignmentName, 
                        status, 
                        loggedAt, 
                        comment, 
                        CASE 
                            WHEN markValue IS NOT NULL THEN 
                                '{"markValue":' || markValue || ',"markType":"' || COALESCE(markType, '') || '","maxMarkValue":' || COALESCE(maxMarkValue, 'null') || '}'
                            ELSE NULL 
                        END 
                    FROM homework_logs
                """.trimIndent())

                // Drop the old table
                db.execSQL("DROP TABLE homework_logs")

                // Rename the new table to the original name
                db.execSQL("ALTER TABLE homework_logs_new RENAME TO homework_logs")

                // Recreate the index
                db.execSQL("CREATE INDEX IF NOT EXISTS index_homework_logs_studentId ON homework_logs(studentId)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create a new table with the desired schema
                db.execSQL("""
                    CREATE TABLE quiz_logs_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        studentId INTEGER NOT NULL,
                        quizName TEXT NOT NULL,
                        loggedAt INTEGER NOT NULL,
                        comment TEXT,
                        marksData TEXT NOT NULL,
                        numQuestions INTEGER NOT NULL,
                        FOREIGN KEY(studentId) REFERENCES students(id) ON DELETE CASCADE
                    )
                """.trimIndent())

                // Copy data from the old table to the new one, transforming the marks columns into a single JSON string.
                db.execSQL("""
                    INSERT INTO quiz_logs_new (id, studentId, quizName, loggedAt, comment, marksData, numQuestions)
                    SELECT 
                        id, 
                        studentId, 
                        quizName, 
                        loggedAt, 
                        comment, 
                        CASE 
                            WHEN markValue IS NOT NULL THEN 
                                '{"markValue":' || markValue || ',"markType":"' || COALESCE(markType, '') || '","maxMarkValue":' || COALESCE(maxMarkValue, 'null') || '}'
                            ELSE '{}' 
                        END,
                        0
                    FROM quiz_logs
                """.trimIndent())

                // Drop the old table
                db.execSQL("DROP TABLE quiz_logs")

                // Rename the new table to the original name
                db.execSQL("ALTER TABLE quiz_logs_new RENAME TO quiz_logs")

                // Recreate the index
                db.execSQL("CREATE INDEX IF NOT EXISTS index_quiz_logs_studentId ON quiz_logs(studentId)")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty migration to fix a bug with the InvalidationTracker.
            }
        }
        
        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty migration.
            }
        }

        val MIGRATION_14_15 = object : Migration(14, 15) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN customOutlineThickness INTEGER")
            }
        }

        val MIGRATION_15_16 = object : Migration(15, 16) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN customFontFamily TEXT")
                db.execSQL("ALTER TABLE students ADD COLUMN customFontSize INTEGER")
                db.execSQL("ALTER TABLE students ADD COLUMN customFontColor TEXT")
            }
        }

        val MIGRATION_16_17 = object : Migration(16, 17) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `guides` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `type` TEXT NOT NULL,
                        `position` REAL NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_17_18 = object : Migration(17, 18) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Empty migration.
            }
        }

        val MIGRATION_18_19 = object : Migration(18, 19) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE students ADD COLUMN customCornerRadius INTEGER")
                db.execSQL("ALTER TABLE students ADD COLUMN customPadding INTEGER")
            }
        }
        
        val MIGRATION_19_20 = object : Migration(19, 20) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE conditional_formatting_rules ADD COLUMN type TEXT NOT NULL DEFAULT 'group'")
            }
        }

        val MIGRATION_20_21 = object : Migration(20, 21) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE quiz_templates ADD COLUMN numQuestions INTEGER")
            }
        }

        val MIGRATION_21_22 = object : Migration(21, 22) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `system_behaviors` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL
                    )
                """.trimIndent())
                db.execSQL("ALTER TABLE students ADD COLUMN temporaryTask TEXT")
            }
        }

        val MIGRATION_22_23 = object : Migration(22, 23) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `reminders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `time` INTEGER NOT NULL,
                        `date` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_23_24 = object : Migration(23, 24) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `email_schedules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `hour` INTEGER NOT NULL,
                        `minute` INTEGER NOT NULL,
                        `daysOfWeek` INTEGER NOT NULL,
                        `recipientEmail` TEXT NOT NULL,
                        `subject` TEXT NOT NULL,
                        `body` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
            }
        }


        fun getDatabase(context: Context, dbName: String = DATABASE_NAME): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val builder = if (dbName == DATABASE_NAME) {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        DATABASE_NAME
                    )
                } else {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    ).createFromFile(context.getDatabasePath(dbName))
                }

                val instance = builder.addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13, MIGRATION_13_14, MIGRATION_14_15, MIGRATION_15_16, MIGRATION_16_17, MIGRATION_17_18, MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun switchToArchive(context: Context, archiveName: String) {
            INSTANCE?.close()
            INSTANCE = null
            INSTANCE = getDatabase(context, archiveName)
        }

        fun switchToLive(context: Context) {
            INSTANCE?.close()
            INSTANCE = getDatabase(context, DATABASE_NAME)
        }
    }
}
