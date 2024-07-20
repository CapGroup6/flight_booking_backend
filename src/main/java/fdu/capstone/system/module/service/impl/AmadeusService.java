package fdu.capstone.system.module.service.impl;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.referenceData.Locations;
import com.amadeus.resources.Airline;
import com.amadeus.resources.FlightOfferSearch;
import com.amadeus.resources.Location;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AmadeusService {

    private final Amadeus amadeus;
    private final Gson gson = new Gson();

    public AmadeusService(@Value("${amadeus.apiKey}") String apiKey,
                          @Value("${amadeus.apiSecret}") String apiSecret) {
        this.amadeus = Amadeus.builder(apiKey, apiSecret).build();
    }

    private void validateFutureDate(String date) {
        LocalDate inputDate = LocalDate.parse(date);
        if (inputDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Date must be in the future: " + date);
        }
    }

    private static final List<Map<String, Object>> predefinedLocations = Arrays.asList(
            createLocation("Toronto", "YYZ", "Canada"),
            createLocation("Vancouver", "YVR", "Canada"),
            createLocation("Montreal", "YUL", "Canada"),
            createLocation("Calgary", "YYC", "Canada"),
            createLocation("Ottawa", "YOW", "Canada"),
            createLocation("Edmonton", "YEG", "Canada"),
            createLocation("Halifax", "YHZ", "Canada"),
            createLocation("Winnipeg", "YWG", "Canada"),
            createLocation("Quebec City", "YQB", "Canada"),
            createLocation("Victoria", "YYJ", "Canada"),
            createLocation("Saskatoon", "YXE", "Canada"),
            createLocation("Regina", "YQR", "Canada")
    );

    private static Map<String, Object> createLocation(String cityName, String iataCode, String countryName) {
        Map<String, Object> location = new HashMap<>();
        location.put("cityName", cityName);
        location.put("iataCode", iataCode);
        location.put("countryName", countryName);
        return location;
    }

    public static List<Map<String, Object>> modifyLocationList(String keyword, List<Map<String, Object>> locationList) {
        for (Map<String, Object> location : predefinedLocations) {
            if (((String)location.get("cityName")).equalsIgnoreCase(keyword) ||
                ((String)location.get("cityName")).toLowerCase().startsWith(keyword.toLowerCase())) {
                Map<String, Object> newLocation = new HashMap<>(location);
                locationList.add(0, newLocation);
                break;
            }
        }
        return locationList;
    }

    public List<Map<String, Object>> getLocations(String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.CITY));
        System.out.println("Keyword:"+keyword);
        Type type = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
        List<Map<String, Object>> locationList = gson.fromJson(gson.toJson(locations), type);
        List<Map<String, Object>> updatedList = modifyLocationList(keyword, locationList);
        return updatedList;
    }

    public List<Map<String, Object>> getFlightOffers(String origin, String destination, String departureDate, String returnDate, int adults) throws ResponseException {
        return getFlightOffers(origin, destination, departureDate, returnDate, adults, 0, "ECONOMY");
    }

    public List<Map<String, Object>> getFlightOffers(String origin, String destination, String departureDate, String returnDate, int adults, int children) throws ResponseException {
        return getFlightOffers(origin, destination, departureDate, returnDate, adults, children, "ECONOMY");
    }

    public List<Map<String, Object>> getFlightOffers(String origin, String destination, String departureDate, String returnDate, int adults, String cabinClass) throws ResponseException {
        return getFlightOffers(origin, destination, departureDate, returnDate, adults, 0, cabinClass);
    }

    public List<Map<String, Object>> getFlightOffers(String origin, String destination, String departureDate, String returnDate, int adults, int children, String cabinClass) throws ResponseException {
        validateFutureDate(departureDate);
        FlightOfferSearch[] offers;
        if (returnDate != null && !returnDate.isEmpty()) {
            validateFutureDate(returnDate);
            offers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", departureDate)
                            .and("returnDate", returnDate)
                            .and("adults", adults)
                            .and("children", children)
                            .and("travelClass", cabinClass)
                            .and("max", 6));
        } else {
            offers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", departureDate)
                            .and("adults", adults)
                            .and("children", children)
                            .and("travelClass", cabinClass)
                            .and("max", 6));
        }

        // Create a map to cache airline names and city/airport names to avoid multiple API calls
        Map<String, String> airlineNameCache = new HashMap<>();
        Map<String, Map<String, String>> cityAirportNameCache = new HashMap<>();

        List<Map<String, Object>> modifiedOffers = new ArrayList<>();

        for (FlightOfferSearch offer : offers) {
            Map<String, Object> offerMap = gson.fromJson(gson.toJson(offer), Map.class);

            List<Map<String, Object>> itineraries = (List<Map<String, Object>>) offerMap.get("itineraries");
            for (Map<String, Object> itinerary : itineraries) {
                List<Map<String, Object>> segments = (List<Map<String, Object>>) itinerary.get("segments");
                for (Map<String, Object> segment : segments) {
                    String carrierCode = (String) segment.get("carrierCode");
                    String airlineName = airlineNameCache.computeIfAbsent(carrierCode, this::getAirlineName);
                    segment.put("airlineName", airlineName);

                    // Process departure and arrival locations
                    Map<String, Object> departure = (Map<String, Object>) segment.get("departure");
                    Map<String, Object> arrival = (Map<String, Object>) segment.get("arrival");

                    String departureIataCode = (String) departure.get("iataCode");
                    String arrivalIataCode = (String) arrival.get("iataCode");

                    Map<String, String> departureNames = cityAirportNameCache.computeIfAbsent(departureIataCode, this::getCityAndAirportName);
                    Map<String, String> arrivalNames = cityAirportNameCache.computeIfAbsent(arrivalIataCode, this::getCityAndAirportName);

                    departure.put("cityName", departureNames.get("cityName"));
                    departure.put("airportName", departureNames.get("airportName"));
                    arrival.put("cityName", arrivalNames.get("cityName"));
                    arrival.put("airportName", arrivalNames.get("airportName"));

                    // Remove the carrierCode field if needed
                    segment.remove("carrierCode");
                }
            }

            modifiedOffers.add(offerMap);
        }

        return modifiedOffers;
    }

    private String getAirlineName(String carrierCode) {
        try {
            Airline[] airlines = amadeus.referenceData.airlines.get(Params.with("airlineCodes", carrierCode));
            if (airlines.length > 0) {
                return airlines[0].getCommonName();
            }
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        return carrierCode;
    }

    private Map<String, String> getCityAndAirportName(String iataCode) {
        Map<String, String> nameMap = new HashMap<>();
        try {
            Location[] locations = amadeus.referenceData.locations.get(Params.with("keyword", iataCode).and("subType", Locations.ANY));
            if (locations.length > 0) {
                nameMap.put("cityName", locations[0].getAddress().getCityName());
                nameMap.put("airportName", locations[0].getName());
            }
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        if (!nameMap.containsKey("cityName")) {
            nameMap.put("cityName", iataCode);
        }
        if (!nameMap.containsKey("airportName")) {
            nameMap.put("airportName", iataCode);
        }
        return nameMap;
    }
}
