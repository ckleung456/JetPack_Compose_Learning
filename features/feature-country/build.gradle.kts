plugins {
    alias(libs.plugins.myapp.android.feature)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.example.feature.country"
}

dependencies {
    implementation(libs.kotlinx.immutable)
}
