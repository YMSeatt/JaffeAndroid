package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.data.StudentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

import com.example.myapplication.data.EmailRepository
import com.example.myapplication.data.EmailScheduleDao
import com.example.myapplication.data.PendingEmailDao
import com.example.myapplication.util.SecurityUtil

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideEmailRepository(
        emailScheduleDao: EmailScheduleDao,
        pendingEmailDao: PendingEmailDao,
        securityUtil: SecurityUtil
    ): EmailRepository {
        return EmailRepository(
            emailScheduleDao,
            pendingEmailDao,
            securityUtil
        )
    }

    @Provides
    @Singleton
    fun provideStudentRepository(
        studentDao: com.example.myapplication.data.StudentDao,
        behaviorEventDao: com.example.myapplication.data.BehaviorEventDao,
        homeworkLogDao: com.example.myapplication.data.HomeworkLogDao,
        quizLogDao: com.example.myapplication.data.QuizLogDao,
        furnitureDao: com.example.myapplication.data.FurnitureDao,
        layoutTemplateDao: com.example.myapplication.data.LayoutTemplateDao,
        quizMarkTypeDao: com.example.myapplication.data.QuizMarkTypeDao,
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