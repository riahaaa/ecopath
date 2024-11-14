package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MyContentActivity: AppCompatActivity() {

    private lateinit var closeButton: TextView
    private lateinit var modifyProfile: TextView
    private lateinit var cancelService: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_content)  // myContent.xml 레이아웃을 연결

        // 뷰 초기화
        closeButton = findViewById(R.id.closeButton)
        modifyProfile = findViewById(R.id.modifyProfile)
        cancelService = findViewById(R.id.cancelService)

        // 닫기 버튼 클릭 리스너 설정
        closeButton.setOnClickListener {
            finish()  // 현재 Activity 종료, 이전 화면으로 돌아갑니다.
        }
    }

    // "내가 쓴 게시글" 클릭 시 실행되는 메서드
    /*fun onModifyProfileClick(view: android.view.View) {
        // 예: 게시글 목록 화면으로 이동
        val intent = Intent(this, MyPostsActivity::class.java)
        startActivity(intent)
    }

    // "내가 쓴 댓글" 클릭 시 실행되는 메서드
    fun onCancelServiceClick(view: android.view.View) {
        // 예: 댓글 목록 화면으로 이동
        val intent = Intent(this, MyCommentsActivity::class.java)
        startActivity(intent)
    }*/
}
