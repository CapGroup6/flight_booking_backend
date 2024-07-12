package fdu.capstone.system.module.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import fdu.capstone.system.module.entity.AirportLocationPair;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

    public List<AirportLocationPair> getStopoverList(List<Map<String, Object>> searchResult) throws JsonProcessingException {
        return getStopoverList(searchResult, "oneway");
    }
    public List<AirportLocationPair> getStopoverList(List<Map<String, Object>> searchResult, String whichTrip) throws JsonProcessingException {
        if (searchResult.isEmpty())
            return new ArrayList<>();
        Set<String> uniqueStopoverList = new LinkedHashSet<>();
        Boolean roundtrip;
        for (Map<String, Object> stringObjectMap : searchResult) {
            ArrayList itinerariesArray = (ArrayList) stringObjectMap.get("itineraries"); // 1 item for one-way, 2 items for round-trip
            roundtrip = (itinerariesArray.size() == 2); // flag of mode switch
            int itineraryStart = 0, itineraryEnd = 0;
            if (roundtrip) {
                if (whichTrip.equals("round")) {
                    itineraryEnd = 1;
                } else if (whichTrip.equals("return")) {
                    itineraryStart = 1;
                    itineraryEnd = 1;
                }
            }
            for (int i = itineraryStart; i < itineraryEnd + 1; ++i) {
                Map<String, Object> itinerariesObject = (Map<String, Object>) itinerariesArray.get(i);
                ArrayList segmentsArray = (ArrayList) itinerariesObject.get("segments");
                if (segmentsArray.size() == 1) // No stopover
                    break;
                for (int j = 1; j < segmentsArray.size(); ++j) {
                    Map<String, Object> transferSegment = (Map<String, Object>) segmentsArray.get(j);
                    Map<String, Object> departureMap = (Map<String, Object>) transferSegment.get("departure");
                    String iataCode = (String) departureMap.get("iataCode");
                    uniqueStopoverList.add(iataCode);
                }
            }
        }

        List<AirportLocationPair> airportLocationPairs = new ArrayList<>();

        String csvFile = "IATA.csv";
        Map<String, String> airportLocationMap = new HashMap<>();
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/" + csvFile));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : csvParser) {
                String airport = record.get("Airport");
                String location = record.get("\uFEFFLocation"); // a Byte Order Mark is at the beginning of the csv
                airportLocationMap.put(airport, location);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String airport: uniqueStopoverList) {
            String location = airportLocationMap.get(airport);
            if (location != null) {
                AirportLocationPair pair = new AirportLocationPair(airport, location);
                airportLocationPairs.add(pair);
            } else {
                System.out.println("Location not found for airport "+airport);
            }
        }
        return airportLocationPairs;
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

    public List<Map<String, Object>> filter(List<Map<String, Object>> searchResult,
                                            String whichTrip,
                                            List<Integer> numStopover,
                                            List<String> stopoverList,
                                            int depStart, int depEnd,
                                            int arrStart, int arrEnd) {
        int num = searchResult.size();
        Boolean[] select = new Boolean[num];
        Arrays.fill(select, true);
        int resultIndex = 0;
        for (Map<String, Object> result: searchResult) {
            ArrayList itinerariesArray = (ArrayList) result.get("itineraries");

            int itineraryStart = 0, itineraryEnd = 0;
            if (itinerariesArray.size() == 2) {
                if (whichTrip.equals("round")) {
                    itineraryEnd = 1;
                } else if (whichTrip.equals("return")) {
                    itineraryStart = 1;
                    itineraryEnd = 1;
                }
            }
            for (int i = itineraryStart; i < itineraryEnd + 1; ++i) {
                Map<String, Object> itinerariesObject = (Map<String, Object>) itinerariesArray.get(i);
                ArrayList segmentsArray = (ArrayList) itinerariesObject.get("segments");
                int segmentNum = segmentsArray.size();
                if (!numStopover.isEmpty()) { // filter: number of stopovers
                    boolean match = false;
                    for (Integer integer : numStopover) {
                        if (integer.equals(segmentNum-1)) {
                            match = true;
                            break;
                        }
                    }
                    select[resultIndex] = select[resultIndex] && match;
                }
                if (!select[resultIndex])
                    break;

                if (!stopoverList.isEmpty()) { // filter: stopover list
                    boolean match = false;
                    for (int j = 1; j < segmentsArray.size(); ++j) {
                        Map<String, Object> transferSegment = (Map<String, Object>) segmentsArray.get(j);
                        Map<String, Object> departureMap = (Map<String, Object>) transferSegment.get("departure");
                        String iataCode = (String) departureMap.get("iataCode");
                        for (String string : stopoverList) {
                            if (string.equals(iataCode)) {
                                match = true;
                                break;
                            }
                        }
                        if (match)
                            break;
                    }
                    select[resultIndex] = select[resultIndex] && match;
                }
                if (!select[resultIndex])
                    break;

                Map<String, Object> departureSegment = (Map<String, Object>) segmentsArray.get(0);
                Map<String, Object> departureMap = (Map<String, Object>) departureSegment.get("departure");
                String departureTimeStr = (String) departureMap.get("at");
                LocalDateTime dateTime = LocalDateTime.parse(departureTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                int departTime = dateTime.getHour()*100+dateTime.getMinute(); // integer: HHMM
                boolean match = (departTime >= depStart) && (departTime <= depEnd);
                select[resultIndex] = select[resultIndex] && match;
                if (!select[resultIndex])
                    break;

                Map<String, Object> arrivalSegment = (Map<String, Object>) segmentsArray.get(segmentNum-1);
                Map<String, Object> arrivalMap = (Map<String, Object>) arrivalSegment.get("arrival");
                String arrivalTimeStr = (String) arrivalMap.get("at");
                dateTime = LocalDateTime.parse(arrivalTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                int arrivalTime = dateTime.getHour()*100+dateTime.getMinute(); // integer: HHMM
                match = (arrivalTime >= arrStart) && (arrivalTime <= arrEnd);
                select[resultIndex] = select[resultIndex] && match;
            }

            resultIndex++;
        }

        List<Map<String, Object>> filterResult = new ArrayList<>();
        for (int i = 0; i < searchResult.size(); ++i)
            if (select[i]) {
                filterResult.add(searchResult.get(i));
            }
        return filterResult;
    }

}
