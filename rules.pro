-dontwarn kotlinx.atomicfu.**
-dontwarn kotlinx.serialization.**
-dontwarn javax.annotation.**

-keep class me.sudodios.codewalker.models.** { *; }
-keep class me.sudodios.codewalker.core.LibCore {
    native <methods>;
}
-keep class kotlinx.coroutines.** { *; }
-keep class kotlinx.coroutines.swing.SwingDispatcherFactory
-keep class kotlinx.serialization.** { *; }

#gson
-keepattributes Signature
-keep class com.google.gson.** { *; }
-keep class com.google.gson.reflect.TypeToken { *; }
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken
-keep class androidx.compose.runtime.** { *; }