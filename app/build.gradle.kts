import org.jetbrains.kotlin.gradle.utils.`is`

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")

}

android {
    namespace = "com.adilmulimani.todoApp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.adilmulimani.todoApp"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding.isEnabled=true

    buildFeatures {
        dataBinding = true
    }


}

dependencies {

    implementation("com.android.volley:volley:1.2.1")
    implementation ("com.github.happysingh23828:HappyTimer:1.0.1")
    implementation ("io.github.ShawnLin013:number-picker:2.4.13")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.databinding:databinding-ktx:8.3.0")
    implementation ("com.github.mckrpk:AnimatedProgressBar:0.1.0")
    implementation ("com.github.qamarelsafadi:CurvedBottomNavigation:0.1.3")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.github.GrenderG:Toasty:1.5.2")
    implementation("androidx.core:core-splashscreen:1.0.1")
}