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

    public List<Map<String, Object>> getLocations(String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.CITY));

        Type type = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(gson.toJson(locations), type);
    }

    public List<Map<String, Object>> getFlightOffers(String origin, String destination, String departureDate, String returnDate, int adults) throws ResponseException {
        validateFutureDate(departureDate);
        FlightOfferSearch[] offers;
        if (returnDate != null && !returnDate.isEmpty()) { // round-trip
            validateFutureDate(returnDate);
            offers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", departureDate)
                            .and("returnDate", returnDate)
                            .and("adults", adults)
                            .and("children", children)
                            .and("infants", infants)
                            .and("travelClass", cabinClass)
                            .and("max", 6));
        } else { // one-way
            offers = amadeus.shopping.flightOffersSearch.get(
                    Params.with("originLocationCode", origin)
                            .and("destinationLocationCode", destination)
                            .and("departureDate", departureDate)
                            .and("adults", adults)
                            .and("children", children)
                            .and("infants", infants)
                            .and("travelClass", cabinClass)
                            .and("max", 6));
        }

        // Create a map to cache airline names and city names to avoid multiple API calls
        Map<String, String> airlineNameCache = new HashMap<>();
        Map<String, String> cityNameCache = new HashMap<>();

        List<Map<String, Object>> modifiedOffers = new ArrayList<>();

        for (FlightOfferSearch offer : offers) {
            // Convert offer to a map
            Map<String, Object> offerMap = gson.fromJson(gson.toJson(offer), Map.class);

            // Process itineraries
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

                    String departureCityName = cityNameCache.computeIfAbsent(departureIataCode, this::getCityName);
                    String arrivalCityName = cityNameCache.computeIfAbsent(arrivalIataCode, this::getCityName);

                    departure.put("cityName", departureCityName);
                    arrival.put("cityName", arrivalCityName);

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
            // Handle the exception (e.g., log it)
            e.printStackTrace();
        }
        return carrierCode; // Fallback to carrier code if name not found
    }

    private String getCityName(String iataCode) {
        try {
            Location[] locations = amadeus.referenceData.locations.get(Params.with("keyword", iataCode).and("subType", Locations.ANY));
            if (locations.length > 0) {
                return locations[0].getAddress().getCityName();
            }
        } catch (ResponseException e) {
            // Handle the exception (e.g., log it)
            e.printStackTrace();
        }
        return iataCode; // Fallback to IATA code if city name not found
    }
}
