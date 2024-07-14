package fdu.capstone.system.module.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.amadeus.exceptions.ResponseException;
import fdu.capstone.system.module.entity.ChatBotLog;
import fdu.capstone.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Author: Liping Yin
 * Date: 2024/6/25
 */


@Slf4j
@Service
public class ChatBotServiceImpl {

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    @Autowired
    private RestTemplate restTemplate;
//    private final RestTemplate restTemplate = new RestTemplate();


    @Autowired
    AmadeusServiceImpl amadeusServiceImpl;
    @Autowired
    StringRedisTemplate stringRedisTemplate;


    @Autowired
    private ChatBotLogServiceImpl chatBotLogService;
    @Autowired
    PreferenceSortService preferenceSortService;


    // Map to store conversation history for each user session
    private final Map<String, List<Map<String, String>>> conversationHistory = new HashMap<>();

    private String[] flightQueryFields = {"departure", "destination", "departureDate", "roundTrip", "returnDate", "adultNumber", "childNumber"};
    //    private String[] preferenceFields = {"flightTakeoffTime", "flightArriveTime", "maxStops", "numberOfBaggage", "transitCountries"};
    private String[] preferenceFields = {"lowPriceScore", "durationScore", "stopScore"};

    String flightSearchPrefix = "This dialogue may contain the following fields: departure, destination, departure date, return date, round trip, number of adults, number of children.\n" +
            "    Your response should only be in JSON format, containing all the provided fields.\n" +
            "    Field names should be: departure, destination, departureDate, returnDate, roundTrip, adultNumber, childNumber.\n" +
            "    If it is not a round trip, set roundTrip to false, otherwise set roundTrip to true.\n" +
            "    For date fields, the return format should be YYYY-MM-DD HH:mm:ss.\n" +
            "    If the year is not provided, use the current year.\n" +
            "    If the month is not provided, use the current month.\n" +
            "    If the day is not provided, use 01 as default.\n" +
            "    If HH, mm, ss are not provided, use 00 as default.\n" +
            "    The input date formats can be DD/MM, MM/DD, DD-MM, or MM-DD.\n" +
            "    If the dialogue does not contain information for a field, exclude it from the response.\n" +
            "    Consider the previous dialogue as well, and remove any duplicated information.\n" +
            "    The returned JSON should be parsable.\n" +
            "    Content:";

    private String preferencePrefix = " This dialogue may contain feedback on the user's evaluations of low price preference, flight duration preference, and the number of flight stops preference.\n" +
            "    Your response should be in JSON format, containing all the provided fields: lowPriceScore, durationScore, stopScore.\n" +
            "    If the dialogue does not contain information for a field, exclude it from the response.\n" +
            "    For any field where the user gives a negative response, set the field to 0.\n" +
            "    If the user does not express any preference, give all fields a default value.\n" +
            "    Consider the previous dialogue as well, and remove any duplicated information.\n" +
            "    The returned JSON should be parsable.\n" +
            "    Content:";
    private String firstFlightSearchFieldQuestion = "Hi there! let me assist you to find best flight information firstly, please tell me: Where are you flying from  where would you like to go, when would you like to go, is it a round trip, if it is a round trip, when is the return date";
    //    private String firstPreferenceSearchFieldQuestion = "great,you have provided all necessary information,now could please provide your preferences regarding  when would like your flight to take off ?  when would like your flight to arrive ? how many stops do you like to take ?  how many baggage do you need to take ? if you flight need to transit to other countries, which countries do you prefer? ";
    private String firstPreferenceSearchFieldQuestion = "Great! You have provided all the necessary information. Now  please answer my first question regarding your flight preference: On a scale of 1 to 10, how important is the price of your flight ticket to you ?";

