package com.lennart.restaurants.analysis;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NewRestaurantsIdentifier {

    public static void main(String[] args) throws Exception {
        NewRestaurantsIdentifier identifier = new NewRestaurantsIdentifier();
        Map<String, Date> newRestaurants = identifier.identifyNewRestaurants(identifier.createDate("2024-08-20"));
        System.out.println("wacht");
    }

    public Map<String, Date> identifyNewRestaurants(Date startDate) throws Exception {
        Map<String, Map<Date, Integer>> reviewData = BasicDataRetriever.getReviewDataForAllRestaurants();

        Map<String, Map<Date, Integer>> newRestaurantsNoDataBefore = identifyNewRestaurantsNoBefore(startDate, reviewData);
        Map<String, Map<Date, Integer>> newRestaurantsWithBeforeData = identifyNewRestaurantsWithBefore(startDate, reviewData);

        Map<String, Date> mapToReturn = Stream.concat(
                newRestaurantsNoDataBefore.entrySet().stream(),
                newRestaurantsWithBeforeData.entrySet().stream()
        ).collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> getOldestDate(entry.getValue())
        ));

        return sortByValue(mapToReturn);
    }

    private Map<String, Map<Date, Integer>> identifyNewRestaurantsNoBefore(Date startDate,
                                                                           Map<String, Map<Date, Integer>> reviewData) {
        int reviewAmountThreshold = 50;

        return reviewData.entrySet().stream()
                .filter(entry -> {
                    Date oldestDate = getOldestDate(entry.getValue());
                    return oldestDate != null && !oldestDate.before(startDate);
                })
                .filter(entry -> getMostRecentReviewAmount(entry.getValue()) < reviewAmountThreshold)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Map<String, Map<Date, Integer>> identifyNewRestaurantsWithBefore(Date startDate,
                                                                             Map<String, Map<Date, Integer>> reviewData) {
        return reviewData.entrySet().stream()
                .filter(entry -> {
                    Map<Date, Integer> reviewDataForRestaurant = entry.getValue();

                    Date mostRecentDateBeforeStartDate = reviewDataForRestaurant.keySet().stream()
                            .filter(date -> !date.after(startDate))
                            .max(Date::compareTo)
                            .orElse(null);

                    Date firstDateAfterStartDate = reviewDataForRestaurant.keySet().stream()
                            .filter(date -> date.after(startDate))
                            .min(Date::compareTo)
                            .orElse(null);

                    return mostRecentDateBeforeStartDate != null &&
                            getReviewAmountForDate(reviewDataForRestaurant, mostRecentDateBeforeStartDate) <= 2 &&
                            firstDateAfterStartDate != null &&
                            getReviewAmountForDate(reviewDataForRestaurant, firstDateAfterStartDate) >= 7;
                })
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }

    private Date getOldestDate(Map<Date, Integer> reviewDataForRestaurant) {
        return reviewDataForRestaurant.keySet().stream()
                .min(Date::compareTo)
                .orElse(null);
    }

    private int getMostRecentReviewAmount(Map<Date, Integer> reviewDataForRestaurant) {
        Optional<Date> mostRecentDate = reviewDataForRestaurant.keySet().stream()
                .max(Date::compareTo);
        return mostRecentDate.map(reviewDataForRestaurant::get).orElse(-1);
    }

    private int getReviewAmountForDate(Map<Date, Integer> reviewDataForRestaurant, Date date) {
        return Optional.ofNullable(date)
                .map(reviewDataForRestaurant::get)
                .orElse(0);
    }

    private Date createDate(String dateString) throws Exception {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.parse(dateString);
    }

    private <K, V extends Comparable<V>> Map<K, V> sortByValue(Map<K, V> map) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<K, V>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}
