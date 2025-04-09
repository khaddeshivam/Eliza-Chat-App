// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("[https://dl.google.com/dl/android/maven2/") }
            maven { url = uri("[https://jitpack.io](https://jitpack.io)") }
        }
    dependencies {
        classpath("com.android.tools.build:gradle:8.3.0")
        classpath("com.google.gms:google-services:4.4.1") // Latest stable version
    }
}

plugins {
    id("com.android.application") version "8.3.0" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
