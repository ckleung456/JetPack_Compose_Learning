plugins {
    alias(libs.plugins.myapp.android.library)
}

android {
    namespace = "com.example.domain"
}

// Layer 0: depends on no other module.
