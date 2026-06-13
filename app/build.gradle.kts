import com.android.build.api.dsl.ApplicationExtension
import java.util.Properties

plugins {
    id("com.android.application")
    id("com.google.devtools.ksp")
    id("kotlin-parcelize")
    kotlin("plugin.compose") version "2.3.21"
}

extensions.configure<ApplicationExtension> {

    signingConfigs {
        create("customDebug") {
            storeFile =
                file("/home/matt/Downloads/keystore/paycalculator_debug.jks")
            storePassword = "!935Gr8t"
            keyPassword = "!935Gr8t"
            keyAlias = "pay_debug"
            enableV1Signing = true
            enableV2Signing = true
        }
        create("release") {
            storeFile =
                file("/home/matt/Downloads/keystore/paycalculator_debug.jks")
            storePassword = "!935Gr8t"
            keyPassword = "!935Gr8t"
            keyAlias = "pay_debug"
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    namespace = "ms.mattschlenkrich.paycalculator"
    compileSdk = 37

    defaultConfig {
        applicationId = "ms.mattschlenkrich.paycalculator"
        minSdk = 28
        targetSdk = 37
        versionCode = 2
        versionName = "v1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("customDebug")

        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }
        val masterPassword = properties.getProperty("master.password")
        buildConfigField("String", "MASTER_PASSWORD", "\"$masterPassword\"")
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("customDebug")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}


kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_0)
    }
}

dependencies {
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.gson)
    implementation(libs.material)
    implementation(libs.mockito.core)
    implementation(libs.mockito.kotlin)
    implementation(libs.mockk)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.google.api.client)
    implementation(libs.google.api.client.android)
    implementation(libs.google.api.services.drive)
    implementation(libs.google.api.client.gson)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    //coRoutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.ktx)

    // Kotlin Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)

    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

}