# Proguard rules for PrecioLuz App

# Retrofit & kotlinx-serialization
-keepattributes *Annotation*, Signature
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }
-keep class com.precioluz.app.data.network.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Room
-keep class * extends androidx.room.RoomDatabase { *; }

# Tink (via androidx.security) referencia anotaciones errorprone que no van en el APK
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi
