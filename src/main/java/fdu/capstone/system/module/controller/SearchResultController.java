package fdu.capstone.system.module.controller;

import com.amadeus.exceptions.ResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import fdu.capstone.system.module.entity.AirportLocationPair;
import fdu.capstone.system.module.service.impl.PreferenceSortService;
import fdu.capstone.system.module.service.impl.SearchResultService;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/result")
public class SearchResultController {
    @Autowired
    private SearchResultService searchResultService;
    @Autowired
    private PreferenceSortService preferenceSortService;

    @GetMapping("/sort")
    public List<Map<String, Object>> sortOffers(@CookieValue("myCookie") @RequestParam String sortBy,
                                                HttpSession session) throws ResponseException {
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        System.out.println("Session ID at sort = "+sessionId);
        List<Map<String, Object>> searchResult = searchResultService.getSearchResults(sessionId);
        System.out.println("Search Result Size = "+String.valueOf(searchResult.size()));
        if (sortBy.equals("price"))
            return searchResultService.sortResultByPrice(searchResult);
        if (sortBy.equals("duration"))
            return searchResultService.sortResultByDuration(searchResult);
        if (sortBy.equals("stopNum"))
            return searchResultService.sortResultByStopoverNum(searchResult);
        return new ArrayList<>();
    }

    @GetMapping("/getStopoverList")
    public List<AirportLocationPair> getStopoverList(@RequestParam String whichTrip,
                                                               HttpSession session) throws JsonProcessingException {
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        List<Map<String, Object>> searchResult = searchResultService.getSearchResults(sessionId);
        if (whichTrip.isEmpty())
            return searchResultService.getStopoverList(searchResult);
        else
            return searchResultService.getStopoverList(searchResult, whichTrip);
    }

    @GetMapping("/getStopoverNum")
    public List<Integer> getStopoverNum(@RequestParam String whichTrip, HttpSession session) {
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        List<Map<String, Object>> searchResult = searchResultService.getSearchResults(sessionId);
        return searchResultService.getStopoverNum(searchResult, whichTrip);
    }

    @GetMapping("/filter")
    public List<Map<String, Object>> filterResult(@RequestParam String whichTrip,
                                                  @RequestParam List<Integer> numStopover,
                                                  @RequestParam List<String> stopOverList,
                                                  @RequestParam int depStart,
                                                  @RequestParam int depEnd,
                                                  @RequestParam int arrStart,
                                                  @RequestParam int arrEnd,
                                                  HttpSession session) {
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        List<Map<String, Object>> searchResult = searchResultService.getSearchResults(sessionId);
        return searchResultService.filter(searchResult, whichTrip, numStopover, stopOverList, depStart, depEnd, arrStart, arrEnd);
    }

    @GetMapping("/preference/rrf")
    public List<Map<String, Object>> rrfPreference(@RequestParam List<Integer> weight, HttpSession session) {
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        List<Map<String, Object>> searchResult = searchResultService.getSearchResults(sessionId);
        if (weight.size() != 3)
            return searchResult;
        List<Double> dWeight = new ArrayList<>();
        double sum = weight.get(0)+weight.get(1)+weight.get(2);
        for (int i = 0; i < 3; ++i)
            dWeight.add(weight.get(i)/sum);
        return preferenceSortService.rrf(searchResult, dWeight, 12);
    }

    @GetMapping("/preference/zScore")
    public List<Map<String, Object>> zScorePreference(@RequestParam List<Integer> weight, HttpSession session) {
        String sessionId = session.getId();
        sessionId = "ThisIsATestSession"; // for test
        List<Map<String, Object>> searchResult = searchResultService.getSearchResults(sessionId);
        if (weight.size() != 3)
            return searchResult;
        List<Double> dWeight = new ArrayList<>();
        double sum = weight.get(0)+weight.get(1)+weight.get(2);
        for (int i = 0; i < 3; ++i)
            dWeight.add(weight.get(i)/sum);
        return preferenceSortService.zScoreSort(searchResult, dWeight);
    }
}
