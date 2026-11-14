import com.example.myapplication.buildlogic.addDependency
import com.example.myapplication.buildlogic.applyPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.project

/**
 * A feature API module (`:feature-<name>-api`): the small contract one feature shares with others, such as
 * domain models, interfaces, or Navigation 3 route keys. It is a plain Android library with
 * kotlinx.serialization and `:core:domain`, and none of the feature's Compose/Hilt/network setup.
 * By the module rules it may depend only on `:core` modules, so it stays light and can't create cycles.
 */
class AndroidFeatureApiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("myapp-android-library")
        applyPlugin("jetbrains-kotlin-serialization")

        dependencies.add("api", project(":core:domain"))
        addDependency("api", "kotlinx-serialization-core")
    }
}
