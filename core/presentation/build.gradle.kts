plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.compose)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

android {
    namespace = "com.example.presentation"
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":core:shared-lib"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)

    // nav 3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    // image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.network)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.compose.ui.test.junit4)
}
