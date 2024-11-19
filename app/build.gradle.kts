plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.musicapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.musicapp"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += listOf("META-INF/NOTICE.md", "META-INF/LICENSE.md")
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        viewBinding = true
    }
}
dependencies {
    // Android Core Dependencies
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")

    // Architecture Components
    val lifecycleVersion = "2.7.0"
    implementation("androidx.lifecycle:lifecycle-viewmodel:$lifecycleVersion")
    implementation("androidx.lifecycle:lifecycle-livedata:$lifecycleVersion")

    // Navigation Components
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // Room Database
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-rxjava2:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // Network & Data Handling
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")

    // Firebase & Google Services
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    // Image Loading & Processing
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("jp.wasabeef:blurry:4.0.1")

    // Utils & Misc
    implementation(libs.androidthings)
    implementation(libs.android.mail)
    implementation(libs.android.activation)
    implementation("com.intuit.sdp:sdp-android:1.0.6")

    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)                            
}