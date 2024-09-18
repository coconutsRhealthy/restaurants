package com.lennart.restaurants.analysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class NewRestaurantsIdentifier {

    public static void main(String[] args) throws Exception {
        NewRestaurantsIdentifier identifier = new NewRestaurantsIdentifier();
        identifier.identifyNewRestaurants(identifier.createDate("2024-09-01"));
    }

    public void identifyNewRestaurants(Date startDate) throws Exception {
        Map<String, Map<Date, Integer>> reviewData = BasicDataRetriever.getReviewDataForAllRestaurants();

        AtomicInteger counter = new AtomicInteger(1);

        reviewData.entrySet().stream()
                .filter(entry -> !hasDataBeforeStartDate(startDate, entry.getValue()))
                .filter(entry -> getReviewAmount(entry.getValue()) < 50)
                .sorted((entry1, entry2) -> Integer.compare(getReviewAmount(entry2.getValue()),
                        getReviewAmount(entry1.getValue())))
                .forEach(entry -> {
                    int reviewAmount = getReviewAmount(entry.getValue());
                    System.out.println(counter.getAndIncrement() + ". " + entry.getKey() + " - " + reviewAmount + " reviews");
                });
    }

    private boolean hasDataBeforeStartDate(Date startDate, Map<Date, Integer> reviewDataForRestaurant) {
        return reviewDataForRestaurant.keySet().stream()
                .anyMatch(date -> !date.after(startDate));
    }

    private int getReviewAmount(Map<Date, Integer> reviewDataForRestaurant) {
        Optional<Date> mostRecentDate = reviewDataForRestaurant.keySet().stream()
                .max(Date::compareTo);
        return mostRecentDate.map(reviewDataForRestaurant::get).orElse(-1);
    }

    private Date createDate(String dateString) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(dateString);
    }
}
