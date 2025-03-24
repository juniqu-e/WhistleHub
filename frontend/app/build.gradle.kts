plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.dagger.hilt)

}

android {
    namespace = "com.whistlehub"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.whistlehub"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags("-std=c++2a")
                arguments("-DANDROID_STL=c++_shared")
            }
        }
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
        compose = true
        prefab = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.material.icons.extended)     // Material Icons Extended (Compose BOM 사용 시 버전 관리는 BOM에 포함)
    implementation(libs.retrofit)     // Retrofit
    implementation(libs.java.jwt)
    implementation(libs.converter.gson)     // Retrofit
    implementation(libs.okhttp.logging.interceptor) // Okhttp
    ksp(libs.androidx.room.compiler)     // Room
    implementation(libs.androidx.room.runtime)     // Room
    implementation(libs.androidx.room.ktx)     // Room
    implementation(libs.androidx.datastore.preferences)     // DataStore
    implementation(libs.androidx.navigation.compose)     // Navigation Compose
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.oboe)     // Oboe (NDK/CMake 설정 필요)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)

    implementation(libs.androidx.media3.ui)
    implementation(libs.lifecycle.viewmodel.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // Superpowered: 직접 SDK 다운로드 및 통합 (Gradle 의존성으로 추가되지 않음)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}