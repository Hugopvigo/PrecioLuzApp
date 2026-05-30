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
