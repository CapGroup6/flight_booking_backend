package fdu.capstone.system.module.controller;

import com.amadeus.exceptions.ResponseException;
import fdu.capstone.system.module.service.impl.AmadeusService;
import fdu.capstone.system.module.service.impl.AmadeusServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/flights")
    public List<Map<String, Object>> getFlightOffers(@RequestParam String origin,
                                                     @RequestParam String destination,
                                                     @RequestParam String departureDate,
                                                     @RequestParam(required = false) String returnDate,
                                                     @RequestParam int adults) throws ResponseException {
        return amadeusService.getFlightOffers(origin, destination, departureDate, returnDate, adults);
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
