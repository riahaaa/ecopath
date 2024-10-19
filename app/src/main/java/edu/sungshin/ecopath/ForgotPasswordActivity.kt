package edu.sungshin.ecopath


import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
class ForgotPasswordActivity : AppCompatActivity(){

    private lateinit var mAuth: FirebaseAuth
    private lateinit var etUserId: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        mAuth = FirebaseAuth.getInstance()
        etUserId = findViewById(R.id.etUserId)
    }

    fun onFindPasswordClick(view: View) {
        val userId = etUserId.text.toString().trim()

        if (userId.isEmpty()) {
            Toast.makeText(this, "아이디를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase Auth를 이용해 아이디로 비밀번호 찾기
        mAuth.sendPasswordResetEmail(userId)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "비밀번호 리셋 이메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "이메일 전송에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
