package tk.ardentbot.rethink.models

import tk.ardentbot.core.executor.BaseCommand
import tk.ardentbot.rethink.Database.connection
import tk.ardentbot.rethink.Database.r

data class Food(val food_id: String, val restaurant_id: String, val gameUsesLeft: Int) {
    fun getRestaurant(): RestaurantModel {
        return BaseCommand.asPojo(r.table("restaurants").get(food_id).run(connection), RestaurantModel::class.java)
    }
}