package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val menuButton = findViewById<ImageButton>(R.id.menuButton)
        menuButton.setOnClickListener {
            // MyPageActivity로 이동하는 인텐트 생성
            val intent = Intent(this, MyPageActivity::class.java)
            startActivity(intent)


        }
        val calculateButton=findViewById<Button>(R.id.calculatebutton)
        calculateButton.setOnClickListener{
            val intent = Intent(this,CalculateActivity::class.java)
            startActivity(intent)
        }

        val calcualteButton = findViewById<Button>(R.id.calculatebutton)
        calcualteButton.setOnClickListener{
            val intent = Intent(this, CalculateActivity::class.java)
            startActivity(intent)

        }
    }
}
