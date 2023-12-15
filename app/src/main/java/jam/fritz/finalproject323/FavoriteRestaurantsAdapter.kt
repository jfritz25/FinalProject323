package jam.fritz.finalproject323

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//Adapter for the favorite restaurants, displays a list of Restaurants
class FavoriteRestaurantsAdapter(private var favoriteRestaurants: List<Restaurant>) : RecyclerView.Adapter<FavoriteRestaurantsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rv_favorite_restaurants, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val restaurant = favoriteRestaurants[position]
        holder.bind(restaurant)
    }

    override fun getItemCount() = favoriteRestaurants.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val restaurantName: TextView = itemView.findViewById(R.id.restaurant_name)

        fun bind(restaurant: Restaurant) {
            restaurantName.text = restaurant.name
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newFavoriteRestaurants: List<Restaurant>) {
        favoriteRestaurants = newFavoriteRestaurants
        notifyDataSetChanged()
    }

}
