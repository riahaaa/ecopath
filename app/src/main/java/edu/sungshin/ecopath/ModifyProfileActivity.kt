package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ModifyProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_profile)

        // TextView 클릭 리스너 설정
        val modifyProfileButton = findViewById<TextView>(R.id.modifyProfile)
        modifyProfileButton.setOnClickListener {
            Toast.makeText(this, "회원정보 수정 클릭됨", Toast.LENGTH_SHORT).show()
            // 회원정보 수정 로직 추가
        }
    }
}
