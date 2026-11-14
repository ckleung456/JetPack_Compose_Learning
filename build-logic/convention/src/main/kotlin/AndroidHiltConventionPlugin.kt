import com.example.myapplication.buildlogic.addDependency
import com.example.myapplication.buildlogic.applyPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Hilt + KSP. Apply after `myapp.android.application` or `myapp.android.library`. */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("ksp")
        applyPlugin("hilt")

        addDependency("implementation", "hilt-android")
        addDependency("ksp", "hilt-compiler")
    }
}
