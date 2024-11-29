import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
}

// local.properties 파일을 읽기
val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

// KAKAO_API_KEY 값을 가져오기
val KakaoApiKey: String = localProperties.getProperty("KAKAO_API_KEY") ?: ""


android {
    namespace = "edu.sungshin.ecopath"
    compileSdk = 35

    defaultConfig {
        applicationId = "edu.sungshin.ecopath"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig에 API 키 추가
        buildConfigField("String", "KAKAO_API_KEY", "\"${KakaoApiKey}\"")
    }

    buildFeatures {
        buildConfig = true
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

    // Firebase BoM (Bill of Materials) 설정
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Firebase UI (Firestore RecyclerView용)
    implementation("com.firebaseui:firebase-ui-firestore:8.0.0")

    // Glide (이미지 로딩 라이브러리)
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    // Kakao Services
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // 테스트 관련 라이브러리
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Kakao Services
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    implementation("androidx.viewpager2:viewpager2:1.1.0")


    // 테스트 관련
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
