pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

// Gradle 8.7 supports Gradle JVM up to JDK 21. Guard against newer
// locally-configured JDKs (for example 24+) that break synchronization
// with a clear message instead of a cryptic compatibility error.
val maxSupportedJava = JavaVersion.VERSION_21
if (JavaVersion.current() > maxSupportedJava) {
    error(
        "Current Gradle JVM ${JavaVersion.current()} is not supported. " +
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
