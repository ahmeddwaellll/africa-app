# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Gson classes
-keep class com.google.gson.** { *; }

# Keep Retrofit classes
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# Keep Room classes
-keep class androidx.room.** { *; }

# Uncomment this to preserve the line number information for debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to hide the original source file name.
#-renamesourcefileattribute SourceFile
