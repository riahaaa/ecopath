package edu.sungshin.ecopath

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            Log.d("DEBUG", "SignUp button clicked")

            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val confirmedPassword = editTextConfirmPassword.text.toString().trim()
            val username = editTextUsername.text.toString().trim()

            if (!isInputValid(username, email, password, confirmedPassword)) {
                Log.d("DEBUG", "Input validation failed")
                return@setOnClickListener
            }

            // 회원가입: 사용자 생성
            Log.d("DEBUG", "Input validation passed, creating user")
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("DEBUG", "User created successfully")
                        val firebaseUser: FirebaseUser? = mAuth.currentUser
                        if (firebaseUser != null) {
                            val uid = firebaseUser.uid

                            // 인증 후 데이터베이스 접근 가능
                            checkUsernameAvailability(username) { isAvailable ->
                                if (isAvailable) {
                                    saveUserData(uid, email, password, username)
                                } else {
                                    Log.d("DEBUG", "Username is not available")
                                    editTextUsername.error = "중복된 아이디입니다"
                                    editTextUsername.requestFocus()
                                    firebaseUser.delete() // 중복된 경우 사용자 계정을 삭제
                                }
                            }
                        }
                    } else {
                        Log.e("DEBUG", "User creation failed: ${task.exception?.message}")
                        Toast.makeText(
                            this,
                            "회원가입 실패: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
        }

    }

    private fun saveUserData(uid: String, email: String, password: String, username: String) {
        val account = UserAccount(
            email = email,
            password = password,
            id = username,
            idToken = uid
        )
        mDatabaseRef.child("UserAccount").child(uid)
            .setValue(account)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("DEBUG", "User data saved successfully")
                    Toast.makeText(this, "회원가입이 완료됐습니다", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Log.e("DEBUG", "Failed to save user data: ${task.exception?.message}")
                    Toast.makeText(this, "회원정보 저장 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
        Log.d("DEBUG", "checkUsernameAvailability called with username: $username")
        mDatabaseRef.child("UserAccount")
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("DEBUG", "Firebase get() successful")
                var isAvailable = true
                for (child in snapshot.children) {
                    val existingUsername = child.child("id").getValue(String::class.java)
                    Log.d("DEBUG", "Checking existing username: $existingUsername")
                    if (existingUsername == username) {
                        isAvailable = false
                        break
                    }
                }
                callback(isAvailable)
            }
            .addOnFailureListener { exception ->
                Log.e("DEBUG", "Error checking username availability: ${exception.message}")
                Toast.makeText(this, "중복 검사 실패", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }
}