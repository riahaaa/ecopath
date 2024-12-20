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
    private lateinit var buttonCalculateEmission: Button


    val KakaoApiKey = BuildConfig.KAKAO_API_KEY


    private var originLatLng: Pair<Double, Double>? = null
    private var destinationLatLng: Pair<Double, Double>? = null
    private var selectedRoute: Route? = null

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
        buttonCalculateEmission = findViewById(R.id.buttonCalculateEmission)
        buttonCalculateEmission.visibility = View.GONE // 초기에는 숨김 처리

        // RecyclerView 설정
        recyclerViewOriginResults.layoutManager = GridLayoutManager(this, 1)
        recyclerViewOriginResults.setHasFixedSize(true)

        recyclerViewDestinationResults.layoutManager = GridLayoutManager(this, 1)
        recyclerViewDestinationResults.setHasFixedSize(true)


        recyclerViewRoutes.layoutManager = GridLayoutManager(this,1)
        recyclerViewRoutes.setHasFixedSize(true)

        // Spinner
        val carType = arrayOf("휘발유/디젤 차", "전기차", "하이브리드차")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, carType)
        spinnerCarType.adapter = adapter
        spinnerCarType.setVisibility(View.GONE)  // 초기에는 자동차 종류 스피너 숨기기

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
                Log.d("DEBUG", "Selected Car Type: $selectedCarType")
                fetchRoutesFromAPI(selectedCarType)
            }
        }

        // 정보 버튼 클릭 리스너
        buttoninformation.setOnClickListener {
            val intent = Intent(this, CalculateInformationActivity::class.java)
            startActivity(intent)
        }

        // 탄소 배출량 계산 버튼 클릭 리스너
        buttonCalculateEmission.setOnClickListener {
            selectedRoute?.let { route ->
                // Spinner에서 선택된 자동차 타입 가져오기
                val selectedCarType = spinnerCarType.selectedItem.toString()
                // 탄소 배출량 계산
                val carbonEmission = calculateCarbonEmission(route.distance, selectedCarType)
                textViewCarbonEmission.visibility = View.VISIBLE
                textViewCarbonEmission.text = "탄소 배출량: ${String.format("%.2f", carbonEmission)} kgCo2"
                buttoninformation.visibility = View.VISIBLE // 대안 제시 버튼 보이기
            }
        }
    }

    // 장소 검색 함수
    private fun searchPlaceWithKakao(query: String, isOrigin: Boolean, callback: (Pair<Double, Double>?) -> Unit) {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val url = "https://dapi.kakao.com/v2/local/search/keyword.json?query=$encodedQuery"
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

                        // 검색 결과가 없으면 '결과 없음'
                        if (places.isEmpty()) {
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
            val carType = "driving"

            val url = "https://apis-navi.kakaomobility.com/v1/directions?"+
                    "origin=${originLatLng.second},${originLatLng.first}" +
                    "&destination=${destinationLatLng.second},${destinationLatLng.first}" +
                    "&priority=RECOMMEND&mode=$carType"
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
                                    this@CalculateActivity.selectedRoute = selectedRoute
                                    spinnerCarType.visibility = View.VISIBLE
                                    buttonCalculateEmission.visibility = View.VISIBLE
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalculateActivity", "Error fetching route", e)
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

            routeList.add(
                Route(
                    info = "경로 ${i + 1}",
                    distance = distance,
                    duration = duration
                )
            )
        }
        return routeList
    }

    // 탄소 배출 계산 공식
    private fun calculateCarbonEmission(distanceText: String, carType: String): Double {
        val distanceInKm = try {
            distanceText.split(" ")[0].replace(",", "").toDouble()
        } catch (e: Exception) {
            Log.e("CalculateActivity", "Error parsing distance: $distanceText", e)
            0.0
        }

        val emissionFactor = when (carType) {
            "휘발유/디젤 차" -> 0.18
            "전기차" -> 0.05
            "하이브리드차" -> 0.10
            else -> 0.18
        }
        return distanceInKm * emissionFactor
    }
}