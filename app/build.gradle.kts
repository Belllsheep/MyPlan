plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.kotlin.kapt) // 使用版本目录声明 kapt，
                                    // 但是要在libs.versions.toml中增加一些定义内容
}

android {
    viewBinding {
        enable = true
    }
    namespace = "com.example.app9"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app9"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //添加Room依赖，注意确保在 build.gradle.kts 文件中启用了 kapt
    // Room 核心库
    implementation("androidx.room:room-runtime:2.6.1")
    // Room 注解处理器（Kotlin 使用 kapt）
    kapt("androidx.room:room-compiler:2.6.1")
    // Room 对 Kotlin 协程的支持
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.gson) // 使用版本目录中的 Gson 依赖

}