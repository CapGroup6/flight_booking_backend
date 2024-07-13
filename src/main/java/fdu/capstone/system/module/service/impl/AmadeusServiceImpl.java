package fdu.capstone.system.module.service.impl;

import com.amadeus.Amadeus;
import com.amadeus.Params;
import com.amadeus.exceptions.ResponseException;
import com.amadeus.referenceData.Locations;
import com.amadeus.resources.FlightOfferSearch;
import com.amadeus.resources.Location;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AmadeusServiceImpl {

    private Amadeus amadeus;
    private Gson gson = new Gson();

    public AmadeusServiceImpl(@Value("${amadeus.apiKey}") String apiKey,
                          @Value("${amadeus.apiSecret}") String apiSecret) {
        this.amadeus = Amadeus.builder(apiKey, apiSecret).build();
    }

//    private void validateFutureDate(String date) {
//        LocalDate inputDate = LocalDate.parse(date);
//        if (inputDate.isBefore(LocalDate.now())) {
//            throw new IllegalArgumentException("Date must be in the future: " + date);
//        }
//    }

    private void validateFutureDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime inputDate = LocalDateTime.parse(date,formatter);
        if (inputDate.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date must be in the future: " + date);
        }
    }

    public List<Map<String, Object>> getLocations(String keyword) throws ResponseException {
        Location[] locations = amadeus.referenceData.locations.get(Params
                .with("keyword", keyword)
                .and("subType", Locations.AIRPORT));

        Type type = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(gson.toJson(locations), type);
    }
    public List<Map<String, Object>> getFlightOffers(String originCity, String destinationCity, String departureDate, String returnDate, int adults) throws ResponseException {
//        validateFutureDate(departureDate);
        if (returnDate != null && !returnDate.isEmpty()) {
            validateFutureDate(returnDate);
        }

        List<Map<String, Object>> originAirports = getLocations(originCity);
        List<Map<String, Object>> destinationAirports = getLocations(destinationCity);

        List<FlightOfferSearch> allOffers = new ArrayList<>();

        for (Map<String, Object> originAirport : originAirports) {
            String originCode = (String) originAirport.get("iataCode");
            for (Map<String, Object> destinationAirport : destinationAirports) {
                String destinationCode = (String) destinationAirport.get("iataCode");

                Params params =   Params.with("originLocationCode", originCode)
                        .and("destinationLocationCode", destinationCode)
                        .and("departureDate", departureDate)
                        .and("adults", adults)
                        .and("max", 3);
                if(returnDate!=null){
                    params = params.and("retureDate",returnDate);
                }
                FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(params);

                for (FlightOfferSearch offer : offers) {
                    allOffers.add(offer);
                }
            }
        }

        Type type = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(gson.toJson(allOffers), type);
    }
}
