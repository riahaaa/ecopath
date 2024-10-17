package edu.sungshin.ecopath

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    lateinit var buttonSignUp : Button
    lateinit var editTextEmail : EditText
    lateinit var editTextConfirmPassword : EditText
    lateinit var editTextPassword : EditText
    lateinit var editTextUsername : EditText
    lateinit var buttonEmailVerification: Button
    private var emailVerification: Boolean = false

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = FirebaseAuth.getInstance()

        buttonSignUp = findViewById(R.id.buttonSignUp)
        buttonSignUp = findViewById(R.id.buttonSignUp)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextUsername = findViewById(R.id.editTextUsername)
        buttonEmailVerification = findViewById(R.id.buttonEmailVerification)

        buttonSignUp.setOnClickListener {
            SignUpUser()
        }

        buttonEmailVerification.setOnClickListener {
            sendEmailVerification()
        }
    }

    private fun SignUpUser() {
        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()
        val confirmedPassword = editTextConfirmPassword.text.toString().trim()
        val username = editTextUsername.text.toString().trim()

        if (username.isEmpty()) {
            editTextUsername.error = "아이디를 입력해주세요"
            editTextUsername.requestFocus()
            return
        }
        if (email.isEmpty()) {
            editTextEmail.error = "이메일을 입력하세요"
            editTextEmail.requestFocus()
            return
        }
        if (password.isEmpty()) {
            editTextPassword.error = "비밀번호가 입력되지 않았습니다"
            editTextPassword.requestFocus()
            return
        }
        if (confirmedPassword.isEmpty()) {
            editTextConfirmPassword.error = "비밀번호를 확인해주세요"
            editTextConfirmPassword.requestFocus()
            return
        }
        if (password != confirmedPassword) {
            editTextConfirmPassword.error = "비밀번호가 맞지 않습니다"
            editTextConfirmPassword.requestFocus()
            return
        }
        if (password.length < 6) {
            editTextPassword.error = "비밀번호는 6자리 이상으로 입력하세요"
            editTextPassword.requestFocus()
            return
        }
        if(emailVerification){
            Toast.makeText(this, "이메일을 인증해주세요", Toast.LENGTH_SHORT).show()
            buttonEmailVerification.requestFocus()
            return
        }

        // 아이디 중복 체크
        checkUsernameAvailability(username) { isAvailable ->
            if (isAvailable) {
                mAuth.createUserWithEmailAndPassword(username, password).addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // Firestore에 사용자 정보 저장
                        val user = mAuth.currentUser
                        val userId = user?.uid
                        if (userId != null) {
                            val db = FirebaseFirestore.getInstance()
                            val userMap = hashMapOf(
                                "username" to username,
                                "email" to email
                            )
                            db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "회원가입을 성공했습니다", Toast.LENGTH_LONG).show()
                                    Intent(this, LoginActivity::class.java).also { startActivity(it) }
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "회원가입에 실패했습니다", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "회원가입에 실패했습니다: 오류 발생", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                editTextUsername.error = "중복된 아이디입니다"
                editTextUsername.requestFocus()
            }
        }
    }

    // 아이디 중복 확인 함수
    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(true)  // 사용 가능한 아이디
                } else {
                    Toast.makeText(this, "이미 존재하는 아이디 입니다", Toast.LENGTH_SHORT).show()
                    callback(false)  // 이미 존재하는 아이디
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "중복된 아이디 확인 실패", Toast.LENGTH_SHORT).show()
                callback(false)  // 중복으로 처리
            }
    }

    // 이메일 인증 함수
    private fun sendEmailVerification() {
        val user = mAuth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "인증 이메일이 전송되었습니다", Toast.LENGTH_SHORT).show()
                    emailVerification = true
                } else {
                    Toast.makeText(this, "인증 이메일 전송 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
