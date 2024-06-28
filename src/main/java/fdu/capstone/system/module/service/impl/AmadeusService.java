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
import java.util.List;
import java.util.Map;

@Service
public class AmadeusService {

    private Amadeus amadeus;
    private Gson gson = new Gson();

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
                .and("subType", Locations.AIRPORT));

        Type type = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(gson.toJson(locations), type);
    }

    public List<Map<String, Object>> getFlightOffers(String origin, String destination, String departureDate, String returnDate, int adults) throws ResponseException {
        validateFutureDate(departureDate);
        if (returnDate != null && !returnDate.isEmpty()) {
            validateFutureDate(returnDate);
        }

        FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(
                Params.with("originLocationCode", origin)
                        .and("destinationLocationCode", destination)
                        .and("departureDate", departureDate)
                        .and("returnDate", returnDate)
                        .and("adults", adults)
                        .and("max", 3));

        Type type = new com.google.gson.reflect.TypeToken<List<Map<String, Object>>>() {}.getType();
        return gson.fromJson(gson.toJson(offers), type);
    }
}
