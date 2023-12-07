plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

//plugins {
//    `android-library`
//    `kotlin-android`
//}
//apply<MainGradlePlugin>()

android {
    namespace = "com.paradise.common"

    compileSdk = 34

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    buildFeatures {
        viewBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
}



dependencies {
    core_model()

    // dagger hilt
    hilt()
    // ExoPlayer
    exoPlayer()
    // mlkit face detection
    mlkit()
    // Camera
    camera()
    // GPS api
    implementation(Dependencies.Location)

    // retrofit
    retrofit()
    // Room DB
    room()
    // Preferences DataStore
    dataStore()
    // TED
    tedPermission()
    //Coroutine
    coroutine()
    // ktx
    ktx()
    // viewmodel_ktx
    lifecycleKTX()
    // RxBinding
    rxBinding()

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
