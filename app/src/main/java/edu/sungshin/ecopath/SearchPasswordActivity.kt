package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SearchPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_password)

        val findPasswordCheckbox: CheckBox = findViewById(R.id.findPasswordCheckbox)


        val nextButton: Button = findViewById(R.id.nextButton)
        nextButton.setOnClickListener {
            if (findPasswordCheckbox.isChecked) {
                // 비밀번호 찾기 페이지로 이동
                val intent = Intent(this, ForgotPasswordActivity::class.java)
                startActivity(intent)

            } else {
                Toast.makeText(this, "비밀번호 찾기 또는 아이디 찾기를 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
