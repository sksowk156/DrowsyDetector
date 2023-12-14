import Dependencies.Location
import Dependencies.MpAndroidChart

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.paradise.statistic"
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
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    kotlinOptions {
        jvmTarget = "18"
    }
}

dependencies {
    core_model()
    core_domain()
    core_data()
    core_common()
    core_common_ui()
    // navi
    navigation()
    // dagger hilt
    hilt()
    // Preferences DataStore
    dataStore()
    // ktx
    ktx()
    // viewmodel_ktx
    lifecycleKTX()
    //Coroutine
    coroutine()
    // Room DB
    room()
    // MPAndroidChart
    implementation(MpAndroidChart)
    // RxBinding
    rxBinding()

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}