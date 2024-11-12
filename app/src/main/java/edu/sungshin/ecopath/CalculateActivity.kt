package edu.sungshin.ecopath

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class CalculateActivity : AppCompatActivity() {

    private lateinit var buttonSearch: Button
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var textViewCarbonEmission: TextView
    private lateinit var buttonEcoAlternative: Button
    val apiKey = BuildConfig.MAPS_API_KEY

    private var originLatLng: LatLng? = null
    private var destinationLatLng: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        Log.d("CalculateActivity", "API Key: $apiKey")

        // 초기화
        buttonSearch = findViewById(R.id.buttonSearch)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        textViewCarbonEmission = findViewById(R.id.textViewCarbonEmission)
        buttonEcoAlternative = findViewById(R.id.buttonEcoAlternative)

        setupAutocompleteFragment(R.id.fragment_autocomplete_origin, isOrigin = true)
        setupAutocompleteFragment(R.id.fragment_autocomplete_destination, isOrigin = false)

        // RecyclerView 설정
        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        recyclerViewRoutes.setHasFixedSize(true)

        // Google Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // 검색 버튼 클릭 리스너
        buttonSearch.setOnClickListener {
            if (originLatLng != null && destinationLatLng != null) {
                fetchRoutesFromAPI()
            } else {
                Toast.makeText(this, "출발지와 목적지를 선택해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 친환경 대안 보기 버튼 클릭 리스너
        buttonEcoAlternative.setOnClickListener {
            showEcoAlternatives()
        }
    }

    private fun setupAutocompleteFragment(fragmentId: Int, isOrigin: Boolean) {
        // AutocompleteSupportFragment를 설정할 레이아웃에 추가
        val autocompleteFragment = supportFragmentManager
            .findFragmentById(fragmentId) as? AutocompleteSupportFragment

        autocompleteFragment?.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG))
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (isOrigin) {
                    originLatLng = place.latLng
                } else {
                    destinationLatLng = place.latLng
                }
                Log.d("CalculateActivity", "Place selected: ${place.name}, Location: ${place.latLng}")
            }

            override fun onError(status: Status) {
                Log.e("CalculateActivity", "An error occurred: $status")
            }
        })
    }

    private fun fetchRoutesFromAPI() {
        val originLatLng = this.originLatLng
        val destinationLatLng = this.destinationLatLng

        if (originLatLng != null && destinationLatLng != null) {
            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=${originLatLng.latitude},${originLatLng.longitude}&destination=${destinationLatLng.latitude},${destinationLatLng.longitude}&alternatives=true&key=$apiKey"

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = URL(url).readText()
                    val routes = parseDirectionsApiResponse(response)

                    withContext(Dispatchers.Main) {
                        if (routes.isNotEmpty()) {
                            recyclerViewRoutes.adapter = RouteAdapter(routes) { selectedRoute ->
                                showSelectedRouteAndCarbonEmission(selectedRoute)
                            }
                        } else {
                            Toast.makeText(this@CalculateActivity, "경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
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
            val legs = route.getJSONArray("legs").getJSONObject(0)

            val distance = legs.getJSONObject("distance").getString("text")
            val duration = legs.getJSONObject("duration").getString("text")
            val travelMode = route.getString("summary")

            routeList.add(
                Route(
                    info = route.getString("summary"),
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
