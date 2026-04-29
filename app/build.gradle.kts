plugins {
    id("com.android.library")
    id("com.vanniktech.maven.publish") version "0.36.0"
}

android {
    namespace = "io.github.tommygeenexus.exifinterfaceextended"
    compileSdk = 37

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    api("org.jspecify:jspecify:1.0.0")
    implementation("androidx.annotation:annotation:1.10.0")
    androidTestImplementation("com.google.truth:truth:1.4.5")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test:rules:1.7.0")
}
