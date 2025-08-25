package com.example.myapplication.data

import android.content.Context
import androidx.room.Room
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
    fun provideAppPreferencesRepository(@ApplicationContext context: Context): com.example.myapplication.preferences.AppPreferencesRepository {
        return com.example.myapplication.preferences.AppPreferencesRepository(context)
    }

    @Provides
    @Singleton
    fun provideStudentRepository(
        studentDao: StudentDao,
        behaviorEventDao: BehaviorEventDao,
        homeworkLogDao: HomeworkLogDao,
        quizLogDao: QuizLogDao,
        furnitureDao: FurnitureDao,
        layoutTemplateDao: LayoutTemplateDao,
        quizMarkTypeDao: QuizMarkTypeDao,
        @ApplicationContext context: Context
    ): StudentRepository {
        return StudentRepository(
            studentDao,
            behaviorEventDao,
            homeworkLogDao,
            quizLogDao,
            furnitureDao,
            layoutTemplateDao,
            quizMarkTypeDao,
            context
        )
    }
}
