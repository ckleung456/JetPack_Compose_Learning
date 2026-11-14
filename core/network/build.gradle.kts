plugins {
    alias(libs.plugins.myapp.android.library)
    alias(libs.plugins.myapp.android.hilt)
}

android {
    namespace = "com.example.network"
}

dependencies {
    api(project(":core:domain"))

    // Feature API interfaces and DTOs use Retrofit/Gson types directly.
    api(libs.retrofit)
    api(libs.retrofit.converter)
    implementation(libs.androidx.coroutines.core)

    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}
