# YouMe — ProGuard / R8 rules

# Kotlin
-dontwarn kotlin.**
-keep class kotlin.** { *; }

# Hilt
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Supabase-kt / Ktor
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# Coil
-dontwarn coil.**

# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Media3
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Google Maps
-keep class com.google.android.gms.maps.** { *; }
-dontwarn com.google.android.gms.**

# Domain models (used in Supabase DTOs)
-keep class com.youme24.app.domain.model.** { *; }
-keep class com.youme24.app.data.remote.supabase.dto.** { *; }
