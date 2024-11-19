package edu.sungshin.ecopath


import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConfirmActivity : AppCompatActivity() {

    private lateinit var textViewRouteInfo: TextView
    private lateinit var textViewRouteDistance: TextView
    private lateinit var textViewRouteDuration: TextView
    private lateinit var textViewRouteCarType: TextView
    private lateinit var textViewCarbonEmission: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        textViewRouteInfo = findViewById(R.id.textViewRouteInfo)
        textViewRouteDistance = findViewById(R.id.textViewRouteDistance)
        textViewRouteDuration = findViewById(R.id.textViewRouteDuration)
        textViewRouteCarType = findViewById(R.id.textViewRouteCarType)
        textViewCarbonEmission = findViewById(R.id.textViewCarbonEmission)

        // Intent로 전달된 데이터 받기
        val routeInfo = intent.getStringExtra("selectedRouteInfo") ?: "정보 없음"
        val routeDistance = intent.getStringExtra("selectedRouteDistance") ?: "정보 없음"
        val routeDuration = intent.getStringExtra("selectedRouteDuration") ?: "정보 없음"
        val routeCarType = intent.getStringExtra("selectedRouteCarType") ?: "정보 없음"
        val carbonEmission = intent.getDoubleExtra("carbonEmission", 0.0)

        // 데이터 표시
        textViewRouteInfo.text = routeInfo
        textViewRouteDistance.text = routeDistance  // 이미 포맷된 거리
        textViewRouteDuration.text = routeDuration  // 이미 포맷된 시간
        textViewRouteCarType.text = routeCarType
        textViewCarbonEmission.text = "탄소 배출량: ${String.format("%.2f", carbonEmission)} kg"
    }
}

