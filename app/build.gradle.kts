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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.androidthings)
//    implementation(files("../spotify-app-remote-release-0.8.0.aar"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    //gson
    implementation("com.google.code.gson:gson:2.10.1")
    //
    val room_version = "2.6.1"
    //room & RxJava
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-rxjava2:$room_version")
    //recycle
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    //RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.8")
    //LifeCycle & ViewModel & Model & Live Data
    val lifecycle_version = "2.7.0"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:lifecycle_version")
    // LiveData
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    //CardView
    implementation("androidx.cardview:cardview:1.0.0")
    //firebase
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.android.gms:play-services-auth:21.1.1")

    implementation("com.intuit.sdp:sdp-android:1.0.6")

    //gif
    implementation("com.github.bumptech.glide:glide:4.11.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.11.0")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("de.hdodenhof:circleimageview:3.1.0")
    //mail
    implementation (libs.android.mail)
    implementation (libs.android.activation)
    //blur
    implementation ("jp.wasabeef:blurry:3.0.0")

}