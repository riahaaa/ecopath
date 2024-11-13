package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class PostDetailActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewContent: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        textViewUsername = findViewById(R.id.textViewUsername)
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewContent = findViewById(R.id.textViewContent)

        // Intent로 전달된 게시글 데이터 받기
        val username = intent.getStringExtra("username") ?: "알 수 없음"
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val content = intent.getStringExtra("content") ?: "내용 없음"

        // 텍스트뷰에 데이터 설정
        textViewUsername.text = username
        textViewTitle.text = title
        textViewContent.text = content
    }
}
