buildscript {
    dependencies {
        classpath(libs.android.shortcut.gradle)
        classpath("com.google.gms:google-services:4.4.0") // Add Google Services plugin
    }
}

plugins {
    alias(kotlinx.plugins.serialization) apply false
    alias(libs.plugins.aboutLibraries) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.moko) apply false
    alias(libs.plugins.sqldelight) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

subprojects {
    afterEvaluate {
        if (plugins.hasPlugin("com.android.application") || plugins.hasPlugin("com.android.library")) {
            // Apply Google Services plugin
            apply(plugin = "com.google.gms.google-services")

            dependencies {
                // Import the Firebase BoM
                add("implementation", platform("com.google.firebase:firebase-bom:32.7.0"))
                
                // Add the dependencies for Firebase products you want to use
                // For example, Firebase Analytics
                add("implementation", "com.google.firebase:firebase-analytics-ktx")
                // Add other Firebase dependencies as needed
                // add("implementation", "com.google.firebase:firebase-auth-ktx")
                // add("implementation", "com.google.firebase:firebase-firestore-ktx")
            }
        }
    }
}
