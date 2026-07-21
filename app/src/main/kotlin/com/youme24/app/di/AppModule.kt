package com.youme24.app.di

import android.content.Context
import com.youme24.app.data.crypto.E2ECryptoService
import com.youme24.app.data.crypto.KeyStorage
import com.youme24.app.data.local.DataStoreManager
import com.youme24.app.data.remote.supabase.SupabaseClientProvider
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
    fun provideSupabaseClient(): SupabaseClientProvider =
        SupabaseClientProvider()

    @Provides
    @Singleton
    fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager =
        DataStoreManager(context)

    @Provides
    @Singleton
    fun provideKeyStorage(@ApplicationContext context: Context): KeyStorage =
        KeyStorage(context)

    @Provides
    @Singleton
    fun provideE2ECryptoService(keyStorage: KeyStorage): E2ECryptoService =
        E2ECryptoService(keyStorage)
}
