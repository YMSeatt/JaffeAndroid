package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.util.EncryptionUtil
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EncryptionModule {

    @Provides
    @Singleton
    fun provideEncryptionUtil(@ApplicationContext context: Context): EncryptionUtil {
        return EncryptionUtil(context)
    }
}
