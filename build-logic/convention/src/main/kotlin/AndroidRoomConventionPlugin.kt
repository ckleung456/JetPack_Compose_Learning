import com.example.myapplication.buildlogic.addDependency
import com.example.myapplication.buildlogic.applyPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

/** Room runtime + KSP compiler. Apply after `myapp.android.application` or `myapp.android.library`. */
class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("ksp")

        addDependency("implementation", "androidx-room-runtime")
        addDependency("implementation", "androidx-room-ktx")
        addDependency("ksp", "androidx-room-compiler")
    }
}
