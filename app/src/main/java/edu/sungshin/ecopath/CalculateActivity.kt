package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
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
    private lateinit var spinnerCarType: Spinner
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var textViewCarbonEmission: TextView
    private lateinit var buttoninformation: Button

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
        spinnerCarType = findViewById(R.id.spinnerTransportMode)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        textViewCarbonEmission = findViewById(R.id.textViewCarbonEmission)
        buttoninformation = findViewById(R.id.buttoninformation)


        val buttoninformation = findViewById<Button>(R.id.buttoninformation)
        buttoninformation .setOnClickListener {
            // MyPageActivity로 이동하는 인텐트 생성
            val intent = Intent(this, CalculateInformationActivity::class.java)
            startActivity(intent)
        }

        // RecyclerView 설정
        recyclerViewOriginResults.layoutManager = GridLayoutManager(this, 1)
        recyclerViewOriginResults.setHasFixedSize(true)

        recyclerViewDestinationResults.layoutManager = GridLayoutManager(this, 1)
        recyclerViewDestinationResults.setHasFixedSize(true)


        recyclerViewRoutes.layoutManager = GridLayoutManager(this,1)
        recyclerViewRoutes.setHasFixedSize(true)

        // Spinner
        val carType = arrayOf("자동차(휘발유, 디젤)", "전기차", "하이브리드차")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, carType)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCarType.adapter = adapter

        // 출발지와 목적지 검색 UI 설정
        setupSearchButton(buttonOriginSearch, editTextOrigin, recyclerViewOriginResults) { query ->
            if (query.isNotEmpty()) {
                searchPlaceWithKakao(query, isOrigin = true) { latLng ->
                    originLatLng = latLng
                    Log.d("CalculateActivity", "출발지 좌표: $originLatLng")
                }
            }
        }

        setupSearchButton(buttonDestinationSearch, editTextDestination, recyclerViewDestinationResults) { query ->
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
                val selectedCarType = spinnerCarType.selectedItem.toString()  // 선택된 이동수단
                fetchRoutesFromAPI(selectedCarType)
            }
        }



    }

    // 장소 검색 함수
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
                        val places = mutableListOf<Place>()

                        for (idx in 0 until documents.length()) {
                            val place = documents.getJSONObject(idx)
                            val name = place.getString("place_name")
                            val address = place.getString("address_name")
                            val lat = place.getDouble("y")
                            val lng = place.getDouble("x")
                            places.add(Place(name, address, lat, lng))
                        }

                        // 검색 결과가 없으면 3개의 '결과 없음'을 추가+
                        if (places.isEmpty()) {
                            places.add(Place("검색 결과가 없습니다", "N/A", 0.0, 0.0))
                            places.add(Place("검색 결과가 없습니다", "N/A", 0.0, 0.0))
                            places.add(Place("검색 결과가 없습니다", "N/A", 0.0, 0.0))
                        }

                        withContext(Dispatchers.Main) {
                            if (places.isNotEmpty()) {
                                setupRecyclerView(
                                    if (isOrigin) recyclerViewOriginResults else recyclerViewDestinationResults,
                                    places,
                                    isOrigin
                                )
                            } else {
                                while (places.size < 3) {
                                    places.add(Place("결과 없음", "N/A", 0.0, 0.0))
                                }
                                setupRecyclerView(
                                    if (isOrigin) recyclerViewOriginResults else recyclerViewDestinationResults,
                                    places,
                                    isOrigin
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("CalculateActivity", "Error fetching place", e)
            }
        }
    }

    // RecyclerView 설정 함수
    private fun setupRecyclerView(recyclerView: RecyclerView, places: List<Place>, isOrigin: Boolean) {
        val adapter = PlaceAdapter(places) { selectedPlace ->
            val latLng = Pair(selectedPlace.lat, selectedPlace.lng)
            if (isOrigin) {
                originLatLng = latLng
                editTextOrigin.setText(selectedPlace.name)
            } else {
                destinationLatLng = latLng
                editTextDestination.setText(selectedPlace.name)
            }
            recyclerView.visibility = View.GONE
        }
        recyclerView.adapter = adapter
    }

    // 검색 버튼 클릭 리스너 설정
    private fun setupSearchButton(button: Button, editText: EditText, recyclerView: RecyclerView, searchCallback: (String) -> Unit) {
        button.setOnClickListener {
            val query = editText.text.toString().trim()
            if (query.isNotEmpty()) {
                recyclerView.visibility = View.VISIBLE
                searchCallback(query)
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 경로를 가져오는 API 호출 함수
    private fun fetchRoutesFromAPI(selectedCarType: String) {
        val originLatLng = this.originLatLng
        val destinationLatLng = this.destinationLatLng

        if (originLatLng != null && destinationLatLng != null) {
            val carType = when (selectedCarType) {
                "전기차" -> "electric driving"
                "하이브리드" -> "hybrid driving"
                else -> "driving"  // 기본적으로 자동차
            }

            val url = "https://apis-navi.kakaomobility.com/v1/directions?origin=${originLatLng.second},${originLatLng.first}&destination=${destinationLatLng.second},${destinationLatLng.first}&priority=RECOMMEND&mode=$carType"
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

    // 경로 응답 파싱 함수
    private fun parseDirectionsApiResponse(response: String): List<Route> {
        val routeList = mutableListOf<Route>()
        val jsonObject = JSONObject(response)
        val routes = jsonObject.getJSONArray("routes")

        Log.d("API_PARSED", "Routes count: ${routes.length()}")  // 경로 개수 확인

        for (i in 0 until routes.length()) {
            val route = routes.getJSONObject(i)

            val summary = route.optJSONObject("summary") // 요약이 있을 때만
            val distance = summary?.getString("distance") ?: "Unknown distance"
            val duration = summary?.getString("duration") ?: "Unknown duration"

            // 다른 키로 자동차 타입을 추출
            val carType = route.optString("car_type", "driving")  // 기본값 'driving'

            Log.d("API_PARSED", "Route $i: distance=$distance, duration=$duration, carType=$carType")  // 경로 정보 확인

            routeList.add(
                Route(
                    info = "경로 ${i + 1}",
                    distance = distance,
                    duration = duration,
                    carType = carType
                )
            )
        }
        return routeList
    }

    // 선택된 경로와 탄소 배출량 표시
    private fun showSelectedRouteAndCarbonEmission(route: Route) {
        val durationInSeconds = route.duration.toInt()
        val distanceInMeters = route.distance.toInt()

        // 포맷된 소요 시간과 거리
        val formattedDuration = formatDuration(durationInSeconds)
        val formattedDistance = formatDistance(distanceInMeters)

        // 탄소 배출량 계산
        val carbonEmission = calculateCarbonEmission(route.distance, route.carType)
        textViewCarbonEmission.text = "탄소 배출량: ${String.format("%.2f", carbonEmission)} kg"

        val intent = Intent(this, ConfirmActivity::class.java).apply {
            putExtra("selectedRouteInfo", route.info)
            putExtra("selectedRouteDistance", formattedDistance)  // 포맷된 거리
            putExtra("selectedRouteDuration", formattedDuration)  // 포맷된 시간
            putExtra("selectedRouteCarType", route.carType)
            putExtra("carbonEmission", carbonEmission)
        }

        startActivity(intent)
    }

    // 소요시간 변환 함수 (초 -> 시, 분으로 변환)
    private fun formatDuration(durationInSeconds: Int): String {
        val hours = durationInSeconds / 3600
        val minutes = (durationInSeconds % 3600) / 60
        return if (hours > 0) {
            String.format("%d시간 %d분", hours, minutes)
        } else {
            String.format("%d분", minutes)
        }
    }

    // 거리 변환 함수 (미터 -> 킬로미터로 변환)
    private fun formatDistance(distanceInMeters: Int): String {
        return if (distanceInMeters >= 1000) {
            val distanceInKm = distanceInMeters / 1000.0
            String.format("%.1f km", distanceInKm)
        } else {
            String.format("%d m", distanceInMeters)
        }
    }


    // 친환경 대안 보기
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



}