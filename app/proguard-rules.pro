# ===================================================================
# Keep & Grow - Production R8 / ProGuard Optimization & Keep Rules
# ===================================================================

# -------------------------------------------------------------------
# 1. General Android & Optimization Settings
# -------------------------------------------------------------------
-repackageclasses
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Preserve line numbers and source file names for production crash reporting
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# -------------------------------------------------------------------
# 2. Kotlin Coroutines & Flow
# -------------------------------------------------------------------
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# -------------------------------------------------------------------
# 3. Room Database
# -------------------------------------------------------------------
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.TypeConverter *;
}
-dontwarn androidx.room.paging.**

# Keep our specific Room Entities
-keep class com.example.data.database.** { *; }

# -------------------------------------------------------------------
# 4. Moshi & JSON Models (Reflection & Codegen)
# -------------------------------------------------------------------
-keepclasseswithmembers class * {
    @com.squareup.moshi.Json <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**

# Keep generated Moshi JSON adapters
-keep class *JsonAdapter {
    public <init>(com.squareup.moshi.Moshi);
    public <init>(com.squareup.moshi.Moshi, java.lang.reflect.Type[]);
}

# Keep Network DTOs
-keep class com.example.network.models.** { *; }

# -------------------------------------------------------------------
# 5. Retrofit & OkHttp
# -------------------------------------------------------------------
-keepattributes Signature, Exceptions
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# Keep OkHttp connection and caching internals
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep our Retrofit API service interfaces
-keep interface com.example.network.FinnhubApiService { *; }

# -------------------------------------------------------------------
# 6. Domain Models & AI Gemini Client
# -------------------------------------------------------------------
-keep class com.example.domain.** { *; }
-keep class com.example.data.api.StockAnalysis { *; }
-keep class com.example.data.api.GeminiClient { *; }
-keep class com.example.ui.viewmodel.StockData { *; }
-keep class com.example.ui.viewmodel.HighGrowthStock { *; }
-keep class com.example.ui.viewmodel.ProfileType { *; }
-keep class com.example.ui.viewmodel.ScreenTab { *; }
-keep class com.example.ui.viewmodel.RebalanceStockAction { *; }

# -------------------------------------------------------------------
# 7. Jetpack Compose & Material 3
# -------------------------------------------------------------------
-keep class androidx.compose.material3.** { *; }
-keep class com.example.ui.components.** { *; }
-dontwarn androidx.compose.**

# -------------------------------------------------------------------
# 8. Roborazzi & Testing (Ignore during release packaging)
# -------------------------------------------------------------------
-dontwarn com.github.takahirom.roborazzi.**
