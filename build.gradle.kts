buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.10.1")
    }
}

tasks.register("clean", Delete::class.java) {
    delete(layout.buildDirectory)
}
