package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkStatusDao
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.QuizLogDao
import com.example.myapplication.data.EmailScheduleDao
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.GuideDao
import com.example.myapplication.data.HomeworkDao
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.LayoutTemplateDao
import com.example.myapplication.data.PendingEmailDao
import com.example.myapplication.data.QuizDao
import com.example.myapplication.data.QuizMarkTypeDao
import com.example.myapplication.data.QuizTemplateDao
import com.example.myapplication.data.ReminderDao
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.SystemBehaviorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies.
 *
 * This module manages the lifecycle and provision of the [AppDatabase] and its
 * associated Data Access Objects (DAOs). It acts as the "Foundational Layer" of the
 * Dependency Injection architecture.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the singleton instance of the [AppDatabase].
     *
     * **Scoping**: Marked as [@Singleton] to ensure that only one instance of the
     * database is created for the entire application lifecycle. This prevents multiple
     * open connections to the SQLite file and ensures that reactive triggers
     * from Room's [androidx.room.InvalidationTracker] are consistent across all components.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /** Provides the [StudentDao] for student metadata and spatial layout management. */
    @Provides
    fun provideStudentDao(appDatabase: AppDatabase): StudentDao {
        return appDatabase.studentDao()
    }

    /** Provides the [BehaviorEventDao] for tracking student behavioral incidents. */
    @Provides
    fun provideBehaviorEventDao(appDatabase: AppDatabase): BehaviorEventDao {
        return appDatabase.behaviorEventDao()
    }

    /** Provides the [HomeworkLogDao] for homework completion and effort tracking. */
    @Provides
    fun provideHomeworkLogDao(appDatabase: AppDatabase): HomeworkLogDao {
        return appDatabase.homeworkLogDao()
    }

    /** Provides the [QuizLogDao] for recording academic quiz performance. */
    @Provides
    fun provideQuizLogDao(appDatabase: AppDatabase): QuizLogDao {
        return appDatabase.quizLogDao()
    }

    /** Provides the [QuizMarkTypeDao] for granular quiz scoring definitions. */
    @Provides
    fun provideQuizMarkTypeDao(appDatabase: AppDatabase): QuizMarkTypeDao {
        return appDatabase.quizMarkTypeDao()
    }

    /** Provides the [CustomBehaviorDao] for user-defined behavior categories. */
    @Provides
    fun provideCustomBehaviorDao(appDatabase: AppDatabase): CustomBehaviorDao {
        return appDatabase.customBehaviorDao()
    }

    /** Provides the [CustomHomeworkTypeDao] for user-defined homework categories. */
    @Provides
    fun provideCustomHomeworkTypeDao(appDatabase: AppDatabase): CustomHomeworkTypeDao {
        return appDatabase.customHomeworkTypeDao()
    }

    /** Provides the [StudentGroupDao] for classroom group management and relational styling. */
    @Provides
    fun provideStudentGroupDao(appDatabase: AppDatabase): StudentGroupDao {
        return appDatabase.studentGroupDao()
    }

    /** Provides the [FurnitureDao] for managing physical classroom layout items. */
    @Provides
    fun provideFurnitureDao(appDatabase: AppDatabase): FurnitureDao {
        return appDatabase.furnitureDao()
    }

    /** Provides the [LayoutTemplateDao] for saving and applying spatial arrangement snapshots. */
    @Provides
    fun provideLayoutTemplateDao(appDatabase: AppDatabase): LayoutTemplateDao {
        return appDatabase.layoutTemplateDao()
    }

    /** Provides the [EmailScheduleDao] for automated report scheduling. */
    @Provides
    fun provideEmailScheduleDao(appDatabase: AppDatabase): EmailScheduleDao {
        return appDatabase.emailScheduleDao()
    }

    /** Provides the [GuideDao] for horizontal and vertical canvas alignment aids. */
    @Provides
    fun provideGuideDao(appDatabase: AppDatabase): GuideDao {
        return appDatabase.guideDao()
    }

    /** Provides the [QuizTemplateDao] for reusable quiz scoring structures. */
    @Provides
    fun provideQuizTemplateDao(appDatabase: AppDatabase): QuizTemplateDao {
        return appDatabase.quizTemplateDao()
    }

    /** Provides the [HomeworkTemplateDao] for reusable homework task structures. */
    @Provides
    fun provideHomeworkTemplateDao(appDatabase: AppDatabase): HomeworkTemplateDao {
        return appDatabase.homeworkTemplateDao()
    }

    /** Provides the [ConditionalFormattingRuleDao] for managing reactive UI styling logic. */
    @Provides
    fun provideConditionalFormattingRuleDao(appDatabase: AppDatabase): ConditionalFormattingRuleDao {
        return appDatabase.conditionalFormattingRuleDao()
    }

    /** Provides the [CustomHomeworkStatusDao] for custom task completion statuses. */
    @Provides
    fun provideCustomHomeworkStatusDao(appDatabase: AppDatabase): CustomHomeworkStatusDao {
        return appDatabase.customHomeworkStatusDao()
    }

    /** Provides the [ReminderDao] for teacher-specific tasks and system-level alarms. */
    @Provides
    fun provideReminderDao(appDatabase: AppDatabase): ReminderDao {
        return appDatabase.reminderDao()
    }

    /** Provides the [SystemBehaviorDao] for standardized, predefined behavioral feedback. */
    @Provides
    fun provideSystemBehaviorDao(appDatabase: AppDatabase): SystemBehaviorDao {
        return appDatabase.systemBehaviorDao()
    }

    /** Provides the [PendingEmailDao] for the reliability-focused email queue. */
    @Provides
    fun providePendingEmailDao(appDatabase: AppDatabase): PendingEmailDao {
        return appDatabase.pendingEmailDao()
    }

    /** Provides the [QuizDao] for normalized quiz assignment management. */
    @Provides
    fun provideQuizDao(appDatabase: AppDatabase): QuizDao {
        return appDatabase.quizDao()
    }

    /** Provides the [HomeworkDao] for normalized homework assignment management. */
    @Provides
    fun provideHomeworkDao(appDatabase: AppDatabase): HomeworkDao {
        return appDatabase.homeworkDao()
    }
}
