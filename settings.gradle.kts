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
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "My Application"
include(":app")
include(":feature-country")
project(":feature-country").projectDir = file("features/feature-country")
include(":feature-doordash")
project(":feature-doordash").projectDir = file("features/feature-doordash")
include(":feature-business")
project(":feature-business").projectDir = file("features/feature-business")
include(":core:location")
include(":core:presentation")
include(":core:domain")
include(":core:shared-lib")
include(":core:network")
