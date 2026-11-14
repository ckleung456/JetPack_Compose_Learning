import com.example.myapplication.buildlogic.ModuleRules
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/** Fails the build as soon as a project dependency breaks [ModuleRules]. Applied by every module convention. */
class ModuleRulesConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val from = target.path
        target.configurations.configureEach {
            val configurationName = name
            dependencies.withType(ProjectDependency::class.java).configureEach {
                val to = path
                ModuleRules.violation(from = from, to = to)?.let { reason ->
                    throw GradleException(
                        "Module dependency rule broken: $from -> $to (in '$configurationName'): $reason"
                    )
                }
            }
        }
    }
}
