package edu.sungshin.ecopath

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PostDetailActivity : AppCompatActivity() {

    private lateinit var textViewUsername: TextView
    private lateinit var textViewTitle: TextView
    private lateinit var textViewContent: TextView
    private lateinit var imageViewPost: ImageView //이미지 뷰를 추가

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        textViewUsername = findViewById(R.id.textViewUsername)
        textViewTitle = findViewById(R.id.textViewTitle)
        textViewContent = findViewById(R.id.textViewContent)
        imageViewPost= findViewById(R.id.imageViewPost)

        // Intent로 전달된 게시글 데이터 받기
        val username = intent.getStringExtra("username") ?: "알 수 없음"
        val title = intent.getStringExtra("title") ?: "제목 없음"
        val content = intent.getStringExtra("content") ?: "내용 없음"
        val imageUrl= intent.getStringExtra("imageUrl")

        // 텍스트뷰에 데이터 설정
        textViewUsername.text = username
        textViewTitle.text = title
        textViewContent.text = content

        // 이미지 URL이 있으면 이미지뷰에 로드
        imageUrl?.let {
            Glide.with(this)
                .load(it)
                .into(imageViewPost)
        } ?: imageViewPost.setImageResource(R.drawable.placeholder) // URL이 없으면 기본 이미지

    }
}
