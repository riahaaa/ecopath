package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query


class HomeActivity : AppCompatActivity() {
    private lateinit var recentPostCard: CardView
    private lateinit var postTitleTextView: TextView
    private lateinit var postSnippetTextView: TextView

    private lateinit var viewPager: ViewPager2

    private val firestore = FirebaseFirestore.getInstance()
    private var postListener: ListenerRegistration? = null // Firestore 리스너 등록 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        recentPostCard = findViewById(R.id.viewRecentPost)
        postTitleTextView = findViewById(R.id.post_title)
        postSnippetTextView = findViewById(R.id.post_snippet)
        viewPager = findViewById(R.id.viewPager)

        val tips = listOf(
            TipCard("Tip 1", "Tip 1 : 친환경 소재 제품을 사용하세요.",R.drawable.eco_friendly),
            TipCard("Tip 2", "Tip 2 : 에너지 효율이 높은 제품을 선택하세요.",R.drawable.energy_efficiency),
            TipCard("Tip 3", "Tip 3 : 쓰레기를 줄이고 대중교통을 이용하세요.",R.drawable.bus_icon),
            TipCard("Tip 4", "Tip 4 :숲을 함부로 훼손하는 일은 하지 마세요.",R.drawable.icon_tree)
        )

        viewPager.adapter = TipCardAdapter(tips)

        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        menuButton.setOnClickListener {
            // MyPageActivity로 이동하는 인텐트 생성
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)


        }

        val calcualteButton = findViewById<Button>(R.id.calculatebutton)
        calcualteButton.setOnClickListener{
            val intent = Intent(this, CalculateActivity::class.java)
            startActivity(intent)

        }

        // CardView 클릭 이벤트
        recentPostCard.setOnClickListener {
            // 게시판 화면(PostListActivity)으로 이동
            val intent = Intent(this, PostListActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        // 화면이 활성화될 때 Firestore 리스너 등록
        startListeningForPosts()
    }
    override fun onPause() {
        super.onPause()
        // 화면이 비활성화될 때 Firestore 리스너 제거
        stopListeningForPosts()
    }
    // Firestore 실시간 리스너 등록
    private fun startListeningForPosts() {
        postListener = firestore.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING) // 최신순 정렬
            .limit(1) // 가장 최근 1개 게시물만 감지
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    postTitleTextView.text = "오류 발생"
                    postSnippetTextView.text = "게시물을 불러올 수 없습니다."
                    return@addSnapshotListener
                }

                if (snapshots != null && !snapshots.isEmpty) {
                    val document = snapshots.documents.first()
                    val title = document.getString("title") ?: "제목 없음"
                    val content = document.getString("content") ?: "내용 없음"

                    // 제목과 내용 일부를 TextView에 설정
                    postTitleTextView.text = title
                    postSnippetTextView.text = content.take(50) // 내용 일부 (50자까지)
                } else {
                    postTitleTextView.text = "게시글이 없습니다."
                    postSnippetTextView.text = "게시판에 새로운 게시글을 작성해보세요."
                }
            }
    }

    // Firestore 실시간 리스너 제거
    private fun stopListeningForPosts() {
        postListener?.remove()
        postListener = null
    }
}