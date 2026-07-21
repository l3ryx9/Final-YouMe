package com.youme24.app.data.remote.supabase

import com.youme24.app.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fournit le client Supabase Kotlin (supabase-kt).
 * Modules activés : Auth, Postgrest, Realtime, Storage, Functions.
 * Injection via Hilt — voir SupabaseModule.
 */
@Singleton
class SupabaseClientProvider @Inject constructor() {

    val client = createSupabaseClient(
        supabaseUrl    = BuildConfig.SUPABASE_URL,
        supabaseKey    = BuildConfig.SUPABASE_ANON_KEY,
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
        install(Functions)
    }
}
