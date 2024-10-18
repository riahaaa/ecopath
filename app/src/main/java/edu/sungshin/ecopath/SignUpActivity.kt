package edu.sungshin.ecopath

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseRef: DatabaseReference

    private lateinit var buttonSignUp: Button
    private lateinit var editTextEmail: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextUsername: EditText

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mAuth = FirebaseAuth.getInstance()
        mDatabaseRef = FirebaseDatabase.getInstance().reference.child("ecopath")

        buttonSignUp = findViewById(R.id.buttonSignUp)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextUsername = findViewById(R.id.editTextUsername)

        buttonSignUp.setOnClickListener {
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmedPassword = editTextConfirmPassword.text.toString().trim()
            val username = editTextUsername.text.toString().trim()

            if (!isInputValid(
                    username,
                    email,
                    password,
                    confirmedPassword
                )
            ) return@setOnClickListener

            // 아이디 중복 체크
            checkUsernameAvailability(username) { isAvailable ->
                if (isAvailable) {
                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, OnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val firebaseUser: FirebaseUser? = mAuth.currentUser
                                val account = UserAccount()
                                if (firebaseUser != null) {
                                    val account = UserAccount().apply {
                                        this.email = firebaseUser.email
                                        this.ID = username
                                        this.password = password
                                        this.IdToken = firebaseUser.uid
                                    }
                                    mDatabaseRef.child("UserAccount").child(firebaseUser.uid)
                                        .setValue(account)

                                    Toast.makeText(this, "회원가입이 완료됐습니다", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                    finish()

                                } else {
                                    Toast.makeText(this, "회원가입을 실패했습니다.", Toast.LENGTH_SHORT).show()

                                }

                            }else {
                                // 오류 처리
                                Toast.makeText(this, task.exception?.message ?: "회원가입을 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        })
                } else {
                    editTextUsername.error = "중복된 아이디입니다"
                    editTextUsername.requestFocus()
                }
            }
        }
    }


    private fun isInputValid(
        username: String,
        email: String,
        password: String,
        confirmedPassword: String
    ): Boolean {
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.error = "유효한 이메일을 입력하세요"
            editTextEmail.requestFocus()
            return false
        }
        if (username.isEmpty()) {
            editTextUsername.error = "아이디를 입력해주세요"
            editTextUsername.requestFocus()
            return false
        }
        if (email.isEmpty()) {
            editTextEmail.error = "이메일을 입력하세요"
            editTextEmail.requestFocus()
            return false
        }
        if (password.isEmpty()) {
            editTextPassword.error = "비밀번호를 입력하세요"
            editTextPassword.requestFocus()
            return false
        }
        if (confirmedPassword.isEmpty()) {
            editTextConfirmPassword.error = "비밀번호를 확인해주세요"
            editTextConfirmPassword.requestFocus()
            return false
        }
        if (password != confirmedPassword) {
            editTextConfirmPassword.error = "비밀번호가 일치하지 않습니다"
            editTextConfirmPassword.requestFocus()
            return false
        }
        if (password.length < 6) {
            editTextPassword.error = "비밀번호는 6자리 이상이어야 합니다"
            editTextPassword.requestFocus()
            return false
        }
        return true
    }

    // 아이디 중복 확인 함수
    private fun checkUsernameAvailability(username: String, callback: (Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.isEmpty)  // 사용 가능한 아이디
            }
            .addOnFailureListener {
                Toast.makeText(this, "중복 검사 실패", Toast.LENGTH_SHORT).show()
                callback(false)  // 중복으로 처리
            }
    }

}
