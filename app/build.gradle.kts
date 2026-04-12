plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    kotlin("plugin.compose") version "2.1.0"
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile =
                file("/mnt/047353E96D6DD7F2/Projects/AndroidProject/keystore/paycalculator_debug.jks")
            storePassword = "!935Gr8t"
            keyPassword = "!935Gr8t"
            keyAlias = "pay_debug"
        }
        create("release") {
            storeFile =
//                file("C:\\Users\\matt_\\OneDrive\\projects\\AndroidProject\\keystore\\matt_signing.jks")
                file("/mnt/02D83355D83345E7/project/AndroidProject/keystore/matt__new_signing.jks")
            storePassword = "!935Gr8t"
            keyAlias = "key0"
            keyPassword = "!935Gr8t"
        }
    }
    namespace = "ms.mattschlenkrich.paycalculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "ms.mattschlenkrich.paycalculator"
        minSdk = 28
        targetSdk = 36
        versionCode = 2
        versionName = "v1.0"
//        ksp {
//            arg("room.schemaLocation", "$projectDir/schemas")
//        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        viewBinding = true
        compose = true
    }
    buildToolsVersion = "36.0.0"
//    kotlinOptions {
//        jvmTarget = JavaVersion.VERSION_17
//    }
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.mockito.core)
    implementation(libs.mockito.kotlin)
    implementation(libs.mockk)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
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
//    testImplementation("junit:junit:4.12")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    val roomVersion = "2.8.4"
    implementation(libs.androidx.room.runtime)
    // To use Kotlin symbol processing tool (ksp)id("androidx.navigation.safeargs.kotlin")
    ksp(libs.androidx.room.compiler)

    //coRoutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.room.ktx)

    val navVersion = "2.9.7"
    // Kotlin Navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
//    ksp("androidx.navigation.safeargs.kotlin:$nav_version")
//    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")

    //Lifecycle architecture
    val lifecycleVersion = "2.10.0"
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    // LiveData
    implementation(libs.androidx.lifecycle.livedata.ktx)
    // Annotation processor
    ksp(libs.androidx.room.compiler)

    val material3Version = "1.4.0"
    val composeVersion = "1.7.8"

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

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}