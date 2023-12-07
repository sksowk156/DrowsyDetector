import Versions.activityKtx
import Versions.camera
import Versions.coroutine
import Versions.daggerHilt
import Versions.dataStore
import Versions.exoPlayer
import Versions.flowbinding
import Versions.fragmentKtx
import Versions.gson
import Versions.lifecycle
import Versions.location
import Versions.mlFaceDetection
import Versions.mlFaceMesh
import Versions.mpChart
import Versions.navigation
import Versions.retrofit
import Versions.room
import Versions.rxbinding
import Versions.tedPermission
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

object Dependencies {
    // Navigation
    const val navigationFragment = ("androidx.navigation:navigation-fragment-ktx:${navigation}")
    const val navigationUi = ("androidx.navigation:navigation-ui-ktx:${navigation}")
    const val navigationRuntime = "androidx.navigation:navigation-runtime-ktx:${navigation}"

    // dagger hilt
    const val hiltAndroid = "com.google.dagger:hilt-android:${daggerHilt}"
    const val hiltCompiler = "com.google.dagger:hilt-android-compiler:${daggerHilt}"
//    const val hiltAgp = "com.google.dagger:hilt-android-gradle-plugin:${daggerHilt}"

    // Preferences DataStore (SharedPreferences like APIs)
    const val DataStore = ("androidx.datastore:datastore-preferences:${dataStore}")
    const val DataStoreRx2 = ("androidx.datastore:datastore-preferences-rxjava2:${dataStore}")
    const val DataStoreRx3 = ("androidx.datastore:datastore-preferences-rxjava3:${dataStore}")
    const val DataStoreCore = ("androidx.datastore:datastore-preferences-core:${dataStore}")

    // ExoPlayer
    const val ExoPlayerCore = ("com.google.android.exoplayer:exoplayer-core:${exoPlayer}")
    const val ExoPlayerDash = ("com.google.android.exoplayer:exoplayer-dash:${exoPlayer}")
    const val ExoPlayerUi = ("com.google.android.exoplayer:exoplayer-ui:${exoPlayer}")

    // TedPermission
    const val TedPermissionNormal = ("io.github.ParkSangGwon:tedpermission-normal:${tedPermission}")
    const val TedPermissionCoroutine =
        ("io.github.ParkSangGwon:tedpermission-coroutine:${tedPermission}")

    // MLkit
    const val MLkitFaceDetection = ("com.google.mlkit:face-detection:${mlFaceDetection}")
    const val MLkitFaceMesh = ("com.google.mlkit:face-mesh-detection:${mlFaceMesh}")

    // GPS api
    const val Location = ("com.google.android.gms:play-services-location:${location}")

    // The following line is optional, as the core library is included indirectly by camera-camera2
    const val CameraXCore = ("androidx.camera:camera-core:${camera}")
    const val CameraXCamera2 = ("androidx.camera:camera-camera2:${camera}")
    const val CameraXLifecycle = ("androidx.camera:camera-lifecycle:${camera}")
    const val CameraXView = ("androidx.camera:camera-view:${camera}")
    const val CameraXVision = ("androidx.camera:camera-mlkit-vision:${camera}")

    // activity_ktx
    const val ActivityKtx = ("androidx.activity:activity-ktx:${activityKtx}")

    // fragment_ktx, viewmodels()를 사용하기 위해
    const val FragmetKtx = ("androidx.fragment:fragment-ktx:${fragmentKtx}")

    // LifecycleScope_ktx
    const val LifecycleScopeViewModelKtx =
        ("androidx.lifecycle:lifecycle-viewmodel-ktx:${lifecycle}")
    const val LifecycleScopeRuntimeKtx = ("androidx.lifecycle:lifecycle-runtime-ktx:${lifecycle}")
    const val LifecycleScopeLivedataKtx = ("androidx.lifecycle:lifecycle-livedata-ktx:${lifecycle}")
    const val LifecycleScopeService = ("androidx.lifecycle:lifecycle-service:${lifecycle}")

