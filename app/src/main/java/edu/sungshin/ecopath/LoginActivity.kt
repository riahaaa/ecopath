package edu.sungshin.ecopath

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    // FirebaseAuth 객체 선언
    private lateinit var auth: FirebaseAuth

    // 이메일과 비밀번호 입력 필드 선언
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // FirebaseAuth 객체 초기화
        auth = FirebaseAuth.getInstance()

        // 이메일과 비밀번호 입력 필드 초기화
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)

        // 로그인 버튼 설정
        val loginButton: Button = findViewById(R.id.loginButton)
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 회원가입 버튼 설정
        val signupButton: Button = findViewById(R.id.registerButton)
        signupButton.setOnClickListener {
            // SignupActivity로 이동
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // 비밀번호 찾기 버튼 설정
        val searchPasswordButton: Button = findViewById(R.id.searchpasswordButton)
        searchPasswordButton.setOnClickListener {
            val intent = Intent(this, SearchPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // Firebase 이메일/비밀번호 로그인 처리
    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    Toast.makeText(this, "로그인 성공", Toast.LENGTH_SHORT).show()

                    // 현재 로그인한 사용자의 UID 가져오기
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {

                        // 홈 화면으로 이동
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "사용자 정보를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // 로그인 실패
                    val errorMessage = when (task.exception?.message) {
                        "The email address is badly formatted." -> "잘못된 이메일 형식입니다."
                        "There is no user record corresponding to this identifier. The user may have been deleted." -> "존재하지 않는 이메일입니다."
                        "The password is invalid or the user does not have a password." -> "비밀번호가 잘못되었습니다."
                        else -> "로그인 실패: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()

                }

            }
    }
}
