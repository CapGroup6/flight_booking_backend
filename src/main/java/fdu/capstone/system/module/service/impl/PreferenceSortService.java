package fdu.capstone.system.module.service.impl;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.ibatis.ognl.ObjectElementsAccessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class PreferenceSortService {
    @Autowired
    private SearchResultService searchResultService;

    public List<Map<String, Object>> rrf(List<Map<String, Object>> searchResult, List<Double> weight, int k) {
        int n = searchResult.size();
        if (n == 0)
            return searchResult;
        double[][] rrfScore = new double[n][4]; // col-0 for total, 1 for price, 2 for duration, 3 for stopover number
        for (int i = 0; i < n; ++i) {
            Map<String, Object> flight = searchResult.get(i);
            flight.put("sortID", i);
        }
        List<Map<String, Object>> resultByPrice = new ArrayList<>();
        List<Map<String, Object>> resultByDuration = new ArrayList<>();
        List<Map<String, Object>> resultByStopNum = new ArrayList<>();
        for (Map<String, Object> item: searchResult) {
            Map<String, Object> copy1 = new HashMap<>(item);
            resultByPrice.add(copy1);
            Map<String, Object> copy2 = new HashMap<>(item);
            resultByDuration.add(copy2);
            Map<String, Object> copy3 = new HashMap<>(item);
            resultByStopNum.add(copy3);
        }
        resultByPrice = searchResultService.sortResultByPrice(resultByPrice);
        resultByDuration = searchResultService.sortResultByDuration(resultByDuration);
        resultByStopNum = searchResultService.sortResultByStopoverNum(resultByStopNum);
        Integer id;

        for (int i = 0; i < n; ++i) {
            Map<String, Object> flight = resultByPrice.get(i);
            id = (Integer) flight.get("sortID");
            rrfScore[id][1] = 1.0/(k+i+1); // the id-th item has a rank of i+1
        }
        for (int i = 0; i < n; ++i) {
            Map<String, Object> flight = resultByDuration.get(i);
            id = (Integer) flight.get("sortID");
            rrfScore[id][2] = 1.0/(k+i+1); // the id-th item has a rank of i+1
        }
        for (int i = 0; i < n; ++i) {
            Map<String, Object> flight = resultByStopNum.get(i);
            id = (Integer) flight.get("sortID");
            rrfScore[id][3] = 1.0/(k+i+1); // the id-th item has a rank of i+1
        }
        int head = 0, tail = 1;
        while (tail < n) { // adjust the stopover score when the stopNums are equal, for stopover has limited values
            Integer headStopNum = searchResultService.getStopoverNum(searchResult.get(head));
            Integer tailStopNum = searchResultService.getStopoverNum(searchResult.get(tail));
            if (headStopNum.equals(tailStopNum)) {
                rrfScore[tail][3] = rrfScore[head][3];
                tail++;
            } else {
                head = tail;
                tail++;
            }
        }

        for (int i = 0; i < n; ++i) { // final weighted RRF score combining all factors
            rrfScore[i][0] = 0;
            for (int j = 1; j < 4; ++j) {
                rrfScore[i][0] += rrfScore[i][j] * weight.get(j-1);
            }
        }

        searchResult.sort((m1, m2) -> {
            Integer id1 = (Integer) m1.get("sortID");
            Integer id2 = (Integer) m2.get("sortID");
            Double score1 = rrfScore[id1][0];
            Double score2 = rrfScore[id2][0];
            return score2.compareTo(score1);
        });

        return searchResult;
    }

    public List<Map<String, Object>> zScoreSort(List<Map<String, Object>> searchResult, List<Double> weight) {
        int n = searchResult.size();
        if (n == 0)
            return searchResult;
        double[][] zScore = new double[n][4];
        for (int i = 0; i < n; ++i) {
            Map<String, Object> flight = searchResult.get(i);
            flight.put("sortID", i);
        }
        DescriptiveStatistics priceStats = new DescriptiveStatistics();
        DescriptiveStatistics durationStats = new DescriptiveStatistics();
//        DescriptiveStatistics stopStats = new DescriptiveStatistics();
        for (Map<String, Object> flight: searchResult) {
            priceStats.addValue(searchResultService.getPrice(flight));
            durationStats.addValue(searchResultService.getDurationInMinute(flight));
//            stopStats.addValue(searchResultService.getStopoverNum(flight));
        }
        double priceMean = priceStats.getMean();
        double priceStddev = priceStats.getStandardDeviation();
        double durationMean = durationStats.getMean();
        double durationStddev = durationStats.getStandardDeviation();
        Integer id, duration, stopNum;
        Double price;
        for (int i = 0; i < n; ++i) {
            Map<String, Object> flight = searchResult.get(i);
            id = (Integer) flight.get("sortID");
            price = searchResultService.getPrice(flight);
            duration = searchResultService.getDurationInMinute(flight);
            stopNum = searchResultService.getStopoverNum(flight);
            zScore[id][1] = (priceMean - price) / priceStddev;
            zScore[id][2] = (durationMean - duration) / durationStddev;
            zScore[id][3] = 2.0 / (stopNum + 1);
            zScore[id][0] = 0;
            for (int j = 1; j < 4; ++j) {
                zScore[id][0] += zScore[id][j] * weight.get(j-1);
            }
        }
        searchResult.sort((m1, m2) -> {
            Integer id1 = (Integer) m1.get("sortID");
            Integer id2 = (Integer) m2.get("sortID");
            Double score1 = zScore[id1][0];
            Double score2 = zScore[id2][0];
            return score2.compareTo(score1);
        });
        return searchResult;
    }
}
