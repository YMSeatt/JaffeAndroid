package com.example.myapplication.data

import android.content.Context
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

    // Add @Provides methods for your DAOs here if they are not already provided elsewhere
    // For example:
    // @Provides
    // @Singleton
    // fun provideStudentDao(appDatabase: AppDatabase): StudentDao {
    //     return appDatabase.studentDao()
    // }
    //
    // @Provides
    // @Singleton
    // fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
    //     return Room.databaseBuilder(
    //         appContext,
    //         AppDatabase::class.java,
    //         "app_database"
    //     ).build()
    // }
}
