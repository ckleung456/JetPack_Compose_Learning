plugins {
    alias(libs.plugins.myapp.android.application)
    alias(libs.plugins.myapp.android.compose)
    alias(libs.plugins.myapp.android.hilt)
    alias(libs.plugins.myapp.android.room)
}

android {
    namespace = "com.example.myapplication"

    defaultConfig {
        applicationId = "com.example.myapplication"
        versionCode = 1
        versionName = "1.0"
    }
}

ksp {
    // Exported schemas give Room a history to write and test Migrations against.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(project(":core:location"))
    implementation(project(":core:presentation"))
    implementation(project(":feature-country"))
    implementation(project(":feature-doordash"))
    implementation(project(":feature-business"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // nav 3
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
