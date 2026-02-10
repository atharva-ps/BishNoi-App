pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://justbaat-apps-b325d.web.app")}
        maven { url = uri("https://artifact.bytedance.com/repository/pangle/") }
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

rootProject.name = "BishNoi"
include(":app")
