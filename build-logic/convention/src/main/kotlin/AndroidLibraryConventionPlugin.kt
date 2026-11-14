import com.android.build.api.dsl.LibraryExtension
import com.example.myapplication.buildlogic.applyPlugin
import com.example.myapplication.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("android-library")
        applyPlugin("kotlin-android")
        applyPlugin("myapp-module-rules")

        extensions.configure<LibraryExtension> {
            configureKotlinAndroid(this)
            defaultConfig.consumerProguardFiles("consumer-rules.pro")
        }
    }
}
