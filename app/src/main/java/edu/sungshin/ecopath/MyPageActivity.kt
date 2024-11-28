package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MyPageActivity : AppCompatActivity() {

    private lateinit var userIdTextView: TextView
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

        // TextView 참조
        userIdTextView = findViewById(R.id.userId)

        // 회원정보 수정 버튼 클릭 리스너
        val modifyProfileButton = findViewById<TextView>(R.id.modifyProfile)
        modifyProfileButton.setOnClickListener {
            val intent = Intent(this, ModifyProfileActivity::class.java)
            startActivity(intent)
        }

        // 현재 로그인한 사용자 가져오기
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userUid = currentUser.uid
            Log.d("DEBUG", "Current User UID: $userUid")

            // Realtime Database에서 사용자 정보 가져오기
            database.child("ecopath").child("UserAccount").child(userUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userName = snapshot.child("id").getValue(String::class.java) ?: "사용자"
                            userIdTextView.text = "${userName}님, 환영합니다!"
                            Log.d("DEBUG", "Fetched User ID: $userName")
                        } else {
                            userIdTextView.text = "사용자 정보를 찾을 수 없습니다."
                            Log.e("DEBUG", "Document does not exist")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        userIdTextView.text = "데이터 로드 실패"
                        Log.e("DEBUG", "Database Error: ${error.message}")
                    }
                })
        } else {
            userIdTextView.text = "로그인이 필요합니다."
            Log.e("DEBUG", "No user is logged in")
        }
    }
}
