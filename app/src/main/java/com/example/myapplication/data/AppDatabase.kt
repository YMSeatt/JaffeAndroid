package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Student::class, BehaviorEvent::class, HomeworkLog::class, Furniture::class], version = 6, exportSchema = false) // Incremented version to 6
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun behaviorEventDao(): BehaviorEventDao
    abstract fun homeworkLogDao(): HomeworkLogDao
    abstract fun furnitureDao(): FurnitureDao

    companion object {
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "seating_chart_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6) // Added MIGRATION_5_6
                // Consider removing fallbackToDestructiveMigration in production if you want to ensure all migrations are handled explicitly.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
