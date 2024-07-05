package fdu.capstone.system.module.controller;

import com.amadeus.exceptions.ResponseException;
import fdu.capstone.system.module.service.impl.AmadeusService;
import fdu.capstone.system.module.service.impl.SearchResultService;
import jakarta.servlet.http.HttpSession;
import fdu.capstone.system.module.service.impl.AmadeusServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class AmadeusController {

    @Autowired
    private AmadeusService amadeusService;

    @Autowired
    private AmadeusServiceImpl amadeusServiceImpl;
    @CrossOrigin(origins = "http://localhost:3000") // Allow requests from localhost:3000

    @GetMapping("/locations")
    public List<Map<String, Object>> getLocations(@RequestParam String keyword) throws ResponseException {
        return amadeusService.getLocations(keyword);
    }

    @Autowired
    private SearchResultService searchResultService;

    private CookieController cookieController;

    @GetMapping("/flights")
    public List<Map<String, Object>> getFlightOffers(@RequestParam String origin,
                                                     @RequestParam String destination,
                                                     @RequestParam String departureDate,
                                                     @RequestParam(required = false) String returnDate,
                                                     @RequestParam int adults,
                                                     HttpSession session) throws ResponseException {
        // test
        System.out.println("AmadeusController getFlightOffers()");
        //
//        System.out.println(cookieController.setCookie());
        List<Map<String, Object>> searchResult = amadeusService.getFlightOffers(origin, destination, departureDate, returnDate, adults);
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        System.out.println("Session ID at save = "+sessionId);
        searchResultService.saveSearchResults(sessionId, searchResult);
        // test
        System.out.println("Result size = "+String.valueOf(searchResult.size()));
        for (Map<String, Object> stringObjectMap : searchResult) {
            ArrayList subResult = (ArrayList) stringObjectMap.get("itineraries");
            Map<String, Object> subResultObject = (Map<String, Object>) subResult.get(0);
            String duration = (String) subResultObject.get("duration");
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
            System.out.println(duration+" "+ hours +" "+ minutes);
        }
        System.out.println();
        return searchResult;
    }
    @GetMapping("/flightsV2")
    public List<Map<String, Object>> getFlightOffersV2(@RequestParam String origin,
                                                     @RequestParam String destination,
                                                     @RequestParam String departureDate,
                                                     @RequestParam(required = false) String returnDate,
                                                     @RequestParam int adults) throws ResponseException {
        return amadeusServiceImpl.getFlightOffers(origin, destination, departureDate, returnDate, adults);
    }
}
