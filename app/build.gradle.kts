plugins {
    alias(libs.plugins.kotlin.android)
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.dokka") version "1.8.10"
}

android {
    namespace = "com.example.bar"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.bar"
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
    buildFeatures {
        viewBinding = true
    }

    // Добавьте этот блок для решения проблемы с META-INF
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/*.version"
            excludes += "/META-INF/io.netty.versions.properties"
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/*.properties"
        }
    }
}

dependencies {
    implementation(libs.androidx.material3.android)
    implementation(libs.firebase.appdistribution.gradle)
    implementation ("com.github.bumptech.glide:glide:4.16.0") // Обновленная версия Glide
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0") // Должен совпадать с версией Glide
    implementation(platform(libs.firebase.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.database)
    implementation(libs.androidx.activity)
    implementation(libs.firebase.auth.ktx)

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Для работы с сетью
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Для корутин
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}