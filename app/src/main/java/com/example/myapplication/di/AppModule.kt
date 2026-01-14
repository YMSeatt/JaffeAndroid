package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.preferences.AppPreferencesRepository
import com.example.myapplication.util.EncryptionKeyProvider
import com.example.myapplication.util.EncryptionUtil
import com.example.myapplication.util.KeyProvider
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
    fun provideKeyProvider(@ApplicationContext context: Context): KeyProvider {
        return EncryptionKeyProvider(context)
    }

    @Provides
    @Singleton
    fun provideEncryptionUtil(keyProvider: KeyProvider): EncryptionUtil {
        return EncryptionUtil(keyProvider.getKey())
    }
}
