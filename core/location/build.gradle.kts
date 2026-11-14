plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.example.location"
}

dependencies {
    implementation(project(":core:domain"))
    implementation(project(":core:shared-lib"))

    implementation(libs.play.services.location)
    implementation(libs.androidx.coroutines.core)
    implementation(libs.androidx.coroutines.play.services)
}
