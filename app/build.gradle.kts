plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

android {
    namespace = "io.github.tommygeenexus.exifinterfaceextended"
    compileSdk = 35

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")
    implementation("androidx.annotation:annotation:1.9.1")
    androidTestImplementation("com.google.truth:truth:1.4.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
}
