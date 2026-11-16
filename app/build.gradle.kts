plugins { id("com.android.application") }

android {
    namespace = "com.rabbi.stream"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.rabbi.stream"
        minSdk = 23
        targetSdk = 35
        versionCode = 3
        versionName = "0.3.0"
    }
}

dependencies {
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
}
