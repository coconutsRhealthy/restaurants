package com.lennart.restaurants;

import java.sql.*;
import java.util.Map;

public class RestaurantsPersister {

    public static void main(String[] args) throws Exception {
        new RestaurantsPersister().fillRestaurantsTable();
    }

    private void fillRestaurantsTable() throws Exception {
        Map<String, Map<String, Object>> allRestaurantData = new RestaurantsJsonReader().getAllRestaurantDataFromJson();
        allRestaurantData.values().forEach(this::addRestaurantIfNotPresent);
    }

    private void addRestaurantIfNotPresent(Map<String, Object> restaurantData) {
        String name = (String) restaurantData.get("name");
        String type = (String) restaurantData.get("type");
        String area = (String) restaurantData.get("area");
        String address = (String) restaurantData.get("address");

        try (Connection con = getConnection();
             PreparedStatement checkStmt = con.prepareStatement(
                     "SELECT * FROM restaurants_amsterdam WHERE name = ? AND address = ?");
             PreparedStatement insertStmt = con.prepareStatement(
                     "INSERT INTO restaurants_amsterdam (name, type, area, address) VALUES (?, ?, ?, ?)")) {

            checkStmt.setString(1, name);
            checkStmt.setString(2, address);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    insertStmt.setString(1, name);
                    insertStmt.setString(2, type);
                    insertStmt.setString(3, area);
                    insertStmt.setString(4, address);

                    int rowsInserted = insertStmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("A new restaurant was inserted successfully!");
                    }
                } else {
                    System.out.println("Restaurant already exists in the database.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error in persisting new data");
            e.printStackTrace();
        }
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurants?&serverTimezone=UTC", "root", "");
    }
}
