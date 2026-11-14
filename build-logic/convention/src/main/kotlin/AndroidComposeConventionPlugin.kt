import com.android.build.api.dsl.CommonExtension
import com.example.myapplication.buildlogic.addDependency
import com.example.myapplication.buildlogic.addPlatform
import com.example.myapplication.buildlogic.applyPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Compose compiler + BOM. Apply after `myapp.android.application` or `myapp.android.library`. */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("kotlin-compose")

        val android = extensions.getByName("android") as CommonExtension<*, *, *, *, *, *>
        android.apply {
            buildFeatures.compose = true
            // Robolectric Compose UI tests resolve real string resources.
            testOptions.unitTests.isIncludeAndroidResources = true
        }

        addPlatform("implementation", "androidx-compose-bom")
        addPlatform("testImplementation", "androidx-compose-bom")
        addPlatform("androidTestImplementation", "androidx-compose-bom")
        // Adds the ComponentActivity that createComposeRule() launches.
        addDependency("debugImplementation", "androidx-compose-ui-test-manifest")
    }
}
