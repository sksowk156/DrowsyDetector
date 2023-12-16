//import ProjectConfig.compileSdk
//import ProjectConfig.minSdk
//import org.gradle.api.JavaVersion
//import org.gradle.api.Plugin
//import org.gradle.api.Project
//import com.android.build.gradle.LibraryExtension
//
//class MainGradlePlugin : Plugin<Project> {
//
//    override fun apply(project: Project) {
//        applyPlugins(project)
//        setProjectConfig(project)
//    }
//
//    private fun applyPlugins(project: Project) {
//        project.apply {
//            plugin("android-library")
//            plugin("kotlin-android")
//            plugin("androidx.navigation.safeargs.kotlin")
//            plugin("com.google.dagger.hilt.android")
//            plugin("kotlin-kapt")
//        }
//    }
//
//    private fun setProjectConfig(project: Project) {
//        project.android().apply {
//            compileSdk = ProjectConfig.compileSdk
//
//            defaultConfig {
//                minSdk = ProjectConfig.minSdk
//                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
//            }
//            buildTypes{
//                release {
//                    // Enables code shrinking, obfuscation, and optimization for only
//                    // your project's release build type.
//                    isMinifyEnabled = true
//                    // Includes the default ProGuard rules files that are packaged with
//                    // the Android Gradle plugin. To learn more, go to the section about
//                    // R8 configuration files.
//                    proguardFiles(
//                        getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
//                    )
//                }
//            }
//
//            compileOptions {
//                sourceCompatibility = JavaVersion.VERSION_18
//                targetCompatibility = JavaVersion.VERSION_18
//            }
//
//            buildFeatures {
//                viewBinding = true
//                dataBinding = true
//            }
//        }
//    }
//
//    private fun Project.android(): LibraryExtension {
//        return extensions.getByType(LibraryExtension::class.java)
//    }
//}