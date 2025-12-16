pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Gradle 8.7 supports running on JDK 17 through 21. Guard against
// unsupported local JVMs (for example 24+) that break synchronization
// with a clear message instead of a cryptic compatibility error.
val minSupportedJava = JavaVersion.VERSION_17
val maxSupportedJava = JavaVersion.VERSION_21
val currentJava = JavaVersion.current()
if (currentJava < minSupportedJava || currentJava > maxSupportedJava) {
    error(
        "Current Gradle JVM $currentJava is not supported. " +
            "Use JDK 17–21 (Android Studio's embedded JDK 21 is recommended)."
    )
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EURestoAndroid"
include(":app")
