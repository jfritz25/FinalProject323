package jam.fritz.finalproject323

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
/*
Below class is our adapter for the all restaurants section, works as adapters should work, displays a list of Restaurants
 */
class AllRestaurantsAdapter(private var allRestaurants: List<Restaurant>) : RecyclerView.Adapter<AllRestaurantsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_all_restaurants, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = allRestaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount() = allRestaurants.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurant_name)

        fun bind(restaurant: Restaurant) {
            restaurantName.text = restaurant.name
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newAllRestaurants: List<Restaurant>) {
        allRestaurants = newAllRestaurants
        notifyDataSetChanged()
    }
}

