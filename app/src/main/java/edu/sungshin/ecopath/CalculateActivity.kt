package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.util.Log

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import okhttp3.OkHttpClient
import okhttp3.Request

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder

class CalculateActivity : AppCompatActivity() {

    private lateinit var editTextOrigin: EditText
    private lateinit var editTextDestination:EditText
    private lateinit var buttonOriginSearch: Button
    private lateinit var buttonDestinationSearch: Button
    private lateinit var buttonSearch: Button
    private lateinit var recyclerViewOriginResults: RecyclerView
    private lateinit var recyclerViewDestinationResults: RecyclerView
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var textViewCarbonEmission: TextView
    private lateinit var buttonEcoAlternative: Button

    val KakaoApiKey = BuildConfig.KAKAO_API_KEY

    private var originLatLng: Pair<Double, Double>? = null
    private var destinationLatLng: Pair<Double, Double>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        // 초기화
        editTextOrigin = findViewById(R.id.editTextOrigin)
        editTextDestination = findViewById(R.id.editTextDestination)
        buttonOriginSearch = findViewById(R.id.OriginSearchButton)
        buttonDestinationSearch = findViewById(R.id.DestinationSearchButton)
        buttonSearch = findViewById(R.id.buttonSearch)
        recyclerViewOriginResults = findViewById(R.id.recyclerViewOriginResults)
        recyclerViewDestinationResults = findViewById(R.id.recyclerViewDestinationResults)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        textViewCarbonEmission = findViewById(R.id.textViewCarbonEmission)
        buttonEcoAlternative = findViewById(R.id.buttonEcoAlternative)

        // RecyclerView 설정
        recyclerViewOriginResults.layoutManager = LinearLayoutManager(this)
        recyclerViewOriginResults.setHasFixedSize(true)

        recyclerViewDestinationResults.layoutManager = LinearLayoutManager(this)
        recyclerViewDestinationResults.setHasFixedSize(true)

        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        recyclerViewRoutes.setHasFixedSize(true)

        // 출발지와 목적지 검색 UI 설정
        setupSearchButton(buttonOriginSearch) { query ->
            if (query.isNotEmpty()) {
                searchPlaceWithKakao(query, isOrigin = true) { latLng ->
                    originLatLng = latLng
                    Log.d("CalculateActivity", "출발지 좌표: $originLatLng")
                }
            }
        }
        setupSearchButton(buttonDestinationSearch) { query ->
            if (query.isNotEmpty()) {
                searchPlaceWithKakao(query, isOrigin = false) { latLng ->
                    destinationLatLng = latLng
                    Log.d("CalculateActivity", "목적지 좌표: $destinationLatLng")
                }
            }
        }

        // 경로 탐색 버튼 클릭 리스너
        buttonSearch.setOnClickListener {
            if (originLatLng == null) {
                Toast.makeText(this, "출발지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else if (destinationLatLng == null) {
                Toast.makeText(this, "목적지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                fetchRoutesFromAPI()
            }
        }

        // 친환경 대안 보기 버튼 클릭 리스너
        buttonEcoAlternative.setOnClickListener {
            showEcoAlternatives()
        }
    }

    private fun searchPlaceWithKakao(query: String, isOrigin: Boolean, callback: (Pair<Double, Double>?) -> Unit) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val kakaoApiUrl = "https://dapi.kakao.com/v2/local/search/keyword.json?query=$encodedQuery"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(kakaoApiUrl)
            .addHeader("Authorization", "KakaoAK $KakaoApiKey")
            .build()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                client.newCall(request).execute().use { response ->
                    val responseData = response.body?.string()
                    if (response.isSuccessful && responseData != null) {
                        val json = JSONObject(responseData)
                        val documents = json.getJSONArray("documents")
                        if (documents.length() > 0) {
                            val place = documents.getJSONObject(0)
                            val lat = place.getDouble("y")
                            val lng = place.getDouble("x")
                            val latLng = Pair(lat, lng)
                            withContext(Dispatchers.Main) {
                                callback(latLng)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                callback(null)
                                Toast.makeText(this@CalculateActivity, "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CalculateActivity", "Error fetching place", e)
            }
        }
    }

    private fun setupSearchButton(editText: EditText, searchCallback: (String) -> Unit) {
        editText.setOnEditorActionListener { v, actionId, event ->
            val query = v.text.toString().trim()
            if (query.isNotEmpty()) {
                searchCallback(query)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun fetchRoutesFromAPI() {
        val originLatLng = this.originLatLng
        val destinationLatLng = this.destinationLatLng


        if (originLatLng != null && destinationLatLng != null) {
            val url = "https://apis-navi.kakaomobility.com/v1/directions?origin=${originLatLng.second},${originLatLng.first}&destination=${destinationLatLng.second},${destinationLatLng.first}&priority=RECOMMEND"
            val client = OkHttpClient()
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "KakaoAK $KakaoApiKey")
                .build()

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    client.newCall(request).execute().use { response ->
                        val responseData = response.body?.string()
                        if (response.isSuccessful && responseData != null) {
                            val routes = parseDirectionsApiResponse(responseData)
                            withContext(Dispatchers.Main) {
                                recyclerViewRoutes.adapter = RouteAdapter(routes) { selectedRoute ->
                                    showSelectedRouteAndCarbonEmission(selectedRoute)
                                }
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@CalculateActivity, "경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalculateActivity", "Error fetching routes", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CalculateActivity, "경로를 가져오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun parseDirectionsApiResponse(response: String): List<Route> {
        val routeList = mutableListOf<Route>()
        val jsonObject = JSONObject(response)
        val routes = jsonObject.getJSONArray("routes")

        for (i in 0 until routes.length()) {
            val route = routes.getJSONObject(i)
            val summary = route.getJSONObject("summary")
            val distance = summary.getJSONObject("distance").getString("text")
            val duration = summary.getJSONObject("duration").getString("text")
            val travelMode = "driving" // 기본 설정

            routeList.add(
                Route(
                    info = "추천 경로 ${i + 1}",
                    distance = distance,
                    duration = duration,
                    travelMode = travelMode
                )
            )
        }
        return routeList
    }

    private fun showSelectedRouteAndCarbonEmission(route: Route) {
        val carbonEmission = calculateCarbonEmission(route.distance, route.travelMode)
        textViewCarbonEmission.text = "탄소 배출량: ${String.format("%.2f", carbonEmission)} kg"

        val intent = Intent(this, ConfirmActivity::class.java).apply {
            putExtra("selectedRouteInfo", route.info)
            putExtra("selectedRouteDistance", route.distance)
            putExtra("selectedRouteDuration", route.duration)
            putExtra("selectedRouteTravelMode", route.travelMode)
            putExtra("carbonEmission", carbonEmission)
        }
        startActivity(intent)
    }

    private fun calculateCarbonEmission(distanceText: String, travelMode: String): Double {
        val distanceInKm = try {
            distanceText.split(" ")[0].replace(",", "").toDouble()
        } catch (e: Exception) {
            0.0
        }

        val emissionFactor = when (travelMode.lowercase()) {
            "driving" -> 0.18
            "walking" -> 0.0
            "bicycling" -> 0.0
            "transit" -> 0.07
            "electric driving" -> 0.05
            "hybrid driving" -> 0.10
            else -> 0.18
        }
        return distanceInKm * emissionFactor
    }

    private fun showEcoAlternatives() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("친환경 대안")
            .setMessage("탄소 배출량을 줄일 수 있는 대안:\n\n- 도보\n- 자전거\n- 대중교통 이용")
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}