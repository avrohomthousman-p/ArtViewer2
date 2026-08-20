# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html



# Keep generic type signatures (Retrofit needs these)
-keepattributes Signature
-keepattributes *Annotation*

# Keep only the generated serializers
-keep class **$$serializer { *; }

# Keep members annotated with @Serializable
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# Keep API models
-keep class com.housmantech.artviewer.data.remote.** { *; }

# Keep Retrofit interfaces
-keep interface com.housmantech.artviewer.data.remote.** { *; }

# Keep Jake Wharton’s converter
-keep class com.jakewharton.retrofit.** { *; }

# Keep generic signature of Call, Response
# (R8 full mode strips signatures from non-kept items)
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Suspend functions wrap return types in Continuation<T>
# R8 full mode strips the generic parameter unless kept
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation




# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile