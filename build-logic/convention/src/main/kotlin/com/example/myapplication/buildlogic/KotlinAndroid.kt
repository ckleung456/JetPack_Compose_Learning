package com.example.myapplication.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

internal object AndroidConfig {
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36
    const val MIN_SDK = 24
    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JvmTarget.JVM_17
}

/** Shared Android + Kotlin settings for application and library modules. */
internal fun Project.configureKotlinAndroid(commonExtension: CommonExtension<*, *, *, *, *, *>) {
    commonExtension.apply {
        compileSdk = AndroidConfig.COMPILE_SDK

        defaultConfig {
            minSdk = AndroidConfig.MIN_SDK
            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }

        compileOptions {
            sourceCompatibility = AndroidConfig.JAVA_VERSION
            targetCompatibility = AndroidConfig.JAVA_VERSION
        }

        testOptions {
            // android.util.Log and friends become no-ops in JVM unit tests.
            unitTests.isReturnDefaultValues = true
        }
    }

    extensions.configure<KotlinAndroidProjectExtension> {
        compilerOptions {
            jvmTarget.set(AndroidConfig.JVM_TARGET)
            // Stable replacement for -XXLanguage:+PropertyParamAnnotationDefaultTargetMode: an annotation on a
            // constructor `val` (e.g. @StringRes) applies to both the parameter and the property.
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }

    // Test dependencies every module uses.
    addDependency("testImplementation", "junit")
    addDependency("androidTestImplementation", "androidx-junit")
    addDependency("androidTestImplementation", "androidx-espresso-core")
}
