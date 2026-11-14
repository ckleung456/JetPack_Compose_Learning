plugins {
    alias(libs.plugins.myapp.android.feature)
}

android {
    namespace = "com.example.feature.business"
}

dependencies {
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.kotlinx.immutable)
}
