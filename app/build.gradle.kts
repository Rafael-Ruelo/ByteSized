plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.thesiswork"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.thesiswork"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}


    dependencies {
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        // Navigation Component
        implementation("androidx.navigation:navigation-fragment:2.7.6")
        implementation("androidx.navigation:navigation-ui:2.7.6")
        // Image loading
        implementation("com.github.bumptech.glide:glide:5.0.7")
        annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
        // ZIP file handling
        implementation("net.lingala.zip4j:zip4j:2.11.5")
    }
