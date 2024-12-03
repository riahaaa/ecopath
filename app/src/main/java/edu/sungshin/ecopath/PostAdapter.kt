package edu.sungshin.ecopath

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class PostAdapter(private val postList: MutableList<Post>) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewAuthor: TextView = itemView.findViewById(R.id.textViewAuthor)
        val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        val textViewSnippet: TextView = itemView.findViewById(R.id.textViewSnippet)
        val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        val textViewLikes: TextView = itemView.findViewById(R.id.textViewLikes) // 공감 수
        val textViewCommentCount: TextView = itemView.findViewById(R.id.textViewCommentCount) // 댓글 수
        val buttonEdit: ImageButton = itemView.findViewById(R.id.buttonEdit) // 수정 버튼
        val buttonDelete: ImageButton = itemView.findViewById(R.id.buttonDelete) // 삭제 버튼
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        holder.textViewAuthor.text = post.username
        holder.textViewTitle.text = post.title
        holder.textViewSnippet.text = post.content.take(100)
        post.timestamp?.let {
            holder.textViewTimestamp.text = dateFormat.format(it.toDate())
        } ?: run {
            holder.textViewTimestamp.text = "시간 정보 없음"
        }

        // 공감 수와 댓글 수 설정
        holder.textViewLikes.text = "공감 ${post.likes}"
        holder.textViewCommentCount.text = "댓글 ${post.commentCount}"

        // 게시글 클릭 리스너
        holder.itemView.setOnClickListener {
            if (post.postid.isNullOrEmpty()) {
                android.util.Log.e("PostAdapter", "Invalid post ID")
                Toast.makeText(it.context, "잘못된 게시글입니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!holder.buttonEdit.isPressed && !holder.buttonDelete.isPressed) {
                val context = it.context
                val intent = Intent(context, PostDetailActivity::class.java)
                intent.putExtra("postId", post.postid)
                intent.putExtra("title", post.title)
                intent.putExtra("content", post.content)
                context.startActivity(intent)
            }
        }

        // 수정 버튼 클릭 리스너
        holder.buttonEdit.setOnClickListener {
            val context = it.context
            val intent = Intent(context, EditPostActivity::class.java)
            intent.putExtra("postId", post.postid) // 게시글 ID를 넘겨줘야 수정이 가능

            // 디버깅 로그 추가
            android.util.Log.d("PostAdapter", "Edit clicked: ID=${post.postid}")

            context.startActivity(intent)
        }

        holder.buttonDelete.setOnClickListener {
            val context = holder.itemView.context
            val docRef = db.collection("posts").document(post.postid) // 게시글 ID를 기준으로 문서 참조

            // Firestore 문서 가져오기
            docRef.get().addOnSuccessListener { document ->
                // 문서가 존재하고 삭제 권한이 있는지 확인
                if (document.exists() && document.getString("uid") == auth.currentUser?.uid) {
                    // 문서 삭제
                    docRef.delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "게시물이 삭제되었습니다.", Toast.LENGTH_SHORT).show()

                            // 삭제 성공 시 postList에서 항목 제거
                            val position = holder.bindingAdapterPosition
                            if (position != RecyclerView.NO_POSITION) {
                                postList.removeAt(position)
                                notifyItemRemoved(position) // RecyclerView 갱신
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "게시물 삭제 실패", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "삭제 권한이 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(context, "게시물을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = postList.size
}