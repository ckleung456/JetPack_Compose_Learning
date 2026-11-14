plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.compose)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.example.shared.lib"
}

dependencies {
    api(project(":core:domain"))
    api(libs.androidx.coroutines.core) // Flow is part of FlowUseCase / ObserveAsEvents public API
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
