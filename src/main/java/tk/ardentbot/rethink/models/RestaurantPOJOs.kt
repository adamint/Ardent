package tk.ardentbot.rethink.models

class RestaurantModel(val owners: MutableList<String>, val food_items: MutableList<RestaurantFoodItem>)

class RestaurantFoodItem(val restaurant_id: String, val sell_cost: String, val production_cost: String, val timeToMake: Int)