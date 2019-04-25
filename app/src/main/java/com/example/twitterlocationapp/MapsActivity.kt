package com.example.twitterlocationapp

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import android.location.LocationManager
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.*
import org.json.JSONObject
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private var longitude: Double = .0
    private var latitude: Double = .0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        getCurrentLocation()
        getTweetsByLocation()

        mapFragment.getMapAsync(this)
    }

    private fun getTweetsByLocation() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.twitter.com/1.1/search/tweets.json?geocode=${latitude},${longitude},5km&result_type=mixed&count=100"

        val stringRequest = object : StringRequest(Request.Method.GET, url,
                Response.Listener<String> { response ->
                    val jsonObj = JSONObject(response.toString())
                    val jsonArray = jsonObj.getJSONArray("statuses")

                    for (i in 0 until jsonArray.length()) {
                        val statuses = jsonArray.getJSONObject(i)

                        val user = statuses.getJSONObject("user")

                        val userName =  user.getString("name")
                        val tweet = statuses.getString("text")

                        if (statuses.get("coordinates").toString() != "null") {
                            val c = statuses.getJSONObject("coordinates").getJSONArray("coordinates")

                            val latLng = LatLng(c.getDouble(1), c.getDouble(0))
                            mMap.addMarker(MarkerOptions().position(latLng).title("by $userName").snippet(tweet))
                        }

                        if (statuses.get("place").toString() != "null") {
                            val place = statuses.getJSONObject("place")

                            val box = place.getJSONObject("bounding_box")
                            val c = box.getJSONArray("coordinates").getJSONArray(0).getJSONArray(0)

                            val latLng = LatLng(c.getDouble(1), c.getDouble(0))
                            mMap.addMarker(MarkerOptions().position(latLng).title("by $userName").snippet(tweet))
                        }
                    }
                },
                Response.ErrorListener { Toast.makeText(this, "That didn't work!", Toast.LENGTH_SHORT).show() }) {

            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["authorization"] = "Bearer ${getString(R.string.twitter_bearer)}"
                return params
            }
        }

        queue.add(stringRequest)
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

        longitude = location.longitude
        latitude = location.latitude
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val currentLocation = LatLng(latitude, longitude)

        val location = CameraUpdateFactory.newLatLngZoom(currentLocation, 12.0f)
        mMap.animateCamera(location)

        mMap.isMyLocationEnabled = true
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Current location:\n" + p0, Toast.LENGTH_LONG).show()
    }
}