    // retrofit
    const val Retrofit = ("com.squareup.retrofit2:retrofit:${retrofit}")
    const val RetrofitGson = ("com.squareup.retrofit2:converter-gson:${retrofit}")

    //gson
    const val Gson = ("com.google.code.gson:gson:${gson}")

    //Coroutine
    const val CoroutinesAndroid = ("org.jetbrains.kotlinx:kotlinx-coroutines-android:${coroutine}")
    const val CoroutinesCore = ("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutine}")

    // Room DB
    const val RoomKtx = ("androidx.room:room-ktx:${room}")
    const val RoomRuntime = ("androidx.room:room-runtime:${room}")
    const val RoomCompiler = ("androidx.room:room-compiler:${room}")

    // MPAndroidChart
    const val MpAndroidChart = ("com.github.PhilJay:MPAndroidChart:v${mpChart}")

    // RxBinding
    const val RxBinding = ("com.jakewharton.rxbinding4:rxbinding:${rxbinding}")
    const val RxBindingcore = ("com.jakewharton.rxbinding4:rxbinding-core:${rxbinding}")
    const val RxBindingappcompat = ("com.jakewharton.rxbinding4:rxbinding-appcompat:${rxbinding}")
    const val RxBindingdrawerlayout =
        ("com.jakewharton.rxbinding4:rxbinding-drawerlayout:${rxbinding}")
    const val RxBindingleanback = ("com.jakewharton.rxbinding4:rxbinding-leanback:${rxbinding}")
    const val RxBindingrecyclerview =
        ("com.jakewharton.rxbinding4:rxbinding-recyclerview:${rxbinding}")
    const val RxBindingslidingpanelayout =
        ("com.jakewharton.rxbinding4:rxbinding-slidingpanelayout:${rxbinding}")
    const val RxBindingswiperefreshlayout =
        ("com.jakewharton.rxbinding4:rxbinding-swiperefreshlayout:${rxbinding}")
    const val RxBindingviewpager = ("com.jakewharton.rxbinding4:rxbinding-viewpager:${rxbinding}")
    const val RxBindingviewpager2 = ("com.jakewharton.rxbinding4:rxbinding-viewpager2:${rxbinding}")
    const val RxBindingmaterial = ("com.jakewharton.rxbinding4:rxbinding-material:${rxbinding}")

    // FlowBinding
    const val FlowBinding =
        ("io.github.reactivecircus.flowbinding:flowbinding-android:${flowbinding}")
    const val FlowBindingmaterial =
        ("io.github.reactivecircus.flowbinding:flowbinding-material:${flowbinding}")
    const val FlowBindingactivity =
        ("io.github.reactivecircus.flowbinding:flowbinding-activity:${flowbinding}")
    const val FlowBindingappcompat =
        ("io.github.reactivecircus.flowbinding:flowbinding-appcompat:${flowbinding}")
    const val FlowBindingcore =
        ("io.github.reactivecircus.flowbinding:flowbinding-core:${flowbinding}")
    const val FlowBindinglifecycle =
        ("io.github.reactivecircus.flowbinding:flowbinding-lifecycle:${flowbinding}")
    const val FlowBindingnavigation =
        ("io.github.reactivecircus.flowbinding:flowbinding-navigation:${flowbinding}")
    const val FlowBindingpreference =
        ("io.github.reactivecircus.flowbinding:flowbinding-preference:${flowbinding}")
    const val FlowBindingrecyclerview =
        ("io.github.reactivecircus.flowbinding:flowbinding-recyclerview:${flowbinding}")
    const val FlowBindingviewpager2 =
        ("io.github.reactivecircus.flowbinding:flowbinding-viewpager2:${flowbinding}")
}

fun DependencyHandler.navigation() {
    implementation(Dependencies.navigationUi)
    implementation(Dependencies.navigationFragment)
}

fun DependencyHandler.room() {
    implementation(Dependencies.RoomKtx)
    implementation(Dependencies.RoomRuntime)
    kapt(Dependencies.RoomCompiler)
}

fun DependencyHandler.retrofit() {
    implementation(Dependencies.Retrofit)
    implementation(Dependencies.RetrofitGson)
    implementation(Dependencies.Gson)
//    implementation(Dependencies.okHttp)
//    implementation(Dependencies.okHttpLoggingInterceptor)
}

