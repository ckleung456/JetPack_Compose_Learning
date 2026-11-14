import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "com.example.myapplication.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    // compileOnly: the real plugin classes come from the root build's classpath (`apply false` in /build.gradle.kts).
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)

    testImplementation(libs.junit)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = libs.plugins.myapp.android.application.get().pluginId
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = libs.plugins.myapp.android.library.get().pluginId
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = libs.plugins.myapp.android.compose.get().pluginId
            implementationClass = "AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = libs.plugins.myapp.android.hilt.get().pluginId
            implementationClass = "AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = libs.plugins.myapp.android.room.get().pluginId
            implementationClass = "AndroidRoomConventionPlugin"
        }
        register("androidFeature") {
            id = libs.plugins.myapp.android.feature.get().pluginId
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        register("androidFeatureApi") {
            id = libs.plugins.myapp.android.featureApi.get().pluginId
            implementationClass = "AndroidFeatureApiConventionPlugin"
        }
        register("moduleRules") {
            id = libs.plugins.myapp.module.rules.get().pluginId
            implementationClass = "ModuleRulesConventionPlugin"
        }
    }
}
