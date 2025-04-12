plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.boozy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.boozy"
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
}

dependencies {
    implementation(libs.appcompat)
    //implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.cardview:cardview:1.0.0")

    implementation ("com.google.android.material:material:1.11.0")

    implementation ("com.google.android.gms:play-services-location:21.0.1")

   // implementation ("com.google.android.gms:play-services-maps:18.1.0")

    implementation ("androidx.core:core-ktx:1.12.0")


    //okHttp
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    //implementation ("com.google.maps.android:android-maps-utils:2.3.0")

    implementation ("com.stripe:stripe-android:20.33.0")

    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.27")

    implementation ("com.google.code.gson:gson:2.10.1")


    //Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")




    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
