package com.lennart.restaurants;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class RestaurantsJsonReader {

    public static void main(String[] args) throws Exception {
        new RestaurantsJsonReader().getAllRestaurantDataFromJson();
    }

    public Map<String, Map<String, Object>> getAllRestaurantDataFromJson() throws Exception {
        Map<String, Map<String, Object>> allRestaurantData = new HashMap<>();

        JSONParser jsonParser = new JSONParser();

        JSONArray apifyData = (JSONArray) jsonParser.parse(
                new FileReader("/Users/lennartmac/Documents/restaurants/restaurants_adam_20aug.json"));

        for(Object apifyDataElement : apifyData) {
            JSONObject restaurantJson = (JSONObject) apifyDataElement;

            String uniqueKey = getUniqueKey(restaurantJson);

            if(uniqueKey != null) {
                allRestaurantData.put(uniqueKey, new HashMap<>());

                Map<String, Object> restaurantMapToFill = allRestaurantData.get(uniqueKey);

                restaurantMapToFill.put("name", getRestaurantName(restaurantJson));
                restaurantMapToFill.put("rating", getRestaurantRating(restaurantJson));
                restaurantMapToFill.put("amount_of_reviews", getAmountOfReviews(restaurantJson));
                restaurantMapToFill.put("type", getRestaurantType(restaurantJson));
                restaurantMapToFill.put("area", getRestaurantArea(restaurantJson));
                restaurantMapToFill.put("address", getRestaurantAddress(restaurantJson));
            }
        }

        return allRestaurantData;
    }

    private String getUniqueKey(JSONObject restaurantJson) {
        String restaurantName = (String) restaurantJson.get("title");
        String address = (String) restaurantJson.get("street");

        if(address == null) {
            return null;
        }

        return restaurantName + "___" + address;
    }

    private String getRestaurantName(JSONObject restaurantJson) {
        return (String) restaurantJson.get("title");
    }

    private String getRestaurantType(JSONObject restaurantJson) {
        return (String) restaurantJson.get("categoryName");
    }

    private String getRestaurantArea(JSONObject restaurantJson) {
        return (String) restaurantJson.get("neighborhood");
    }

    private String getRestaurantAddress(JSONObject restaurantJson) {
        return (String) restaurantJson.get("street");
    }

    private Double getRestaurantRating(JSONObject restaurantJson) {
        Object rating = restaurantJson.get("totalScore");

        if(rating instanceof Double) {
            return (Double) restaurantJson.get("totalScore");
        } else if(rating instanceof Long) {
            return ((Long) rating).doubleValue();
        } else {
            return null;
        }
    }

    private int getAmountOfReviews(JSONObject restaurantJson) {
        Long reviewsCountLong = (Long) restaurantJson.get("reviewsCount");
        return reviewsCountLong.intValue();
    }
}
