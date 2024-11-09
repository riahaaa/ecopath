package edu.sungshin.ecopath


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ConfirmActivity : AppCompatActivity() {

    private lateinit var textViewRouteInfo: TextView
    private lateinit var textViewRouteDistance: TextView
    private lateinit var textViewRouteDuration: TextView
    private lateinit var textViewRouteTravelMode: TextView
    private lateinit var textViewCarbonEmission: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm)

        textViewRouteInfo = findViewById(R.id.textViewRouteInfo)
        textViewRouteDistance = findViewById(R.id.textViewRouteDistance)
        textViewRouteDuration = findViewById(R.id.textViewRouteDuration)
        textViewRouteTravelMode = findViewById(R.id.textViewRouteTravelMode)
        textViewCarbonEmission = findViewById(R.id.textViewCarbonEmission)

        // Intent로 전달된 데이터 받기
        val routeInfo = intent.getStringExtra("selectedRouteInfo") ?: "정보 없음"
        val routeDistance = intent.getStringExtra("selectedRouteDistance") ?: "정보 없음"
        val routeDuration = intent.getStringExtra("selectedRouteDuration") ?: "정보 없음"
        val routeTravelMode = intent.getStringExtra("selectedRouteTravelMode") ?: "정보 없음"
        val carbonEmission = intent.getDoubleExtra("carbonEmission", 0.0)

        // 데이터 표시
        textViewRouteInfo.text = "경로 정보: $routeInfo"
        textViewRouteDistance.text = "거리: $routeDistance"
        textViewRouteDuration.text = "소요 시간: $routeDuration"
        textViewRouteTravelMode.text = "교통 수단: $routeTravelMode"
        textViewCarbonEmission.text = "탄소 배출량: ${String.format("%.2f", carbonEmission)} kg"
    }
}
