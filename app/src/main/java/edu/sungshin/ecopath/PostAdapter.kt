package edu.sungshin.ecopath

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import com.bumptech.glide.Glide
import android.widget.ImageView

class PostAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {
        private val dateFormat=SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())//시간표시

    fun getPostList(): MutableList<Post>{
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
        holder.textViewAuthor.text = post.username //작성자 이름 상단에 표시
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

        //firestore에서 가져온 이미지 url있으면 glide로 이미지 로드
        post.imageUrl?.let {
            Glide.with(holder.itemView.context)
                .load(it)
                .into(holder.imageViewPost)
        } ?: holder.imageViewPost.setImageResource(R.drawable.placeholder)

        // 이미지를 클릭할 때 게시글 상세 화면으로 이동
        holder.imageViewPost.setOnClickListener {
            val context = it.context
            val intent = Intent(context, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id) // 게시글 ID 전달
            intent.putExtra("title", post.title) // 게시글 제목 전달
            intent.putExtra("content", post.content) // 게시글 내용 전달
            intent.putExtra("imageUrl", post.imageUrl) // 이미지 URL 전달
            context.startActivity(intent)
        }



        // 수정 버튼 클릭 리스너
        holder.buttonEdit.setOnClickListener {
            val context = it.context
            val intent = Intent(context, EditPostActivity::class.java)
            intent.putExtra("postId", post.id) // 게시글 ID를 넘겨줘야 수정이 가능
            context.startActivity(intent)
        }

        // 삭제 버튼 클릭 리스너
        holder.buttonDelete.setOnClickListener {
            val context = it.context
            // Firestore에서 해당 게시글 삭제
            FirebaseFirestore.getInstance()
                .collection("posts")
                .document(post.id)  // post.id는 각 게시글의 고유 ID
                .delete()
                .addOnSuccessListener {
                    // 삭제 후 postList에서 해당 게시글 제거하고 RecyclerView 갱신
                    postList.removeAt(position)
                    notifyItemRemoved(position)
                    Toast.makeText(context, "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "게시글 삭제 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount(): Int = postList.size
}
