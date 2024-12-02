package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.EmailAuthProvider

class MemberActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var realtimeDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance()

        // UI 요소 초기화
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val checkEmailButton = findViewById<Button>(R.id.modifyEmailButton)
        val currentPasswordInput = findViewById<EditText>(R.id.currentPasswordInput)
        val newPasswordInput = findViewById<EditText>(R.id.newPasswordInput)
        val confirmNewPasswordInput = findViewById<EditText>(R.id.confirmNewPasswordInput)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        // 사용자 이메일 확인 버튼 클릭 이벤트
        checkEmailButton.setOnClickListener {
            checkUserEmail(emailInput.text.toString().trim())
        }

        // 비밀번호 변경 버튼 클릭 이벤트
        changePasswordButton.setOnClickListener {
            changePassword(
                currentPasswordInput.text.toString().trim(),
                newPasswordInput.text.toString().trim(),
                confirmNewPasswordInput.text.toString().trim()
            )
        }
    }

    private fun checkUserEmail(inputEmail: String) {
        if (inputEmail.isEmpty()) {
            Toast.makeText(this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val userUid = user.uid
            val userRef = realtimeDatabase.getReference("ecopath").child("UserAccount").child(userUid)

            userRef.get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        val registeredEmail = dataSnapshot.child("email").value.toString()
                        if (inputEmail == registeredEmail) {
                            Toast.makeText(this, "이메일 인증 성공.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "등록된 이메일과 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "사용자 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "데이터 조회 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String, confirmNewPassword: String) {
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmNewPassword) {
            Toast.makeText(this, "새 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null && user.email != null) {
            // 사용자 재인증
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // 비밀번호 업데이트
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updateTask ->
                                if (updateTask.isSuccessful) {
                                    // 비밀번호 변경 성공 후 Realtime Database 업데이트
                                    val userUid = user.uid
                                    val userRef = FirebaseDatabase.getInstance().reference
                                        .child("ecopath")
                                        .child("UserAccount")
                                        .child(userUid)

                                    // 변경된 비밀번호를 Realtime Database에 저장
                                    userRef.child("password").setValue(newPassword)
                                        .addOnSuccessListener {
                                            Toast.makeText(this, "비밀번호 변경 성공 및 Realtime Database 업데이트 완료.", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(this, "Realtime Database 업데이트 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(this, "비밀번호 변경 실패: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "사용자 인증 실패.", Toast.LENGTH_SHORT).show()
        }
    }

}

