package edu.sungshin.ecopath

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreatePostActivity : AppCompatActivity() {

    private lateinit var editTextTitle: EditText
    private lateinit var editTextContent: EditText
    private lateinit var buttonPost: Button
    private lateinit var imageView: ImageView
    private lateinit var buttonSelectImage: Button
    private var selectedImageUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_post)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)
        }

        // UI 요소 초기화
        editTextTitle = findViewById(R.id.editTextTitle)
        editTextContent = findViewById(R.id.editTextContent)
        buttonPost = findViewById(R.id.buttonPost)
        imageView = findViewById(R.id.imageView)
        buttonSelectImage = findViewById(R.id.buttonSelectImage)

        // 이미지 선택 버튼 클릭 리스너
        buttonSelectImage.setOnClickListener {
            openGallery()
        }

        // 게시글 작성 버튼 클릭 리스너
        buttonPost.setOnClickListener {
            val title = editTextTitle.text.toString().trim()
            val content = editTextContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(this, "제목과 내용을 입력하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val imageUrl = selectedImageUri?.let { uri -> uploadImageToStorage(uri) }
                    savePostToFirestore(title, content, "익명 사용자", imageUrl)
                    runOnUiThread {
                        Toast.makeText(this@CreatePostActivity, "게시물이 성공적으로 업로드되었습니다.", Toast.LENGTH_SHORT).show()
                        finish() // 작성 완료 후 액티비티 종료
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(this@CreatePostActivity, "업로드에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // 갤러리 열기
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_IMAGE_PICK)
    }

    // 갤러리에서 이미지 선택 후 결과 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data

            if (selectedImageUri != null) {
                imageView.setImageURI(selectedImageUri) // 선택한 이미지 미리보기
                imageView.visibility = View.VISIBLE
                android.util.Log.d("CreatePostActivity", "Selected Image URI: $selectedImageUri")
            } else {
                Toast.makeText(this, "이미지가 선택되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Firebase Storage에 이미지 업로드 (비동기 작업)
    private suspend fun uploadImageToStorage(uri: Uri): String {
        val fileName = "images/${System.currentTimeMillis()}.jpg"
        val imageRef = storageRef.child(fileName)
        try {
            imageRef.putFile(uri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            android.util.Log.d("CreatePostActivity", "Uploaded Image URL: $downloadUrl")
            return downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("CreatePostActivity", "Image Upload Failed: ${e.message}")
            throw e
        }
    }

    private suspend fun savePostToFirestore(
        title: String,
        content: String,
        username: String,
        imageUrl: String?
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            runOnUiThread {
                Toast.makeText(this, "사용자가 로그인되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val databaseRef = FirebaseDatabase.getInstance().getReference("ecopath").child("UserAccount").child(uid)
        val snapshot = databaseRef.get().await()

// 로그 추가해서 데이터 확인
        Log.d("CreatePostActivity", "Snapshot Data: ${snapshot.value}")
        val usernameFromDB = snapshot.child("id").value as? String
        Log.d("CreatePostActivity", "Username from DB: $usernameFromDB")

        if (usernameFromDB == null) {
            runOnUiThread {
                Toast.makeText(this, "사용자의 id를 가져올 수 없습니다. 기본값을 사용합니다.", Toast.LENGTH_SHORT).show()
            }
            return
        }



        val postId = System.currentTimeMillis().toString()
        val post = hashMapOf(
            "postId" to postId,
            "title" to title,
            "content" to content,
            "id" to usernameFromDB, // "id" 필드에서 사용자 이름을 가져옵니다
            "imageUrl" to imageUrl,
            "timestamp" to Timestamp.now(),
            "likes" to 0,
            "commentCount" to 0
        )

        // Firestore에 게시글 저장
        firestore.collection("posts")
            .document(postId)
            .set(post)
            .await()
    }

}
