package com.example.myapplication.di

import android.app.Application
import android.content.Context
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.ReminderDao
import com.example.myapplication.data.importer.JsonImporter
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.util.ReminderManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppPreferencesRepository(@ApplicationContext context: Context): AppPreferencesRepository {
        return AppPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideReminderDao(appDatabase: AppDatabase): ReminderDao {
        return appDatabase.reminderDao()
    }

    @Provides
    @Singleton
    fun provideReminderManager(application: Application): ReminderManager {
        return ReminderManager(application)
    }

    @Provides
    @Singleton
    fun provideJsonImporter(
        application: Application,
        appDatabase: AppDatabase
    ): JsonImporter {
        return JsonImporter(
            application,
            appDatabase.studentDao(),
            appDatabase.furnitureDao(),
            appDatabase.behaviorEventDao(),
            appDatabase.homeworkLogDao(),
            appDatabase.studentGroupDao(),
            appDatabase.customBehaviorDao(),
            appDatabase.customHomeworkStatusDao(),
            appDatabase.customHomeworkTypeDao(),
            appDatabase.homeworkTemplateDao()
        )
    }
}