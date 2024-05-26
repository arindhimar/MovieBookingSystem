plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.combined_loginregister"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.combined_loginregister"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.database.ktx)
    implementation(libs.play.services.auth)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.recyclerview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.google.android.material:material:1.5.0")
    implementation ("org.imaginativeworld.oopsnointernet:oopsnointernet:2.0.0")
    implementation ("com.squareup.okhttp3:okhttp:4.9.1")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("com.furkanakdemir:surroundcardview:1.0.6")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.22")
    implementation ("com.github.ArjunGupta08:Horizontal-CalendarDate-With-Click-listener:1.1.0")
<<<<<<< HEAD





=======
    implementation ("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation ("nl.joery.animatedbottombar:library:1.1.0")
    implementation("io.github.androidpoet:dropdown:1.1.2")
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("androidx.browser:browser:1.3.0")
    implementation ("com.google.android.gms:play-services-auth:20.1.0")
    implementation("com.google.android.play:integrity:1.3.0")
    implementation("com.razorpay:checkout:1.6.33")
>>>>>>> 7249331f80923bcd6c1ebf26e26936377b1a2884




}