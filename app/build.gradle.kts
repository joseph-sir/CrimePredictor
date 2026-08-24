plugins {
    id("com.android.application")
}

android {
    namespace = "com.edu.crimepredictor"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.edu.crimepredictor"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(libs.androidx.core.ktx.v1131)
    implementation(libs.androidx.appcompat.v170)
    implementation(libs.material.v1120)
    implementation(libs.androidx.constraintlayout.v220)

    // Retrofit
    implementation(libs.retrofit.v2110)
    implementation(libs.converter.gson.v2110)
    implementation(libs.logging.interceptor)

    // Gson
    implementation(libs.gson.v2110)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v121)
    androidTestImplementation(libs.androidx.espresso.core.v361)

        // ... other dependencies ...

        implementation(libs.logging.interceptor.v490)
        implementation(libs.okhttp)

        // Retrofit 2.9.0 works fine with OkHttp 4.9.0
        implementation(libs.retrofit.v290)
        implementation(libs.converter.gson.v290)

        // ... other dependencies ...

}