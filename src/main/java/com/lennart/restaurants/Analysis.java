package com.lennart.restaurants;

import java.sql.*;

public class Analysis {

    public static void main(String[] args) throws Exception {
        new Analysis().printRatingDifferences(Date.valueOf("2024-08-19"), Date.valueOf("2024-08-20"));
    }

    public void printReviewsDifference(Date date1, Date date2) throws Exception {
        String query = "SELECT "
                + "r.name AS restaurant_name, "
                + "r1.date AS date1, "
                + "r1.reviews AS reviews_date1, "
                + "r1.rating AS rating_date1, "
                + "r2.date AS date2, "
                + "r2.reviews AS reviews_date2, "
                + "r2.rating AS rating_date2, "
                + "ABS(r1.reviews - r2.reviews) AS review_difference "
                + "FROM ratings_amsterdam r1 "
                + "JOIN ratings_amsterdam r2 ON r1.restaurant_id = r2.restaurant_id "
                + "JOIN restaurants_amsterdam r ON r1.restaurant_id = r.id "
                + "WHERE r1.date = ? AND r2.date = ? "
                + "AND r1.reviews <> r2.reviews "
                + "ORDER BY review_difference DESC";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setDate(1, date1);
            stmt.setDate(2, date2);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-60s %-10s %-10s %-10s %-10s %-10s%n",
                        "Restaurant Name", "Rev_1", "Rev_2", "Diff", "Rat_1", "Rat_2");

                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    restaurantName = removeNonWesternCharacters(restaurantName); // Remove non-Western characters
                    int reviewsDate1 = rs.getInt("reviews_date1");
                    int reviewsDate2 = rs.getInt("reviews_date2");
                    int reviewDifference = rs.getInt("review_difference");
                    double ratingDate1 = rs.getDouble("rating_date1");
                    double ratingDate2 = rs.getDouble("rating_date2");

                    System.out.printf("%-60s %-10d %-10d %-10d %-10.1f %-10.1f%n",
                            restaurantName,
                            reviewsDate1,
                            reviewsDate2,
                            reviewDifference,
                            ratingDate1,
                            ratingDate2);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching rating differences.");
            e.printStackTrace();
        }
    }





    // Method to print rating differences with formatted output
    public void printRatingDifferences(Date date1, Date date2) throws Exception {
        String query = "SELECT "
                + "r.name AS restaurant_name, "
                + "r1.date AS date1, "
                + "r1.reviews AS reviews_date1, "
                + "r1.rating AS rating_date1, "
                + "r2.date AS date2, "
                + "r2.reviews AS reviews_date2, "
                + "r2.rating AS rating_date2, "
                + "ABS(r1.rating - r2.rating) AS rating_difference "
                + "FROM ratings_amsterdam r1 "
                + "JOIN ratings_amsterdam r2 ON r1.restaurant_id = r2.restaurant_id "
                + "JOIN restaurants_amsterdam r ON r1.restaurant_id = r.id "
                + "WHERE r1.date = ? AND r2.date = ? "
                + "AND r1.rating <> r2.rating "
                + "ORDER BY rating_difference DESC";

        // Use try-with-resources to manage resources
        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            // Set parameters for the query
            stmt.setDate(1, date1);
            stmt.setDate(2, date2);

            // Execute the query and retrieve results
            try (ResultSet rs = stmt.executeQuery()) {
                // Print header
                System.out.printf("%-60s %-10s %-10s %-10s %-10s %-10s%n",
                        "Restaurant Name", "Rev_1", "Rev_2", "Diff", "Rat_1", "Rat_2");

                // Print results
                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    restaurantName = removeNonWesternCharacters(restaurantName); // Remove non-Western characters
                    int reviewsDate1 = rs.getInt("reviews_date1");
                    int reviewsDate2 = rs.getInt("reviews_date2");
                    double ratingDate1 = rs.getDouble("rating_date1");
                    double ratingDate2 = rs.getDouble("rating_date2");
                    double ratingDifference = rs.getDouble("rating_difference");

                    // Print the results with formatting
                    System.out.printf("%-60s %-10d %-10d %-10.1f %-10.1f %-10.1f%n",
                            restaurantName,
                            reviewsDate1,
                            reviewsDate2,
                            ratingDifference,
                            ratingDate1,
                            ratingDate2);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching rating differences.");
            e.printStackTrace();
        }
    }



    private String removeNonWesternCharacters(String input) {
        return input.replaceAll("[^\\p{ASCII}]", "");
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurants?&serverTimezone=UTC", "root", "");
    }
}
