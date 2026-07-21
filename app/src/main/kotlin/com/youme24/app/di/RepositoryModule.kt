package com.youme24.app.di

import com.youme24.app.data.gemini.GeminiRepositoryImpl
import com.youme24.app.data.location.LocationRepositoryImpl
import com.youme24.app.data.remote.supabase.AuthRepositoryImpl
import com.youme24.app.data.remote.supabase.MessageRepositoryImpl
import com.youme24.app.data.remote.supabase.PartnerRepositoryImpl
import com.youme24.app.data.remote.supabase.MemoryRepositoryImpl
import com.youme24.app.data.remote.supabase.UserRepositoryImpl
import com.youme24.app.domain.repository.IAuthRepository
import com.youme24.app.domain.repository.IMemoryRepository
import com.youme24.app.domain.repository.IGeminiRepository
import com.youme24.app.domain.repository.ILocationRepository
import com.youme24.app.domain.repository.IMessageRepository
import com.youme24.app.domain.repository.IPartnerRepository
import com.youme24.app.domain.repository.IUserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): IAuthRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): IUserRepository

    @Binds @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): IMessageRepository

    @Binds @Singleton
    abstract fun bindPartnerRepository(impl: PartnerRepositoryImpl): IPartnerRepository

    @Binds @Singleton
    abstract fun bindGeminiRepository(impl: GeminiRepositoryImpl): IGeminiRepository

    @Binds @Singleton
    abstract fun bindLocationRepository(impl: LocationRepositoryImpl): ILocationRepository

    @Binds @Singleton
    abstract fun bindMemoryRepository(impl: MemoryRepositoryImpl): IMemoryRepository
}