    private String updatedQuestions = " The user is trying to update the following fields: departure, destination, departure date, return date, round trip, number of adults, number of children, evaluations of low price preference, flight duration preference, and the number of flight stops preference.\n" +
            "    Please return the updated information in JSON format.\n" +
            "    The field names should be: departure, destination, departureDate, returnDate, roundTrip, adultNumber, childNumber, lowPriceScore, durationScore, stopScore.\n" +
            "    Also, add a flag named isChange to indicate if there are any changes in the listed fields compared to the previous information.\n" +
            "    For date fields, the return format should be YYYY-MM-DD HH:mm:ss.\n" +
            "    Your response should only be in JSON format.\n" +
            "    If the previous dialogue contains duplicated information, remove the duplicates.\n" +
            "    Content: ";
    private String flightFieldChatType = "flightInfo";
    private String preferenceFieldChatType = "preference";
    private String updateChatType = "all";


    //indicate if all query necessary info is gathered?
    private String roundCompleted = "_necessity";

    private int redisKeyExpiringTime = 2;

    private static final String COMPLETION = "completion";

    private static Map<String, String> flightSearchInfoQuestionNameAndQuestion = new HashMap<>();

    private static Map<String, String> preferenceQuestionNameAndQuestion = new HashMap<>();

    private Map<String, String> sessionNextPreferenceQuestionMap = new ConcurrentHashMap<>();
    private Map<String, String> sessionNextFlightSearchQuestionMap = new ConcurrentHashMap<>();
    ;

    static {
//        when would you like to go, is it a round trip, if it is a round trip, when is the return date
//        "departure", "destination", "departureDate", "roundTrip", "returnDate", "adultNumber", "childNumber"
        flightSearchInfoQuestionNameAndQuestion.put("departure", "Hi there! Let me assist you in finding the best flight information. Firstly, please tell me: Where are you flying from ?  ");
        flightSearchInfoQuestionNameAndQuestion.put("destination", "where would you like to go? ");
        flightSearchInfoQuestionNameAndQuestion.put("departureDate", "when would you like to go? ");
        flightSearchInfoQuestionNameAndQuestion.put("roundTrip", "is it a round trip? ");
        flightSearchInfoQuestionNameAndQuestion.put("returnDate", "It is a round trip, so when would you like to come back? ");
        flightSearchInfoQuestionNameAndQuestion.put("adultNumber", "how many adults will be traveling on this flight?  ");
        flightSearchInfoQuestionNameAndQuestion.put("childNumber", "how many kids will go with you? ");


        preferenceQuestionNameAndQuestion.put("lowPriceScore", "On a scale of 1 to 10, how important is the price of your flight ticket to you ? ");
        preferenceQuestionNameAndQuestion.put("durationScore", "On a scale of 1 to 10, how important is the flight duration to you ? ");
        preferenceQuestionNameAndQuestion.put("stopScore", "Some people prefer direct flights over connecting flights. On a scale of 1 to 10, how important is it for you to have a direct flight ? ");
    }


