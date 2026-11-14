import com.android.build.api.dsl.ApplicationExtension
import com.example.myapplication.buildlogic.AndroidConfig
import com.example.myapplication.buildlogic.applyPlugin
import com.example.myapplication.buildlogic.configureKotlinAndroid
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) { configure() }
    }

    private fun Project.configure() {
        applyPlugin("android-application")
        applyPlugin("kotlin-android")
        applyPlugin("myapp-module-rules")

        extensions.configure<ApplicationExtension> {
            configureKotlinAndroid(this)
            defaultConfig.targetSdk = AndroidConfig.TARGET_SDK
            buildTypes {
                release {
                    isMinifyEnabled = false
                    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                }
            }
        }
    }
}
