package edu.sungshin.ecopath

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.Status
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

class CalculateActivity: AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    private lateinit var editTextOrigin: EditText
    private lateinit var editTextDestination: EditText
    private lateinit var buttonSearch: Button
    private lateinit var recyclerViewRoutes: RecyclerView
    private lateinit var textViewCarbonEmission: TextView
    private lateinit var buttonEcoAlternative: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculate)

        // 초기화
        editTextOrigin = findViewById(R.id.editTextOrigin)
        editTextDestination = findViewById(R.id.editTextDestination)
        buttonSearch = findViewById(R.id.buttonSearch)
        recyclerViewRoutes = findViewById(R.id.recyclerViewRoutes)
        textViewCarbonEmission = findViewById(R.id.textViewCarbonEmission)
        buttonEcoAlternative = findViewById(R.id.buttonEcoAlternative)

        // 위치 권한 확인 및 요청
        checkLocationPermission()

        // RecyclerView 설정
        recyclerViewRoutes.layoutManager = LinearLayoutManager(this)
        recyclerViewRoutes.setHasFixedSize(true)

        // Google Places API 초기화
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "apikeys")  // 실제 API 키로 교체
        }

        // 검색 버튼 클릭 리스너
        buttonSearch.setOnClickListener {
            val origin = editTextOrigin.text.toString()
            val destination = editTextDestination.text.toString()

            if (origin.isNotEmpty() && destination.isNotEmpty()) {
                fetchRoutesFromAPI(origin, destination)
            } else {
                Toast.makeText(this, "출발지와 목적지를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // 친환경 대안 보기 버튼 클릭 리스너
        buttonEcoAlternative.setOnClickListener {
            showEcoAlternatives()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "위치 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchRoutesFromAPI(origin: String, destination: String) {
        val apiKey = "apikeys"  // 실제 API 키로 교체
        val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=${origin.replace(" ", "+")}&destination=${destination.replace(" ", "+")}&alternatives=true&key=$apiKey"

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

    private fun parseDirectionsApiResponse(response: String): List<Route> {
        val routeList = mutableListOf<Route>()
        val jsonObject = JSONObject(response)
        val routes = jsonObject.getJSONArray("routes")

        for (i in 0 until routes.length()) {
            val route = routes.getJSONObject(i)
            val legs = route.getJSONArray("legs").getJSONObject(0)

            val distance = legs.getJSONObject("distance").getString("text")
            val duration = legs.getJSONObject("duration").getString("text")
            val travelMode = route.getString("summary")  // 운전, 도보, 자전거 등

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

