package edu.sungshin.ecopath

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseApp
import edu.sungshin.ecopath.R

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        Handler(Looper.getMainLooper()).postDelayed({
            // FirebaseAuth 인스턴스 가져오기
            val auth = FirebaseAuth.getInstance()

            // 명시적으로 로그아웃 처리 (세션 초기화)
            auth.signOut()

            // 로그인 상태에 따라 화면 전환
            val intent = if (auth.currentUser != null) {
                // 사용자가 로그인되어 있으면 HomeActivity로 이동
                Intent(this, HomeActivity::class.java)
            } else {
                // 로그인되지 않은 경우 LoginActivity로 이동
                Intent(this, LoginActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000) // 2초 대기

    }
}