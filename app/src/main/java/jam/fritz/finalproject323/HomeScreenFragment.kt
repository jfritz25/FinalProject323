package jam.fritz.finalproject323
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import org.w3c.dom.Text
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt


/*
Home Screen Fragment Class
 */
class HomeScreenFragment : Fragment() {

    private lateinit var drawerLayout: DrawerLayout //For the navigation drawer
    private lateinit var toggle: ActionBarDrawerToggle // For the nav drawer
    private lateinit var searchView: androidx.appcompat.widget.SearchView // Search bar
    private var adapter = AllRestaurantsAdapter(mutableListOf())
    private var favAdapter = FavoriteRestaurantsAdapter(mutableListOf())
    private val user = FirebaseAuth.getInstance().currentUser
    private lateinit var fusedLocationClient: FusedLocationProviderClient //Location
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (isGranted) {
            getLastLocation()
        } else {
        }
    }
    private var userLat: Double = 0.0 //Initalizes location
    private var userLong: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
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
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    userLat = it.latitude
                    userLong = it.longitude
                }
            }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_homescreen, container, false)
        drawerLayout = view.findViewById(R.id.drawerLayout)
        val navigationView: NavigationView = view.findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(activity, drawerLayout, R.string.open, R.string.close) //Toggles for nav drawer
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    drawerLayout.closeDrawers() //Closes drawer when home button is selected
                }

                R.id.nav_recent_orders -> {
                    //Would navigate to recent orders
                }

                R.id.nav_sign_out -> { //Signs user out
                    val auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                }
            }
            drawerLayout.closeDrawer(Gravity.LEFT, true)
            true
        }

        val signOutItem = navigationView.menu.findItem(R.id.nav_sign_out)
        val actionView = signOutItem.actionView
        val params = actionView?.layoutParams
        params?.width = ViewGroup.LayoutParams.MATCH_PARENT
        actionView?.layoutParams = params

        actionView?.setOnClickListener {//Also signs user out
            val auth = FirebaseAuth.getInstance()
            auth.signOut()
            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
        }

        val fragment1 = FavoriteRestaurantsFragment()
        val fragment2 = AllRestaurantsFragment()

        childFragmentManager.beginTransaction().apply {//Creates and inserts favorite and all fragments into their container views
            replace(R.id.fragment_container_all, fragment2)
            replace(R.id.fragment_container_favorite, fragment1)
            commit()
        }

        val searchButton: ImageButton = view.findViewById(R.id.searchButton)
        searchView = view.findViewById(R.id.searchView)

        // Makes search view visible when search image button pressed
        searchButton.setOnClickListener {
            searchView.visibility = View.VISIBLE
        }
        //When search view is closed, reset data
        searchView.setOnCloseListener{
            val db = FirebaseFirestore.getInstance()
            db.collection("Restaurants").addSnapshotListener {snapshot, e ->
                val listResAll = ArrayList<Restaurant>()
                val listResFav = ArrayList<Restaurant>()
                for (doc in snapshot!!) {
                    val location = doc.getGeoPoint("Location")
                    val distance = location?.let {
                        calculateDistance(
                            userLat,
                            userLong,
                            it.latitude,
                            it.longitude
                        )
                    }
                    if (distance != null) {
                        if (distance <= 50) {
                            //Need to check if it actually is part of the text
                            val name = doc.getString("Name")!!
                            val items = doc.get("Items") as List<String>
                            val prices = doc.get("Prices") as List<String>
                            val userFav = doc.get("userFav") as List<String>
                            val loc = doc.get("Location") as GeoPoint
                            val res = Restaurant(name, items, prices, loc, userFav)
                            listResAll.add(res)
                            if (user!!.email in userFav) {
                                listResFav.add(res)
                            }
                        }
                    }
                }

                updateListRes(listResAll,listResFav)

            }
            true
        }

        // Calls database query when search is submitted
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                Log.d("SearchView", "onQueryTextSubmit called with query: $query")
                searchRes(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })


        return view
    }

    //Search result builder based on query
    fun searchRes(query: String) {
        val db = FirebaseFirestore.getInstance()
        val searchQuery = if (query.isNotEmpty()) query.substring(0, 1).toUpperCase() + query.substring(1).toLowerCase() else query

        db.collection("Restaurants").whereEqualTo("Name", searchQuery)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("Search", "Listen failed.", e)
                    return@addSnapshotListener
                }

                val listRes = ArrayList<Restaurant>()
                for (doc in snapshots!!) {
                    //Need to check if it actually is part of the text
                    val name = doc.getString("Name")!!
                    val items = doc.get("Items") as List<String>
                    val prices = doc.get("Prices") as List<String>
                    val userFav = doc.get("userFav") as List<String>
                    val loc = doc.get("Location") as GeoPoint
                    val res = Restaurant(name, items, prices, loc, userFav)
                    listRes.add(res)
                }

                updateListRes(listRes)
            }
    }
    //Updates UI after search
    fun updateListRes(listResAll: List<Restaurant>, listResFav: List<Restaurant> = listResAll) {
        val rvAll = view?.findViewById<RecyclerView>(R.id.recycler_view_all)
        val rvFav = view?.findViewById<RecyclerView>(R.id.recycler_view_fav)
        rvAll?.adapter = adapter
        rvFav?.adapter = favAdapter
        adapter.updateData(listResAll)
        favAdapter.updateData(listResFav)
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //Adds and configures nav drawer
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val button: ImageButton = view.findViewById(R.id.menuButton)
        button.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }




        val db = FirebaseFirestore.getInstance()
        val im = FirebaseStorage.getInstance()
        val id = user?.uid
        val imref = im.reference.child("profileImages/$id/$id")
        val ref = db.collection("users")
        ref.document(user?.uid.toString()).get().addOnCompleteListener { task -> //Uploads user info to nav drawer
            if (task.isSuccessful) {
                val name = drawerLayout.findViewById<TextView>(R.id.name)
                val email = drawerLayout.findViewById<TextView>(R.id.email)
                val pfp = drawerLayout.findViewById<ImageButton>(R.id.profile_image)
                val document = task.result
                if (document != null && document.exists()) {
                    imref.downloadUrl.addOnSuccessListener { uri ->
                        print(uri)
                        Glide.with(requireContext())
                            .load(uri)
                            .into(pfp)
                        val fname = document.getString("Name")
                        name.text = fname
                        email.text = user?.email
                    }
                }
            }
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
}








