# Ktor
-keep class io.ktor.** { *; }
-keepnames class io.ktor.** { *; }
-dontwarn kotlinx.atomicfu.**
-dontwarn io.ktor.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class es.hugopvigo.precioluz.data.api.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
