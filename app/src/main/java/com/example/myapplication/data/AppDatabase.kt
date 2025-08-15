package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Student::class, BehaviorEvent::class, HomeworkLog::class], version = 5, exportSchema = false) // Incremented version to 5
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDao
    abstract fun behaviorEventDao(): BehaviorEventDao
    abstract fun homeworkLogDao(): HomeworkLogDao

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
                // You might want to populate existing rows with generated initials here
                // For example, by querying all students, generating initials, and updating them.
                // This example keeps it simple by adding the column as nullable.
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "seating_chart_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // Added MIGRATION_3_4 and MIGRATION_4_5
                // Consider removing fallbackToDestructiveMigration in production if you want to ensure all migrations are handled explicitly.
                .fallbackToDestructiveMigration() 
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