    public Object chat(Long userId, String sessionId, String prompt) {
        String allNecessaryInfoIsGathered = stringRedisTemplate.opsForValue().get(sessionId + roundCompleted);
        if (allNecessaryInfoIsGathered != null && allNecessaryInfoIsGathered.equals(COMPLETION)) {
            // user try to update  query and preference info
            String openAIResponse = null;
            JSONObject updatedQueryInfo = null;
            try {
                openAIResponse = askOpenAI(userId, sessionId, prompt, updatedQuestions, updateChatType);
                updatedQueryInfo = JSON.parseObject(openAIResponse, JSONObject.class);
            } catch (JSONException e) {
                log.error("open ai return data format is error, try it again ", e);
                openAIResponse = askOpenAI(userId, sessionId, prompt, updatedQuestions, updateChatType);
                updatedQueryInfo = JSON.parseObject(openAIResponse, JSONObject.class);
            }

            boolean isUpdated = (Boolean) updatedQueryInfo.get("isChange");
            if (!isUpdated) {
//                String p = "looks like user did not updated the existed information, please give him/her a nice notice to update those information";
                String p = "looks like you do not updated the existed information, please update your flight search information";
//                openAIResponse = askOpenAI(userId, sessionId, p, "", updateChatType);
                return p;
            } else {
                if ((Boolean) updatedQueryInfo.get("roundTrip")) {
                    if (updatedQueryInfo.get("returnDate") == null) {
                        return flightSearchInfoQuestionNameAndQuestion.get("returnDate");
                    }
                }
                List<Map<String, Object>> amadeusResult = searchAmadeus(updatedQueryInfo);
                if (amadeusResult == null) {
                    amadeusResult = searchAmadeus(updatedQueryInfo);
                }
                if (amadeusResult == null) {
                    return "error occur while search flight info";
                } else {
                    return sortFlightInfoByPreference(amadeusResult, updatedQueryInfo);
                }
            }

        } else {
            //does user provide any information about departure,destination,departure date
            String flightQueryInfoRaw = stringRedisTemplate.opsForValue().get(sessionId);
            String resp = null;
            if (flightQueryInfoRaw == null) {
                //first user build connection with the chatbot service
                stringRedisTemplate.opsForValue().set(sessionId, "{}", redisKeyExpiringTime, TimeUnit.HOURS);
//                return firstFlightSearchFieldQuestion;
                sessionNextFlightSearchQuestionMap.put(sessionId, flightQueryFields[0]);
                return flightSearchInfoQuestionNameAndQuestion.get(flightQueryFields[0]);
            } else {
                // flightQueryInfoRaw is not null, it means this is not the connection dialogue
                String flightInfoAskResp = askOpenAIForFlightInfo(userId, sessionId, prompt);
                if (flightInfoAskResp.equals(COMPLETION)) {
                    String preferenceResult = extractPreferenceInfo(userId, sessionId, prompt);
                    if (preferenceResult.equals(COMPLETION)) {
                        stringRedisTemplate.opsForValue().set(sessionId + roundCompleted, COMPLETION, redisKeyExpiringTime, TimeUnit.HOURS);
                        //user preference request is satisfied, and to search amadeus
                        JSONObject flightQueryInfo = JSON.parseObject(stringRedisTemplate.opsForValue().get(sessionId));
                        List<Map<String, Object>> amadeusResult = searchAmadeus(flightQueryInfo);
                        if (amadeusResult == null) {
                            return "error occur while search flight info";
                        } else {
                            String preferenceRedisKey = sessionId + "_preference";
                            String userPreferences = stringRedisTemplate.opsForValue().get(preferenceRedisKey);

                            JSONObject userPreferencesJson = JSON.parseObject(userPreferences, JSONObject.class);
                            return sortFlightInfoByPreference(amadeusResult, userPreferencesJson);
                        }
                    } else {
                        return preferenceResult;
                    }
                } else {
                    return flightInfoAskResp;
                }
            }
        }
    }


    private List<Map<String, Object>> sortFlightInfoByPreference(List<Map<String, Object>> amadeusResult, JSONObject flightQueryInfo) {

        List<Double> preferences = new ArrayList<>();
        preferences.add(Double.parseDouble(flightQueryInfo.get(preferenceFields[0]).toString()));
        preferences.add(Double.parseDouble(flightQueryInfo.get(preferenceFields[1]).toString()));
        preferences.add(Double.parseDouble(flightQueryInfo.get(preferenceFields[2]).toString()));
        List<Map<String, Object>> sortedResult = preferenceSortService.rrf(amadeusResult, preferences, 12);
        return sortedResult;
    }


