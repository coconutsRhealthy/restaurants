package com.lennart.restaurants;

import java.sql.*;

public class Analysis {

    public static void main(String[] args) throws Exception {
        //new Analysis().printReviewAndRatingDiffs(Date.valueOf("2024-08-23"), Date.valueOf("2024-08-28"), DifferenceType.REVIEWS);
        new Analysis().printAllRestaurants(SortBy.REVIEWS);
        //new Analysis().printDataFor("WONDR");
        //new Analysis().printFilteredRestaurants("george", SortBy.RATING);
        //new Analysis().identifyNewRestaurants(Date.valueOf("2024-08-01"), 100, 6);
    }

    private enum DifferenceType {
        REVIEWS, RATINGS
    }

    private enum SortBy {
        NAME,
        RATING,
        REVIEWS
    }

    public void identifyNewRestaurants(Date fromDate, int maxInitialReviewAmount, int minAdditionalReviews) throws Exception {
        String query = "SELECT r.name AS restaurant_name, "
                + "rr1.date AS first_date, "
                + "rr1.reviews AS first_reviews, "
                + "rr2.date AS second_date, "
                + "rr2.reviews AS second_reviews "
                + "FROM restaurants_amsterdam r "
                + "JOIN ratings_amsterdam rr1 ON r.id = rr1.restaurant_id "
                + "JOIN ratings_amsterdam rr2 ON r.id = rr2.restaurant_id "
                + "JOIN ("
                + "    SELECT restaurant_id, MIN(date) AS earliest_date "
                + "    FROM ratings_amsterdam "
                + "    WHERE reviews <= ? AND date > ? "
                + "    GROUP BY restaurant_id"
                + ") earliest_reviews ON rr1.restaurant_id = earliest_reviews.restaurant_id "
                + "                     AND rr1.date = earliest_reviews.earliest_date "
                + "WHERE rr2.date > rr1.date "
                + "  AND rr2.reviews >= rr1.reviews + ? "
                + "  AND rr1.date > ? "
                + "  AND NOT EXISTS ("
                + "      SELECT 1 FROM ratings_amsterdam rr3 "
                + "      WHERE rr3.restaurant_id = r.id "
                + "      AND rr3.date > rr1.date "
                + "      AND rr3.reviews - rr1.reviews > rr2.reviews - rr1.reviews "
                + "      AND rr3.date > rr2.date"
                + "  ) "
                + "ORDER BY rr1.reviews ASC, (rr2.reviews - rr1.reviews) DESC;";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, maxInitialReviewAmount);
            stmt.setDate(2, fromDate);
            stmt.setInt(3, minAdditionalReviews);
            stmt.setDate(4, fromDate);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-60s %-15s %-10s %-15s %-10s%n",
                        "Restaurant Name", "First Date", "Reviews (1st)", "Second Date", "Reviews (2nd)");

                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    Date firstDate = rs.getDate("first_date");
                    int firstReviews = rs.getInt("first_reviews");
                    Date secondDate = rs.getDate("second_date");
                    int secondReviews = rs.getInt("second_reviews");

                    System.out.printf("%-60s %-15s %-10d %-15s %-10d%n",
                            restaurantName, firstDate.toString(), firstReviews, secondDate.toString(), secondReviews);
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while identifying new restaurants.");
            e.printStackTrace();
        }
    }

    public void printFilteredRestaurants(String nameOrTypePart, SortBy sortBy) throws Exception {
        String query = getFilteredRestaurantsQuery(sortBy);

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, "%" + nameOrTypePart + "%");
            stmt.setString(2, "%" + nameOrTypePart + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-5s %-60s %-10s %-10s %-15s%n",
                        "#", "Restaurant Name", "Rating", "Reviews", "Most Recent Date");

                int counter = 1;

                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    restaurantName = removeNonWesternCharacters(restaurantName);
                    double rating = rs.getDouble("rating");
                    int reviews = rs.getInt("reviews");
                    Date mostRecentDate = rs.getDate("most_recent_date");

                    System.out.printf("%-5d %-60s %-10.1f %-10d %-15s%n",
                            counter, restaurantName, rating, reviews, mostRecentDate.toString());

                    counter++;
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching filtered restaurant data.");
            e.printStackTrace();
        }
    }

    private String getFilteredRestaurantsQuery(SortBy sortBy) {
        String orderByClause = switch (sortBy) {
            case NAME -> "ORDER BY r.name ASC";
            case RATING -> "ORDER BY rr.rating DESC, rr.reviews DESC";
            case REVIEWS -> "ORDER BY rr.reviews DESC";
        };

        return "SELECT r.name AS restaurant_name, rr.rating, rr.reviews, rr.date AS most_recent_date "
                + "FROM restaurants_amsterdam r "
                + "JOIN ratings_amsterdam rr ON r.id = rr.restaurant_id "
                + "WHERE rr.date = (SELECT MAX(rr2.date) "
                + "                FROM ratings_amsterdam rr2 "
                + "                WHERE rr2.restaurant_id = rr.restaurant_id) "
                + "AND (r.name LIKE ? OR r.type LIKE ?) "
                + orderByClause;
    }

    public void printAllRestaurants(SortBy sortBy) throws Exception {
        String query = getAllRestaurantsQuery(sortBy);

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-5s %-60s %-10s %-10s %-15s%n",
                        "#", "Restaurant Name", "Rating", "Reviews", "Most Recent Date");

                int counter = 1;

                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    restaurantName = removeNonWesternCharacters(restaurantName);
                    double rating = rs.getDouble("rating");
                    int reviews = rs.getInt("reviews");
                    Date mostRecentDate = rs.getDate("most_recent_date");

                    System.out.printf("%-5d %-60s %-10.1f %-10d %-15s%n",
                            counter, restaurantName, rating, reviews, mostRecentDate.toString());

                    counter++;
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching most recent ratings.");
            e.printStackTrace();
        }
    }

    private String getAllRestaurantsQuery(SortBy sortBy) {
        String orderByClause = switch (sortBy) {
            case NAME -> "ORDER BY r.name ASC";
            case RATING -> "ORDER BY rr.rating DESC, rr.reviews DESC";
            case REVIEWS -> "ORDER BY rr.reviews DESC";
        };

        return "SELECT r.name AS restaurant_name, rr.rating, rr.reviews, rr.date AS most_recent_date "
                + "FROM restaurants_amsterdam r "
                + "JOIN ratings_amsterdam rr ON r.id = rr.restaurant_id "
                + "WHERE rr.date = (SELECT MAX(rr2.date) "
                + "                FROM ratings_amsterdam rr2 "
                + "                WHERE rr2.restaurant_id = rr.restaurant_id) "
                + orderByClause;
    }

    public void printDataFor(String namePart) throws Exception {
        String query = "SELECT r.id AS restaurant_id, r.name AS restaurant_name, rr.date AS rating_date, rr.reviews AS review_count, rr.rating AS rating "
                + "FROM restaurants_amsterdam r "
                + "JOIN ratings_amsterdam rr ON r.id = rr.restaurant_id "
                + "WHERE r.name LIKE ? "
                + "ORDER BY r.id, rr.date DESC";

        try (Connection con = getConnection();
             PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.setString(1, "%" + namePart + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                Integer currentRestaurantId = null;

                while (rs.next()) {
                    Integer restaurantId = rs.getInt("restaurant_id");
                    String restaurantName = rs.getString("restaurant_name");
                    Date ratingDate = rs.getDate("rating_date");
                    int reviewCount = rs.getInt("review_count");
                    double rating = rs.getDouble("rating");

                    if (!restaurantId.equals(currentRestaurantId)) {
                        if (currentRestaurantId != null) {
                            System.out.println("********");
                            System.out.println();
                        }
                        System.out.println("********");
                        System.out.printf("%-30s%n", restaurantName);
                        System.out.printf("%-15s %-10s %-5s%n", "Date", "Rating", "Reviews");
                        currentRestaurantId = restaurantId;
                    }

                    System.out.printf("%-15s %-10.1f %-5d%n",
                            ratingDate.toString(), rating, reviewCount);
                }

                if (currentRestaurantId != null) {
                    System.out.println("********");
                }
            }

        } catch (SQLException e) {
            System.out.println("SQL Error while fetching restaurant data.");
            e.printStackTrace();
        }
    }

    private void printReviewAndRatingDiffs(Date date1, Date date2, DifferenceType type) throws Exception {
        String query = getReviewAndRatingDiffQuery(type);

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

    private String getReviewAndRatingDiffQuery(DifferenceType type) {
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
