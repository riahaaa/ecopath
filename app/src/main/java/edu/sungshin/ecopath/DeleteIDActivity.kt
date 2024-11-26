package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class DeleteIDActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var passwordEditText: EditText
    private lateinit var deleteIDBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_deleteid)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        passwordEditText = findViewById(R.id.password)
        deleteIDBtn = findViewById(R.id.deleteIDBtn)

        // 회원 탈퇴 버튼을 비활성화로 설정
        deleteIDBtn.isEnabled = false

        deleteIDBtn.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // 계정 삭제
                deleteUserAccount(currentUser)
            } else {
                Toast.makeText(this, "로그인 상태가 아닙니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 비밀번호 입력 후 재인증
        findViewById<Button>(R.id.reAuthenticateBtn).setOnClickListener {
            val password = passwordEditText.text.toString().trim()

            if (password.isNotEmpty()) {
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    // 비밀번호 확인 후 계정 삭제
                    reAuthenticateAndEnableDelete(currentUser, password)
                } else {
                    Toast.makeText(this, "로그인 상태가 아닙니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun reAuthenticateAndEnableDelete(user: FirebaseUser, password: String) {
        val credential = EmailAuthProvider.getCredential(user.email!!, password)

        // 비밀번호 재인증
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // 비밀번호가 맞으면 성공 메시지 출력
                Toast.makeText(this, "비밀번호가 맞았습니다.", Toast.LENGTH_SHORT).show()
                // 회원 탈퇴 버튼 활성화
                deleteIDBtn.isEnabled = true
            } else {
                Toast.makeText(this, "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteUserAccount(user: FirebaseUser) {
        val userId = user.uid

        // Realtime Database에서 해당 사용자 데이터 삭제
        val userRef = database.getReference("UserAccount").child(userId)
        userRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Firebase Authentication에서 사용자 삭제
                user.delete().addOnCompleteListener { deleteTask ->
                    if (deleteTask.isSuccessful) {
                        Toast.makeText(this, "회원 탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()  // 활동 종료 (로그아웃)
                    } else {
                        Toast.makeText(this, "회원 탈퇴 실패: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "사용자 데이터 삭제 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
