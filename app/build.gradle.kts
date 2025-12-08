plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.bytedance.lemon"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.bytedance.lemon"
        minSdk = 24
        targetSdk = 36
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("io.github.scwang90:refresh-layout-kernel:2.1.0")
// 核心库，必须
    implementation("io.github.scwang90:refresh-header-classics:2.1.0")
// 经典刷新头
    implementation("io.github.scwang90:refresh-footer-classics:2.1.0")

// 经典加载
    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")


    implementation("androidx.lifecycle:lifecycle-livedata:2.6.2")
    implementation("androidx.transition:transition:1.4.1")

    //增加网络加载图片的操作
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}