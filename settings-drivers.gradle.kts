// driver-only settings：在 WSL/CI 中只构建驱动模块，避免配置 :app 时要求 Android SDK。
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "RemoteDBClientDrivers"
include(":driver-api", ":driver-mysql", ":driver-postgresql")
