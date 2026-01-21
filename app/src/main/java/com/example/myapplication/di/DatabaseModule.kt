package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.BehaviorEventDao
import com.example.myapplication.data.CustomBehaviorDao
import com.example.myapplication.data.CustomHomeworkTypeDao
import com.example.myapplication.data.HomeworkLogDao
import com.example.myapplication.data.QuizLogDao
import com.example.myapplication.data.EmailScheduleDao
import com.example.myapplication.data.FurnitureDao
import com.example.myapplication.data.GuideDao
import com.example.myapplication.data.HomeworkTemplateDao
import com.example.myapplication.data.LayoutTemplateDao
import com.example.myapplication.data.QuizMarkTypeDao
import com.example.myapplication.data.QuizTemplateDao
import com.example.myapplication.data.StudentDao
import com.example.myapplication.data.StudentGroupDao
import com.example.myapplication.data.ConditionalFormattingRuleDao
import com.example.myapplication.data.SystemBehaviorDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideStudentDao(appDatabase: AppDatabase): StudentDao {
        return appDatabase.studentDao()
    }

    @Provides
    fun provideBehaviorEventDao(appDatabase: AppDatabase): BehaviorEventDao {
        return appDatabase.behaviorEventDao()
    }

    @Provides
    fun provideHomeworkLogDao(appDatabase: AppDatabase): HomeworkLogDao {
        return appDatabase.homeworkLogDao()
    }

    @Provides
    fun provideQuizLogDao(appDatabase: AppDatabase): QuizLogDao {
        return appDatabase.quizLogDao()
    }

    @Provides
    fun provideQuizMarkTypeDao(appDatabase: AppDatabase): QuizMarkTypeDao {
        return appDatabase.quizMarkTypeDao()
    }

    @Provides
    fun provideCustomBehaviorDao(appDatabase: AppDatabase): CustomBehaviorDao {
        return appDatabase.customBehaviorDao()
    }

    @Provides
    fun provideCustomHomeworkTypeDao(appDatabase: AppDatabase): CustomHomeworkTypeDao {
        return appDatabase.customHomeworkTypeDao()
    }

    @Provides
    fun provideStudentGroupDao(appDatabase: AppDatabase): StudentGroupDao {
        return appDatabase.studentGroupDao()
    }

    @Provides
    fun provideFurnitureDao(appDatabase: AppDatabase): FurnitureDao {
        return appDatabase.furnitureDao()
    }

    @Provides
    fun provideLayoutTemplateDao(appDatabase: AppDatabase): LayoutTemplateDao {
        return appDatabase.layoutTemplateDao()
    }

    @Provides
    fun provideEmailScheduleDao(appDatabase: AppDatabase): EmailScheduleDao {
        return appDatabase.emailScheduleDao()
    }

    @Provides
    fun provideGuideDao(appDatabase: AppDatabase): GuideDao {
        return appDatabase.guideDao()
    }

    @Provides
    fun provideQuizTemplateDao(appDatabase: AppDatabase): QuizTemplateDao {
        return appDatabase.quizTemplateDao()
    }

    @Provides
    fun provideHomeworkTemplateDao(appDatabase: AppDatabase): HomeworkTemplateDao {
        return appDatabase.homeworkTemplateDao()
    }

    @Provides
    fun provideConditionalFormattingRuleDao(appDatabase: AppDatabase): ConditionalFormattingRuleDao {
        return appDatabase.conditionalFormattingRuleDao()
    }

    @Provides
    fun provideSystemBehaviorDao(appDatabase: AppDatabase): SystemBehaviorDao {
        return appDatabase.systemBehaviorDao()
    }
}
