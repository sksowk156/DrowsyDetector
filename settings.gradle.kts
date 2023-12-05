pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://www.jitpack.io") }
    }
}
rootProject.name = "DrowsyDetector"
include(":app")
include(":feature:home")
include(":feature:setting")
include(":feature:analyze")
include(":core:common")
include(":core:data")
include(":core:domain")
include(":core:network")
include(":core:database")
include(":core:model")
include(":feature:statistic")
include(":core:datastore")
include(":core:common-ui")
