plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.worrie.simplenoteapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.worrie.simplenoteapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 2
        versionName = "1.7"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    implementation("com.google.code.gson:gson:2.10.1")
}
