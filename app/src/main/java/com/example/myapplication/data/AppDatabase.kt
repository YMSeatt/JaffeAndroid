package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Student::class,
        BehaviorEvent::class,
        HomeworkLog::class,
        Furniture::class,
        QuizLog::class,
        StudentGroup::class,
        LayoutTemplate::class,
        ConditionalFormattingRule::class
    ],
    version = 7,
    exportSchema = false
)
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


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2: Migration = object : Migration(1, 2) { /* ... existing migration ... */
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
        val MIGRATION_2_3: Migration = object : Migration(2, 3) { /* ... existing migration ... */
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
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE students ADD COLUMN customWidth INTEGER")
                database.execSQL("ALTER TABLE students ADD COLUMN customHeight INTEGER")
                database.execSQL("ALTER TABLE students ADD COLUMN customBackgroundColor TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN customOutlineColor TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN customTextColor TEXT")
            }
        }

        // Migration from version 4 to 5: Adds initials field to Student table
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE students ADD COLUMN initials TEXT")
            }
        }

        // Migration from version 5 to 6: Adds new fields to Student and creates Furniture table
        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns to students table
                database.execSQL("ALTER TABLE students ADD COLUMN stringId TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN nickname TEXT")
                database.execSQL("ALTER TABLE students ADD COLUMN gender TEXT NOT NULL DEFAULT 'Boy'")
                database.execSQL("ALTER TABLE students ADD COLUMN groupId TEXT")

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

        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 1. Create new tables
                database.execSQL("""
                    CREATE TABLE `student_groups` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE `quiz_logs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `studentId` INTEGER NOT NULL,
                        `quizName` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `marksData` TEXT,
                        `numQuestions` INTEGER NOT NULL,
                        `comment` TEXT,
                        FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_quiz_logs_studentId` ON `quiz_logs`(`studentId`)")

                database.execSQL("""
                    CREATE TABLE `layout_templates` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `layoutData` TEXT NOT NULL
                    )
                """)
                database.execSQL("""
                    CREATE TABLE `conditional_formatting_rules` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `ruleType` TEXT NOT NULL,
                        `parameters` TEXT NOT NULL,
                        `style` TEXT NOT NULL,
                        `enabled` INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // 2. Recreate students table to change groupId type
                database.execSQL("""
                    CREATE TABLE `students_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `stringId` TEXT,
                        `firstName` TEXT NOT NULL,
                        `lastName` TEXT NOT NULL,
                        `nickname` TEXT,
                        `gender` TEXT NOT NULL DEFAULT 'Boy',
                        `groupId` INTEGER,
                        `initials` TEXT,
                        `xPosition` REAL NOT NULL,
                        `yPosition` REAL NOT NULL,
                        `customWidth` INTEGER,
                        `customHeight` INTEGER,
                        `customBackgroundColor` TEXT,
                        `customOutlineColor` TEXT,
                        `customTextColor` TEXT,
                        FOREIGN KEY(`groupId`) REFERENCES `student_groups`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_students_new_groupId` ON `students_new`(`groupId`)")
                // Copy data, excluding the old groupId which was TEXT and likely not useful as an INT.
                database.execSQL("""
                    INSERT INTO students_new (id, stringId, firstName, lastName, nickname, gender, initials, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor)
                    SELECT id, stringId, firstName, lastName, nickname, gender, initials, xPosition, yPosition, customWidth, customHeight, customBackgroundColor, customOutlineColor, customTextColor FROM students
                """)
                database.execSQL("DROP TABLE students")
                database.execSQL("ALTER TABLE students_new RENAME TO students")

                // 3. Recreate homework_logs table to add new fields and change types
                database.execSQL("""
                    CREATE TABLE `homework_logs_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `studentId` INTEGER NOT NULL,
                        `homeworkName` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `status` TEXT,
                        `marksData` TEXT,
                        `numItems` INTEGER,
                        `comment` TEXT,
                        FOREIGN KEY(`studentId`) REFERENCES `students`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_homework_logs_new_studentId` ON `homework_logs_new`(`studentId`)")
                // Copy data from old to new, mapping columns
                database.execSQL("""
                    INSERT INTO homework_logs_new (id, studentId, homeworkName, timestamp, status, comment)
                    SELECT id, studentId, assignmentName, loggedAt, status, comment FROM homework_logs
                """)
                database.execSQL("DROP TABLE homework_logs")
                database.execSQL("ALTER TABLE homework_logs_new RENAME TO homework_logs")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "seating_chart_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                // Consider removing fallbackToDestructiveMigration in production if you want to ensure all migrations are handled explicitly.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
