// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val agpVersion by extra("8.1.2")
    repositories {
        google()
    }
    dependencies {
        val navVersion = "2.9.7"
        classpath(libs.androidx.navigation.safe.args.gradle.plugin)
    }
}
plugins {
    // Replace `<...>` with the plugin name appropriate for your target environment
//    kotlin("jvm") version "1.9.0"
    // For example, if your target environment is JVM:
    kotlin("jvm") version "2.1.0"
    // If your target is Kotlin Multiplatform:
    // kotlin("multiplatform") version "2.1.0"

    id("com.android.application") version "9.2.0" apply false
    id("org.jetbrains.kotlin.android") version "2.3.20" apply false
    id("com.android.library") version "9.2.0" apply false
    id("com.google.devtools.ksp") version "2.3.2" apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
}