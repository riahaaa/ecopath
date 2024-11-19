plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "edu.sungshin.ecopath"
    compileSdk = 34

    defaultConfig {
        applicationId = "edu.sungshin.ecopath"
        minSdk = 21
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.2.1")


    // Firebase BoM (Bill of Materials) 사용
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))

    // Firebase 서비스 추가
    implementation("com.google.firebase:firebase-auth-ktx")            // Firebase Authentication
    implementation("com.google.firebase:firebase-firestore-ktx")       // Firebase Firestore
    implementation("com.google.firebase:firebase-database-ktx")        // Firebase Realtime Database
    implementation("com.google.firebase:firebase-storage-ktx")         // Firebase Storage
    implementation("com.google.firebase:firebase-analytics-ktx")       // Firebase Analytics

    // Glide (이미지 로딩 라이브러리)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("com.google.firebase:firebase-storage:21.0.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

    // Firebase UI (Firestore RecyclerView용)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.0")

    // 테스트 라이브러리
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}





