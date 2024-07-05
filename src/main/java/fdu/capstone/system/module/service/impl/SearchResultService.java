package fdu.capstone.system.module.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SearchResultService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static final String SEARCH_RESULTS_PREFIX = "searchResults::";

    public void saveSearchResults(String sessionId, List<Map<String, Object>> searchResults) {
        try {
            String json = objectMapper.writeValueAsString(searchResults);
            redisTemplate.opsForValue().set(SEARCH_RESULTS_PREFIX + sessionId, json, Duration.ofMinutes(30));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize search results", e);
        }
    }

    public List<Map<String, Object>> getSearchResults(String sessionId) {
        try {
            String json = (String) redisTemplate.opsForValue().get(SEARCH_RESULTS_PREFIX + sessionId);
            if (json != null) {
                return objectMapper.readValue(json, new TypeReference<List<Map<String, Object>>>(){});
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize search results", e);
        }
        return new ArrayList<>();
//        return (List<Map<String, Object>>) redisTemplate.opsForValue().get(SEARCH_RESULTS_PREFIX + sessionId);
    }

    public List<Map<String, Object>> sortResultByPrice(List<Map<String, Object>> searchResult) {
        if (searchResult.isEmpty())
            return searchResult;
        searchResult.sort((m1, m2) -> {
            Map<String, Object> price1 = (Map<String, Object>) m1.get("price");
            Map<String, Object> price2 = (Map<String, Object>) m2.get("price");
            // After get("price"), the return format is
            // {currency=EUR, total=862.15, base=383.0, fees=[{amount=0.0, type=SUPPLIER}, {amount=0.0, type=TICKETING}], grandTotal=862.15}
            // cast this to Map<String, Object> and get("total") to get the price (as a double)
            Double value1 = (Double) price1.get("total");
            Double value2 = (Double) price2.get("total");

            return value1.compareTo(value2);
        });
        // test // output to console to check
        for (Map<String, Object> stringObjectMap : searchResult) {
            System.out.print(((Map<String, Object>) stringObjectMap.get("price")).get("total"));
            System.out.print(" ");
        }
        System.out.println();
        return searchResult;
    }

    public Integer flightDurationInMinutes(String duration) {
        Pattern pattern = Pattern.compile("PT(?:(\\d+)H)?(?:(\\d+)M)?");
        Matcher matcher = pattern.matcher(duration);

        int hours = 0;
        int minutes = 0;

        if (matcher.matches()) {
            // Extract hours
            String hoursStr = matcher.group(1);
            if (hoursStr != null) {
                hours = Integer.parseInt(hoursStr);
            }

            // Extract minutes
            String minutesStr = matcher.group(2);
            if (minutesStr != null) {
                minutes = Integer.parseInt(minutesStr);
            }
        }
        return hours*60+minutes;
    }
    public List<Map<String, Object>> sortResultByDuration(List<Map<String, Object>> searchResult) {
        if (searchResult.isEmpty())
            return searchResult;
        searchResult.sort((m1, m2) -> {
            ArrayList itineraries1 = (ArrayList) m1.get("itineraries");
            ArrayList itineraries2 = (ArrayList) m2.get("itineraries");
            Map<String, Object> itinerariesObject1 = (Map<String, Object>) itineraries1.get(0);
            Map<String, Object> itinerariesObject2 = (Map<String, Object>) itineraries2.get(0);
            String durationStr1 = (String) itinerariesObject1.get("duration");
            String durationStr2 = (String) itinerariesObject2.get("duration");
            Integer duration1 = flightDurationInMinutes(durationStr1);
            Integer duration2 = flightDurationInMinutes(durationStr2);

            return duration1.compareTo(duration2);
        });
        // test // output to console to check
        for (Map<String, Object> stringObjectMap : searchResult) {
            System.out.print(flightDurationInMinutes((String)(((Map<String, Object>) ((ArrayList) stringObjectMap.get("itineraries")).get(0)).get("duration"))));
            System.out.print(" ");
        }
        System.out.println();
        return searchResult;
    }

}
