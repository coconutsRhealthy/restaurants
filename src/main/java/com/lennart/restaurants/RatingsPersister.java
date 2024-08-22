package com.lennart.restaurants;

import java.sql.*;
import java.util.Map;

public class RatingsPersister {

    public static void main(String[] args) throws Exception {
        new RatingsPersister().fillRatingsTable();
    }

    private void fillRatingsTable() throws Exception {
        Date date = Date.valueOf("2024-08-20");
        Map<String, Map<String, Object>> allRestaurantData = new RestaurantsJsonReader().getAllRestaurantDataFromJson();
        allRestaurantData.values().forEach(data -> addRatingData(data, date));
    }

    private void addRatingData(Map<String, Object> restaurantData, Date date) {
        String name = (String) restaurantData.get("name");
        String address = (String) restaurantData.get("address");

        String selectQuery = "SELECT id FROM restaurants_amsterdam WHERE name = ? AND address = ?";
        String insertQuery = "INSERT INTO ratings_amsterdam (restaurant_id, date, reviews, rating) VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement selectStmt = con.prepareStatement(selectQuery)) {
            selectStmt.setString(1, name);
            selectStmt.setString(2, address);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    Integer reviews = restaurantData.get("amount_of_reviews") != null ?
                            (Integer) restaurantData.get("amount_of_reviews") : null;
                    Double rating = restaurantData.get("rating") != null ?
                            (Double) restaurantData.get("rating") : null;

                    try (PreparedStatement insertStmt = con.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, id);
                        insertStmt.setDate(2, date);
                        insertStmt.setObject(3, reviews, java.sql.Types.INTEGER);
                        insertStmt.setObject(4, rating, java.sql.Types.DOUBLE);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("SQL Error in persisting new rating data");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Error in persisting new rating data");
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurants?&serverTimezone=UTC", "root", "");
    }
}