fun DependencyHandler.rxBinding() {
    implementation(Dependencies.RxBinding)
    implementation(Dependencies.RxBindingappcompat)
    implementation(Dependencies.RxBindingcore)
    implementation(Dependencies.RxBindingleanback)
    implementation(Dependencies.RxBindingmaterial)
    implementation(Dependencies.RxBindingdrawerlayout)
    implementation(Dependencies.RxBindingrecyclerview)
    implementation(Dependencies.RxBindingslidingpanelayout)
    implementation(Dependencies.RxBindingswiperefreshlayout)
    implementation(Dependencies.RxBindingviewpager)
    implementation(Dependencies.RxBindingviewpager2)
}

fun DependencyHandler.flowBinding() {
    implementation(Dependencies.FlowBinding)
    implementation(Dependencies.FlowBindinglifecycle)
    implementation(Dependencies.FlowBindingnavigation)
    implementation(Dependencies.FlowBindingrecyclerview)
    implementation(Dependencies.FlowBindingpreference)
    implementation(Dependencies.FlowBindingviewpager2)
    implementation(Dependencies.FlowBindingappcompat)
    implementation(Dependencies.FlowBindingcore)
    implementation(Dependencies.FlowBindingactivity)
    implementation(Dependencies.FlowBindingmaterial)
}

fun DependencyHandler.camera() {
    implementation(Dependencies.CameraXCore)
    implementation(Dependencies.CameraXCamera2)
    implementation(Dependencies.CameraXLifecycle)
    implementation(Dependencies.CameraXView)
    implementation(Dependencies.CameraXVision)
}

fun DependencyHandler.mlkit() {
    implementation(Dependencies.MLkitFaceMesh)
    implementation(Dependencies.MLkitFaceDetection)
}

fun DependencyHandler.coroutine() {
    implementation(Dependencies.CoroutinesCore)
    implementation(Dependencies.CoroutinesAndroid)
}

fun DependencyHandler.ktx() {
    implementation(Dependencies.FragmetKtx)
    implementation(Dependencies.ActivityKtx)
}

fun DependencyHandler.lifecycleKTX() {
    implementation(Dependencies.LifecycleScopeViewModelKtx)
    implementation(Dependencies.LifecycleScopeRuntimeKtx)
    implementation(Dependencies.LifecycleScopeLivedataKtx)
    implementation(Dependencies.LifecycleScopeService)
}

fun DependencyHandler.dataStore() {
    implementation(Dependencies.DataStore)
    implementation(Dependencies.DataStoreRx2)
    implementation(Dependencies.DataStoreRx3)
    implementation(Dependencies.DataStoreCore)
}

fun DependencyHandler.exoPlayer() {
    implementation(Dependencies.ExoPlayerCore)
    implementation(Dependencies.ExoPlayerDash)
    implementation(Dependencies.ExoPlayerUi)
}

fun DependencyHandler.tedPermission() {
    implementation(Dependencies.TedPermissionNormal)
    implementation(Dependencies.TedPermissionCoroutine)
}

fun DependencyHandler.hilt() {
    implementation(Dependencies.hiltAndroid)
    kapt(Dependencies.hiltCompiler)
}

fun DependencyHandler.feature_analyze() {
    implementation(project(":feature:analyze"))
}

fun DependencyHandler.feature_home() {
    implementation(project(":feature:home"))
}

fun DependencyHandler.feature_setting() {
    implementation(project(":feature:setting"))
}

fun DependencyHandler.feature_statistic() {
    implementation(project(":feature:statistic"))
}

fun DependencyHandler.core_common() {
    implementation(project(":core:common"))
}

fun DependencyHandler.core_data() {
    implementation(project(":core:data"))
}

fun DependencyHandler.core_common_ui() {
    implementation(project(":core:common-ui"))
}

fun DependencyHandler.core_domain() {
    implementation(project(":core:domain"))
}

fun DependencyHandler.core_model() {
    implementation(project(":core:model"))
}

