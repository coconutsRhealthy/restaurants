package com.lennart.restaurants;

import java.sql.*;

public class Analysis {

    public static void main(String[] args) throws Exception {
        new Analysis().printReviewAndRatingDiffs(Date.valueOf("2024-08-19"), Date.valueOf("2024-08-20"), DifferenceType.REVIEWS);
    }

    private enum DifferenceType {
        REVIEWS, RATINGS
    }

    private void printReviewAndRatingDiffs(Date date1, Date date2, DifferenceType type) throws Exception {
        String query = getQuery(type);

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setDate(1, date1);
            stmt.setDate(2, date2);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-60s %-10s %-10s %-10s %-10s %-10s %-10s%n",
                        "Restaurant Name", "Rat_1", "Rat_2", "Diff", "Rev_1", "Rev_2", "Diff");

                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    restaurantName = removeNonWesternCharacters(restaurantName);
                    int reviewsDate1 = rs.getInt("reviews_date1");
                    int reviewsDate2 = rs.getInt("reviews_date2");
                    double ratingDate1 = rs.getDouble("rating_date1");
                    double ratingDate2 = rs.getDouble("rating_date2");
                    int reviewDifference = reviewsDate2 - reviewsDate1;
                    double ratingDifference = ratingDate2 - ratingDate1;

                    System.out.printf("%-60s %-10.1f %-10.1f %-10.1f %-10d %-10d %-10d%n",
                            restaurantName,
                            ratingDate1, ratingDate2, ratingDifference,
                            reviewsDate1, reviewsDate2, reviewDifference);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching differences.");
            e.printStackTrace();
        }
    }

    private String getQuery(DifferenceType type) {
        String differenceColumn;
        String condition;

        if (type == DifferenceType.REVIEWS) {
            differenceColumn = "r2.reviews - r1.reviews AS difference";
            condition = "r1.reviews <> r2.reviews";
        } else {
            differenceColumn = "r2.rating - r1.rating AS difference";
            condition = "r1.rating <> r2.rating";
        }

        return "SELECT "
                + "r.name AS restaurant_name, "
                + "r1.date AS date1, "
                + "r1.reviews AS reviews_date1, "
                + "r1.rating AS rating_date1, "
                + "r2.date AS date2, "
                + "r2.reviews AS reviews_date2, "
                + "r2.rating AS rating_date2, "
                + differenceColumn + " "
                + "FROM ratings_amsterdam r1 "
                + "JOIN ratings_amsterdam r2 ON r1.restaurant_id = r2.restaurant_id "
                + "JOIN restaurants_amsterdam r ON r1.restaurant_id = r.id "
                + "WHERE r1.date = ? AND r2.date = ? "
                + "AND " + condition + " "
                + "ORDER BY difference DESC";
    }

    private String removeNonWesternCharacters(String input) {
        return input.replaceAll("[^\\p{ASCII}]", "");
    }

    private Connection getConnection() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurants?&serverTimezone=UTC", "root", "");
    }
}
