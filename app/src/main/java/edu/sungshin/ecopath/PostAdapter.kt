package edu.sungshin.ecopath

import edu.sungshin.ecopath.R
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.ImageView

class PostAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) // 시간 표시

    fun getPostList(): MutableList<Post> {
        return postList
    }

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAuthor: TextView = itemView.findViewById(R.id.textViewAuthor)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewSnippet: TextView = itemView.findViewById(R.id.textViewSnippet)
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val textViewLikes: TextView = itemView.findViewById(R.id.textViewLikes) // 공감 수
        val textViewCommentCount: TextView = itemView.findViewById(R.id.textViewCommentCount) // 댓글 수
        val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit) // 수정 버튼
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete) // 삭제 버튼
        val imageViewPost: ImageView = itemView.findViewById(R.id.imageViewPost)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        holder.textViewAuthor.text = post.username // 작성자 이름 표시
        holder.textViewTitle.text = post.title
        holder.textViewSnippet.text = post.content.take(100) // 본문 일부만 표시

        // 작성 시간을 읽기 쉬운 형식으로 변환하여 표시
        post.timestamp?.let {
            holder.textViewTimestamp.text = dateFormat.format(it.toDate())
        } ?: run {
            holder.textViewTimestamp.text = "시간 정보 없음"
        }

        // 공감 수와 댓글 수 설정
        holder.textViewLikes.text = "공감 ${post.likes}"
        holder.textViewCommentCount.text = "댓글 ${post.commentCount}"

        // Firestore에서 가져온 이미지 URL 있으면 Glide로 이미지 로드
        post.imageUrl?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.imageViewPost)
        } ?: holder.imageViewPost.setImageResource(R.drawable.placeholder)

        // 이미지를 클릭할 때 게시글 상세 화면으로 이동
        holder.imageViewPost.setOnClickListener {
            val context = it.context
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("postId", post.postid) // 게시글 ID 전달
            intent.putExtra("title", post.title) // 게시글 제목 전달
            intent.putExtra("content", post.content) // 게시글 내용 전달
            intent.putExtra("imageUrl", post.imageUrl) // 이미지 URL 전달
            context.startActivity(intent)
        }

        // 수정 버튼 클릭 리스너
        holder.buttonEdit.setOnClickListener {
            val context = it.context
            val intent = Intent(context, EditPostActivity::class.java)
            intent.putExtra("postId", post.postid) // 게시글 ID를 넘겨줘야 수정이 가능
            context.startActivity(intent)
        }

        // 삭제 버튼 클릭 리스너
        holder.buttonDelete.setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            db.collection("posts").document(post.postid).delete()
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                    postList.removeAt(position)
                    notifyItemRemoved(position) // 데이터 삭제 후 화면 갱신
                }
                .addOnFailureListener {
                    Toast.makeText(holder.itemView.context, "게시물 삭제 실패", Toast.LENGTH_SHORT).show()
                }
        }
        // 이미지 클릭 리스너에서 의도치 않게 이동을 막는 부분을 추가
        holder.imageViewPost.setOnClickListener {
            // 수정 및 삭제 버튼이 클릭된 상태라면 이미지 클릭을 무시
            if (holder.buttonEdit.isPressed || holder.buttonDelete.isPressed) {
                return@setOnClickListener
            }
            val context = it.context
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("postId", post.postid) // 게시글 ID 전달
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}
