plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.jetbrains.kotlin.android)
    
}

android {
    namespace = "com.example.ecohelper"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.ecohelper"
        minSdk = 23
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        mlModelBinding = true
    }

    aaptOptions {
        noCompress("tflite")
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.core.ktx)
    implementation(libs.tensorflow.lite.gpu)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)


    implementation("androidx.camera:camera-view:1.3.0-alpha06")
    implementation("com.github.jose-jhr:Library-CameraX:1.0.8")
    implementation("org.tensorflow:tensorflow-lite-support:+")
    implementation("org.tensorflow:tensorflow-lite:+")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")
}
