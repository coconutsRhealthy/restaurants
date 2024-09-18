package com.lennart.restaurants.analysis;

import java.sql.*;
import java.util.Date;
import java.util.*;

public class BasicDataRetriever {

    public static void main(String[] args) throws Exception {
        BasicDataRetriever.getReviewDataForAllRestaurants();
    }

    public static Map<String, Map<Date, Integer>> getReviewDataForAllRestaurants() throws Exception {
        List<String> allRestaurants = getAllRestaurantNames();
        Map<String,  Map<Date, Integer>> reviewDataPerRestaurant = new HashMap<>();

        for (String restaurantName : allRestaurants) {
            String query = "SELECT rr.date AS review_date, rr.reviews AS review_count "
                    + "FROM ratings_amsterdam rr "
                    + "JOIN restaurants_amsterdam r ON rr.restaurant_id = r.id "
                    + "WHERE r.name = ? "
                    + "ORDER BY rr.date ASC";

            try (Connection con = getConnection();
                 PreparedStatement stmt = con.prepareStatement(query)) {

                stmt.setString(1, restaurantName);

                try (ResultSet rs = stmt.executeQuery()) {
                    Map<Date, Integer> reviewsByDate = new TreeMap<>();

                    while (rs.next()) {
                        Date reviewDate = rs.getDate("review_date");
                        int reviewCount = rs.getInt("review_count");
                        reviewsByDate.put(reviewDate, reviewCount);
                    }

                    reviewDataPerRestaurant.put(restaurantName, reviewsByDate);
                }

            } catch (SQLException e) {
                System.out.println("SQL Error while fetching review data for restaurant: " + restaurantName);
                e.printStackTrace();
            }
        }

        return reviewDataPerRestaurant;
    }

    public static List<String> getAllRestaurantNames() throws Exception {
        String query = "SELECT r.name AS restaurant_name FROM restaurants_amsterdam r ORDER BY r.name ASC";
        List<String> restaurantNames = new ArrayList<>();

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String restaurantName = rs.getString("restaurant_name");
                restaurantNames.add(restaurantName);
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching restaurant names.");
            e.printStackTrace();
        }

        return restaurantNames;
    }

    private static Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurants?&serverTimezone=UTC", "root", "");
    }
}
