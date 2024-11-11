plugins {
    id("com.android.application") // Android 애플리케이션 플러그인 적용
    id("org.jetbrains.kotlin.android") // Kotlin Android 플러그인 적용
    id("com.google.gms.google-services") // Google 서비스 플러그인 적용
}

android {
    namespace = "edu.sungshin.ecopath" // 네임스페이스 설정
    compileSdk = 34 // 컴파일 SDK 버전 설정

    defaultConfig {
        applicationId = "edu.sungshin.ecopath" // 애플리케이션 ID
        minSdk = 21 // 최소 SDK 버전
        targetSdk = 34 // 타겟 SDK 버전
        versionCode = 1 // 버전 코드
        versionName = "1.0" // 버전 이름

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // 테스트 러너 설정
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 프로가드 활성화 여부
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // 자바 호환성
        targetCompatibility = JavaVersion.VERSION_1_8 // 자바 호환성
    }
    kotlinOptions {
        jvmTarget = "1.8" // Kotlin JVM 타겟
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1") // Android KTX 라이브러리
    implementation("androidx.appcompat:appcompat:1.7.0") // AppCompat 라이브러리
    implementation("com.google.android.material:material:1.12.0") // Material Components
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-database:21.0.0")
    implementation("com.google.firebase:firebase-auth:23.0.0") // ConstraintLayout

    testImplementation("junit:junit:4.13.2") // JUnit 테스트 라이브러리
    androidTestImplementation("androidx.test.ext:junit:1.2.1") // AndroidX 테스트 라이브러리
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1") // Espresso 테스트 라이브러리

    // Firebase BoM 가져오기
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-analytics") // Firebase Analytics 라이브러리
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation ("com.google.firebase:firebase-database-ktx:20.3.0")

    // Google Play Services 의존성
    implementation ("com.google.android.gms:play-services-places:18.0.1")
    implementation ("com.google.android.libraries.places:places-compat:2.7.0")
}