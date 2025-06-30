// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val agp_version by extra("8.1.2")
    repositories {
        google()
    }
    dependencies {
        val nav_version = "2.8.9"
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
    }
}
plugins {
    // Replace `<...>` with the plugin name appropriate for your target environment
    kotlin("jvm") version "1.9.0"
    // For example, if your target environment is JVM:
    // kotlin("jvm") version "2.1.0"
    // If your target is Kotlin Multiplatform:
    // kotlin("multiplatform") version "2.1.0"

    id("com.android.application") version "8.1.4" apply false
    id("org.jetbrains.kotlin.android") version "2.1.20" apply false
    id("com.android.library") version "8.11.0" apply false
    id("com.google.devtools.ksp") version "2.1.20-1.0.31" apply false
    id("com.google.dagger.hilt.android") version "2.42" apply false
}