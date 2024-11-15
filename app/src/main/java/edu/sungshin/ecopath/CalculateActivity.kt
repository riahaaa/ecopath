package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var spinnerTransportMode: Spinner
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
        spinnerTransportMode = findViewById(R.id.spinnerTransportMode)
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
        recyclerViewOriginResults.layoutManager = LinearLayoutManager(this)
        recyclerViewOriginResults.setHasFixedSize(true)

        recyclerViewDestinationResults.layoutManager = LinearLayoutManager(this)
        recyclerViewDestinationResults.setHasFixedSize(true)

        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        recyclerViewRoutes.setHasFixedSize(true)

        // Spinner
        val transportModes = arrayOf("자동차", "대중교통", "전기차", "하이브리드")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, transportModes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTransportMode.adapter = adapter

        // 출발지와 목적지 검색 UI 설정
        setupSearchButton(buttonOriginSearch, editTextOrigin) { query ->
            if (query.isNotEmpty()) {
                searchPlaceWithKakao(query, isOrigin = true) { latLng ->
                    originLatLng = latLng
                    Log.d("CalculateActivity", "출발지 좌표: $originLatLng")
                }
            }
        }

        setupSearchButton(buttonDestinationSearch, editTextDestination) { query ->
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
                val selectedTransportMode = spinnerTransportMode.selectedItem.toString()  // 선택된 이동수단
                fetchRoutesFromAPI(selectedTransportMode)
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

                        withContext(Dispatchers.Main) {
                            if (places.isNotEmpty()) {
                                setupRecyclerView(
                                    if (isOrigin) recyclerViewOriginResults else recyclerViewDestinationResults,
                                    places,
                                    isOrigin
                                )
                            } else {
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
        }
        recyclerView.adapter = adapter
    }

    // 검색 버튼 클릭 리스너 설정
    private fun setupSearchButton(button: Button, editText: EditText, searchCallback: (String) -> Unit) {
        button.setOnClickListener {
            val query = editText.text.toString().trim()
            if (query.isNotEmpty()) {
                searchCallback(query)  // 입력된 검색어로 콜백 실행
            } else {
                Toast.makeText(this, "검색어를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 경로를 가져오는 API 호출 함수
    private fun fetchRoutesFromAPI(selectedTransportMode: String) {
        val originLatLng = this.originLatLng
        val destinationLatLng = this.destinationLatLng

        if (originLatLng != null && destinationLatLng != null) {
            val travelMode = when (selectedTransportMode) {
                "대중교통" -> "transit"
                "전기차" -> "electric driving"
                "하이브리드" -> "hybrid driving"
                else -> "driving"  // 기본적으로 자동차
            }

            val url = "https://apis-navi.kakaomobility.com/v1/directions?origin=${originLatLng.second},${originLatLng.first}&destination=${destinationLatLng.second},${destinationLatLng.first}&priority=RECOMMEND&mode=$travelMode"
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

        for (i in 0 until routes.length()) {
            val route = routes.getJSONObject(i)
            val summary = route.getJSONObject("summary")

            val distance = summary.getString("distance")  // distance는 직접 String으로 받기
            val duration = summary.getString("duration")
            val travelMode = if (route.has("mode")) {
                route.getString("mode")
            } else {
                "driving"  // 기본값을 "driving" (자동차)로 설정
            }

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

    // 선택된 경로와 탄소 배출량 표시
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