# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Hilt
-keep,allowobfuscation,allowshrinking class dagger.hilt.internal.aggregatedroot.AggregatedRootImpl

# Room
-keep class * extends androidx.room.RoomDatabase

# General Compose (Prevent shrinking issues with generic classes if any)
-keep class androidx.compose.runtime.Composer

# Model Classes
-keep class com.valmortheosz.valduplipict.data.model.** { *; }
