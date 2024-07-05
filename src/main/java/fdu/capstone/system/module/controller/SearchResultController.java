package fdu.capstone.system.module.controller;

import com.amadeus.exceptions.ResponseException;
import fdu.capstone.system.module.service.impl.SearchResultService;
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
        return new ArrayList<>();
    }
}
