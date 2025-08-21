package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.AppDatabase
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
    fun provideAppDatabase(@ApplicationContext context: Context) = AppDatabase.getDatabase(context)

    @Provides
    fun provideStudentDao(appDatabase: AppDatabase) = appDatabase.studentDao()

    @Provides
    fun provideFurnitureDao(appDatabase: AppDatabase) = appDatabase.furnitureDao()

    @Provides
    fun provideLayoutTemplateDao(appDatabase: AppDatabase) = appDatabase.layoutTemplateDao()

    @Provides
    fun provideBehaviorEventDao(appDatabase: AppDatabase) = appDatabase.behaviorEventDao()

    @Provides
    fun provideQuizLogDao(appDatabase: AppDatabase) = appDatabase.quizLogDao()

    @Provides
    fun provideHomeworkLogDao(appDatabase: AppDatabase) = appDatabase.homeworkLogDao()

    @Provides
    fun provideStudentGroupDao(appDatabase: AppDatabase) = appDatabase.studentGroupDao()

    @Provides
    fun provideConditionalFormattingRuleDao(appDatabase: AppDatabase) = appDatabase.conditionalFormattingRuleDao()

    @Provides
    fun provideHomeworkTemplateDao(appDatabase: AppDatabase) = appDatabase.homeworkTemplateDao()

    @Provides
    fun provideQuizTemplateDao(appDatabase: AppDatabase) = appDatabase.quizTemplateDao()

    @Provides
    fun provideQuizMarkTypeDao(appDatabase: AppDatabase) = appDatabase.quizMarkTypeDao()
}
