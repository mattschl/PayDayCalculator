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

    implementation("androidx.core:core-ktx:1.18.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("org.mockito:mockito-core:5.23.0")
    implementation("org.mockito.kotlin:mockito-kotlin:6.3.0")
    implementation("io.mockk:mockk:1.14.9")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.9.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.9.7")
    implementation("androidx.test.ext:junit-ktx:1.3.0")
    implementation("com.google.api-client:google-api-client:2.8.0")
    implementation("com.google.api-client:google-api-client-android:2.8.0")
    implementation("com.google.apis:google-api-services-drive:v3-rev20250511-2.0.0")
    implementation("com.google.api-client:google-api-client-gson:2.8.0")
    implementation("com.google.android.gms:play-services-auth:21.3.0")
    implementation("androidx.credentials:credentials:1.6.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.6.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.2.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
//    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    val roomVersion = "2.8.4"
    implementation("androidx.room:room-runtime:$roomVersion")
    // To use Kotlin symbol processing tool (ksp)id("androidx.navigation.safeargs.kotlin")
    ksp("androidx.room:room-compiler:$roomVersion")

    //coRoutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("androidx.room:room-ktx:$roomVersion")

    val navVersion = "2.9.7"
    // Kotlin Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")
//    ksp("androidx.navigation.safeargs.kotlin:$nav_version")
//    implementation("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")

    //Lifecycle architecture
    val lifecycleVersion = "2.10.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycleVersion")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    // Annotation processor
    ksp("androidx.room:room-compiler:$roomVersion")

    val material3Version = "1.4.0"

    implementation("androidx.compose.material3:material3:$material3Version")

}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}