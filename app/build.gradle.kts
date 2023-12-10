import Dependencies.Location
import Dependencies.MpAndroidChart

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.dagger.hilt.android")
    kotlin("kapt")
}

android {
    namespace = "com.paradise.drowsydetector"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.paradise.drowsydetector"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_18
        targetCompatibility = JavaVersion.VERSION_18
    }
    kotlinOptions {
        jvmTarget = "18"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
//    hilt {
//        enableAggregatingTask = true
//    }
}

dependencies {
    // LeakCanary
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.12")
//    releaseImplementation("com.squareup.leakcanary:leakcanary-android-no-op:2.7")
    core_model()
//    core_domain()
    core_common()
//    core_data()
    core_common_ui()
    feature_analyze()
    feature_home()
    feature_setting()
    feature_statistic()
    // navi
    navigation()
    // dagger hilt
    hilt()
//    // Preferences DataStore
//    dataStore()
//    // ExoPlayer
//    exoPlayer()
//    // TedPermission
    tedPermission()
    // mlkit face detection
//    mlkit()
//    // GPS api
//    implementation(Location)
//    // Camera
//    camera()
//    // ktx
//    ktx()
//    // viewmodel_ktx
//    lifecycleKTX()
//    // retrofit
//    retrofit()
//    //Coroutine
//    coroutine()
//    // Room DB
//    room()
//    // MPAndroidChart
//    implementation(MpAndroidChart)
//    // RxBinding
//    rxBinding()
//    // FlowBinding
//    val flowbinding_version = "1.2.0"
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-android:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-material:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-activity:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-appcompat:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-core:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-lifecycle:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-navigation:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-preference:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-recyclerview:${flowbinding_version}")
//    implementation("io.github.reactivecircus.flowbinding:flowbinding-viewpager2:${flowbinding_version}")

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

kapt {
    correctErrorTypes = true
}