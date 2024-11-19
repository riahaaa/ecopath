package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyPageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // 닫기 버튼 클릭 리스너 설정
        val closeButton = findViewById<TextView>(R.id.closeButton)
        closeButton.setOnClickListener {
            finish() // 현재 액티비티를 종료하여 이전 화면으로 돌아감
        }

        // 회원정보 수정 버튼 클릭 리스너 설정
        val modifyProfileButton = findViewById<TextView>(R.id.modifyProfile)
        modifyProfileButton.setOnClickListener {
            val intent = Intent(this, ModifyProfileActivity::class.java)
            startActivity(intent)
        }

        // 서비스 탈퇴 버튼 클릭 리스너 설정
        val cancelServiceButton = findViewById<TextView>(R.id.cancelService)
        cancelServiceButton.setOnClickListener {
            val intent = Intent(this, CancelServiceActivity::class.java)
            startActivity(intent)
        }

        // Post 버튼 클릭 리스너 설정
        val myContentButton = findViewById<TextView>(R.id.postListButton)
        myContentButton.setOnClickListener {
            val intent = Intent(this, PostListActivity::class.java)
            startActivity(intent)
        }
    }
}
