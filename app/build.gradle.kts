plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
}

android {
    signingConfigs {
        getByName("debug") {
            storeFile =
                file("/mnt/02D83355D83345E7/project/AndroidProject/keystore/matt__new_signing.jks")
            storePassword = "!935Gr8t"
            keyPassword = "!935Gr8t"
            keyAlias = "key0"
        }
        create("release") {
            storeFile =
                file("/mnt/02D83355D83345E7/project/AndroidProject/keystore/matt__new_signing.jks")
            storePassword = "!935Gr8t"
            keyAlias = "key0"
            keyPassword = "!935Gr8t"
        }
    }
    namespace = "ms.mattschlenkrich.paycalculator"
    compileSdk = 35

    defaultConfig {
        applicationId = "ms.mattschlenkrich.paycalculator"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "v1.0"
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    packaging {
        resources {
            excludes.add("META-INF/*")
        }
    }
    buildFeatures {
        //noinspection DataBindingWithoutKapt
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.mockito:mockito-core:5.17.0")
    implementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    implementation("io.mockk:mockk:1.14.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    val room_version = "2.7.1"
    implementation("androidx.room:room-runtime:$room_version")
    // To use Kotlin symbol processing tool (ksp)id("androidx.navigation.safeargs.kotlin")
    ksp("androidx.room:room-compiler:$room_version")

    //coRoutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.room:room-ktx:$room_version")

    val nav_version = "2.9.0"
    // Kotlin Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
//    ksp("androidx.navigation.safeargs.kotlin:$nav_version")
//    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")

    //Lifecycle architecture
    val lifecycle_version = "2.9.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version")
    // Annotation processor
    ksp("androidx.room:room-compiler:$room_version")

    val material3_version = "1.3.2"

    implementation("androidx.compose.material3:material3:$material3_version")

}