    private String askOpenAIForFlightInfo(Long userId, String sessionId, String prompt) {


//        String resp;
        String flightQueryInfoRaw = stringRedisTemplate.opsForValue().get(sessionId);

        JSONObject flightQueryInfo = JSON.parseObject(flightQueryInfoRaw, JSONObject.class);
        Boolean roundTrip = flightQueryInfo.getBoolean("roundTrip");
        if (flightQueryInfo.size() == flightQueryFields.length || (roundTrip != null && !roundTrip && flightQueryInfo.size() == flightQueryFields.length - 1)) {
            // go to next level dialogue, to ask user's preference
            return COMPLETION;
        }

        prompt = flightSearchInfoQuestionNameAndQuestion.get(sessionNextFlightSearchQuestionMap.get(sessionId)) + prompt;

        String openAIResponse = askOpenAI(userId, sessionId, prompt, flightSearchPrefix, flightFieldChatType);
        try {
            flightQueryInfo = JSON.parseObject(openAIResponse, JSONObject.class);
        } catch (JSONException e) {
            log.error("open ai return data format is error, try it again ", e);
            openAIResponse = askOpenAI(userId, sessionId, prompt, flightSearchPrefix, flightFieldChatType);
            flightQueryInfo = JSON.parseObject(openAIResponse, JSONObject.class);
        }

        //first save openai response to redis
        stringRedisTemplate.opsForValue().set(sessionId, openAIResponse, redisKeyExpiringTime, TimeUnit.HOURS);

        for (String queryFiled : flightQueryFields) {
            Object fieldValue = flightQueryInfo.get(queryFiled);
            if (fieldValue == null) {
                if (queryFiled.equals("returnDate")) {
                    if (!(Boolean) flightQueryInfo.get("roundTrip")) {
                        continue;
                    }
                }
                sessionNextFlightSearchQuestionMap.put(sessionId, queryFiled);
                return flightSearchInfoQuestionNameAndQuestion.get(queryFiled);
            }
        }
        return COMPLETION;

    }

