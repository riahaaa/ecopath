package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.database.FirebaseDatabase
import android.util.Log


class MemberActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var realtimeDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        // Firebase 초기화
        auth = FirebaseAuth.getInstance()
        realtimeDatabase = FirebaseDatabase.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val currentPasswordInput = findViewById<EditText>(R.id.currentPasswordInput)
        val newPasswordInput = findViewById<EditText>(R.id.newPasswordInput)
        val confirmNewPasswordInput = findViewById<EditText>(R.id.confirmNewPasswordInput)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)

        // 사용자 이메일 불러오기
        loadUserData(emailInput)

        // 비밀번호 변경 버튼 클릭 이벤트
        changePasswordButton.setOnClickListener {
            changePassword(
                currentPasswordInput.text.toString().trim(),
                newPasswordInput.text.toString().trim(),
                confirmNewPasswordInput.text.toString().trim()
            )
        }
    }

    private fun loadUserData(emailInput: EditText) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            Log.d("UID 확인", "현재 UID: $userId")

            // Realtime Database에서 사용자 데이터 가져오기
            val userRef = realtimeDatabase.reference.child("UserAccount").child(userId)
            Log.d("데이터 요청", "요청 경로: UserAccount/$userId")

            userRef.get()
                .addOnSuccessListener { dataSnapshot ->
                    if (dataSnapshot.exists()) {
                        Log.d("데이터 요청 성공", "데이터: ${dataSnapshot.value}")
                    } else {
                        Log.e("데이터 요청 실패", "UserAccount/$userId 경로에 데이터 없음")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("데이터 요청 실패", "오류: ${e.message}")
                }

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
                                    Toast.makeText(this, "비밀번호가 성공적으로 변경되었습니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "비밀번호 변경 실패: ${updateTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "현재 비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "사용자 인증에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
}

