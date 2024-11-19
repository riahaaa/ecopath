package edu.sungshin.ecopath

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

class CommentAdapter(private val commentList: MutableList<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCommentAuthor: TextView = itemView.findViewById(R.id.textViewCommentAuthor)
        val textViewCommentTimestamp: TextView = itemView.findViewById(R.id.textViewCommentTimestamp)
        val textViewCommentContent: TextView = itemView.findViewById(R.id.textViewCommentContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        //uid 로그를 추가
        Log.d("CommentAdapter","Author ID: ${comment.username}")

        // 작성자 ID (UID) 표시
        holder.textViewCommentAuthor.text = comment.username
        holder.textViewCommentContent.text = comment.content

        // 작성 시간 표시
        holder.textViewCommentTimestamp.text = comment.timestamp?.let {
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(it.toDate())
        } ?: "시간 정보 없음"
    }

    override fun getItemCount(): Int = commentList.size
}
