# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Ktor / CIO
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn org.slf4j.**
