plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.cs708_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.cs708_android"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //implementation(androidx.core:core:1.13.0)
    //implementation('com.github.amitshekhariitbhu.Fast-Android-Networking:android-networking:1.0.4')
    //implementation("com.github.amitshekhariitbhu.Fast-Android-Networking:android-networking:1.0.4")
    implementation("com.github.amitshekhariitbhu.Fast-Android-Networking:rx-android-networking:1.0.4")
    implementation("com.github.amitshekhariitbhu.Fast-Android-Networking:android-networking:1.0.2")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("com.google.android.material:material:1.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    testImplementation("junit:junit:4.+")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

}