    public String askOpenAI(Long userId, String sessionId, String prompt, String preQuestion, String type) {

        List<String> chatBotLogs = null;
        if (type.equals(updateChatType)) {
            chatBotLogs = chatBotLogService.getChatbotLogListFromCacheBySessionId(sessionId, flightFieldChatType);
            List<String> preferenceChatBotLogs = chatBotLogService.getChatbotLogListFromCacheBySessionId(sessionId, preferenceFieldChatType);
            List<String> updatedChatBotLogs = chatBotLogService.getChatbotLogListFromCacheBySessionId(sessionId, updateChatType);
            chatBotLogs.addAll(preferenceChatBotLogs);
            chatBotLogs.addAll(updatedChatBotLogs);
        } else {
            chatBotLogs = chatBotLogService.getChatbotLogListFromCacheBySessionId(sessionId, type);
        }


        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> userPrompt = new HashMap<>();
        userPrompt.put("role", "user");
        userPrompt.put("content", preQuestion + prompt);
        messages.add(userPrompt);

        if (chatBotLogs != null && chatBotLogs.size() > 0) {
            for (String chatBotLog : chatBotLogs) {
                Map<String, String> userHistoryPrompt = new HashMap<>();
                userHistoryPrompt.put("role", "assistant");
                userHistoryPrompt.put("content", chatBotLog);
                messages.add(userHistoryPrompt);
            }

        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        String responseBody = response.getBody();
        JSONObject choice = (JSONObject) ((JSONArray) JSON.parseObject(responseBody).get("choices")).get(0);
        JSONObject message = (JSONObject) choice.get("message");
        String responseContent = message.get("content").toString();
        ChatBotLog responseChatLog = ChatBotLog.builder().content(prompt).sessionId(sessionId).chatType(type).build();

        if (userId != null) {
            responseChatLog.setUserId(userId);
        }

        chatBotLogService.addChatbotLog(responseChatLog);

        responseContent = responseContent.replace("json", "").replace("```", "");
        return responseContent;

    }


    //{"lowPriceScore", "durationScore", "stopScore"};
    private String extractPreferenceInfo(Long userId, String sessionId, String prompt) {

        String resp = COMPLETION;
        String preferenceRedisKey = sessionId + "_preference";

        String userPreferences = stringRedisTemplate.opsForValue().get(preferenceRedisKey);
        if (userPreferences == null) {
            sessionNextPreferenceQuestionMap.put(sessionId, preferenceFields[0]);
            stringRedisTemplate.opsForValue().set(preferenceRedisKey, "{}", redisKeyExpiringTime, TimeUnit.HOURS);
            return firstPreferenceSearchFieldQuestion;
        }


        JSONObject userPreferencesJson = JSON.parseObject(userPreferences, JSONObject.class);

        String openAIResponse = "";
        if (userPreferencesJson.size() != preferenceFields.length) {

            String questionField = sessionNextPreferenceQuestionMap.get(sessionId);
            prompt = preferenceQuestionNameAndQuestion.get(questionField) + prompt;
            try {
                openAIResponse = askOpenAI(userId, sessionId, prompt, preferencePrefix, preferenceFieldChatType);
                userPreferencesJson = JSON.parseObject(openAIResponse, JSONObject.class);
            } catch (JSONException e) {
                log.error("open ai return data format is error, try it again ", e);
                openAIResponse = askOpenAI(userId, sessionId, prompt, preferencePrefix, preferenceFieldChatType);
                userPreferencesJson = JSON.parseObject(openAIResponse, JSONObject.class);
            }

            stringRedisTemplate.opsForValue().set(preferenceRedisKey, openAIResponse, redisKeyExpiringTime, TimeUnit.HOURS);

            for (String preferenceField : preferenceFields) {
                if (userPreferencesJson.get(preferenceField) == null) {
                    sessionNextPreferenceQuestionMap.put(sessionId, preferenceField);
                    resp = preferenceQuestionNameAndQuestion.get(preferenceField);
                    break;
                }
            }
            return resp;
        } else {
            // all the preference information are provided
            return COMPLETION;
        }
    }


    public List<Map<String, Object>> searchAmadeus(JSONObject flightQueryInfo) {
        try {
            // invoke flight search api
            String departure = flightQueryInfo.get("departure").toString();
            String destination = flightQueryInfo.get("destination").toString();
            String departureDate = flightQueryInfo.get("departureDate").toString();
            String returnDate = flightQueryInfo.get("returnDate") == null ? null : flightQueryInfo.get("returnDate").toString();
            int adultNumber = (int) flightQueryInfo.get("adultNumber");
            departureDate = DateUtil.addYears(departureDate, 1);
            departureDate = DateUtil.convertToLocalDate(departureDate);
            if (returnDate != null) {
                returnDate = DateUtil.addYears(returnDate, 1);
                returnDate = DateUtil.convertToLocalDate(returnDate);

            }
            return amadeusServiceImpl.getFlightOffers(departure, destination, departureDate, returnDate, adultNumber);
        } catch (ResponseException e) {
            log.error("search flight info api error", e);
            return null;
        }
    }


    public String getCompletion(String sessionId, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        List<Map<String, String>> messages = conversationHistory.computeIfAbsent(sessionId, k -> new ArrayList<>());

        // Add the new user message to the conversation history
        messages.add(new HashMap<String, String>() {{
            put("role", "user");
            put("content", prompt);
        }});

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                OPENAI_API_URL,
                HttpMethod.POST,
                entity,
                String.class
        );

        String responseBody = response.getBody();

        // Add the assistant's response to the conversation history
        messages.add(new HashMap<String, String>() {{
            put("role", "assistant");
            put("content", parseContentFromResponse(responseBody));
        }});

        return responseBody;
    }

    private String parseContentFromResponse(String response) {
        // Parse the response body to extract the assistant's message content
        // This is a simplified example, you might need a proper JSON parsing logic here
        int startIndex = response.indexOf("\"content\":\"") + 11;
        int endIndex = response.indexOf("\"", startIndex);
        return response.substring(startIndex, endIndex);
    }
}

