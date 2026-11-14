import com.android.build.api.dsl.LibraryExtension
import com.example.myapplication.buildlogic.addDependency
import com.example.myapplication.buildlogic.applyPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.project

/**
 * A feature module: Android library + Compose + Hilt + Navigation 3 routes (kotlinx.serialization) +
 * secrets from local.properties exposed as BuildConfig fields, plus the core modules and test libraries
 * every feature uses. Features may depend only on :core modules (enforced by `myapp.module.rules`).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("myapp-android-library")
        applyPlugin("myapp-android-compose")
        applyPlugin("myapp-android-hilt")
        applyPlugin("jetbrains-kotlin-serialization")
        applyPlugin("google-secrets-gradle")

        extensions.configure<LibraryExtension> {
            buildFeatures.buildConfig = true
        }

        dependencies.apply {
            add("implementation", project(":core:presentation"))
            add("implementation", project(":core:network")) // also exposes :core:domain and Retrofit via api
            add("implementation", project(":core:shared-lib"))
        }

        listOf(
            "androidx-core-ktx",
            "androidx-lifecycle-runtime-compose",
            "androidx-compose-ui",
            "androidx-compose-material3",
            "androidx-compose-material-icons",
            "androidx-navigation3-runtime",
            "androidx-hilt-lifecycle-viewmodel-compose",
            "kotlinx-serialization-core",
            "okhttp3-logging",
        ).forEach { addDependency("implementation", it) }

        listOf(
            "kotlinx-coroutines-test",
            "turbine",
            "robolectric",
            "androidx-junit",
            "androidx-compose-ui-test-junit4",
        ).forEach { addDependency("testImplementation", it) }

        addDependency("androidTestImplementation", "androidx-compose-ui-test-junit4")
    }
}
