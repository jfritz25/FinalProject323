package jam.fritz.finalproject323

import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

/*
All Restaurants Fragment
 */
class AllRestaurantsFragment : Fragment() {
    private var restaurants = mutableListOf<Restaurant>()
    private lateinit var fusedLocationClient: FusedLocationProviderClient //Actively gets location
    private var userLat: Double = 37.422 //Initalizes values for lat and long (they get overwritten)
    private var userLong: Double = -122.085
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getLastLocation()
        } else {
        }
    }
    private var adapter= AllRestaurantsAdapter(mutableListOf())



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation() //When we have the permissions we get the last location
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

    }
    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation //Gets the location
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    userLat = it.latitude
                    userLong = it.longitude
                }
            }

        val db = FirebaseFirestore.getInstance()
        db.collection("Restaurants")//Gets data from Restaurants collection on firebase
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val location = document.getGeoPoint("Location")
                    if (location != null) {
                        val distance = calculateDistance( //Computes distance of lat long points
                            userLat,
                            userLong,
                            location.latitude,
                            location.longitude
                        )
                        if (distance <= 50) { //Gets only the close restaurants
                            val name = document.getString("Name")!!
                            val items = document.get("Items") as List<String>
                            val prices = document.get("Prices") as List<String>
                            val userFav = document.get("userFav") as List<String>
                            val loc = location

                            val res = Restaurant(name, items, prices, loc, userFav) //Creates instance of restaurant data class
                            restaurants.add(res)
                        }
                    }
                }
                activity?.runOnUiThread{
                    adapter.updateData(restaurants) //Updates UI
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }



    }
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 3963 // radius of earth in mi
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(latDistance / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(lonDistance / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_all_restaurants, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Bind the recycler views
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_all)
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }



}
