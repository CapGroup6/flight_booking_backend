package fdu.capstone.system.module.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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


    // Map to store conversation history for each user session
    private final Map<String, List<Map<String, String>>> conversationHistory = new HashMap<>();

    private String[] flightQueryFields = {"departure", "destination", "departureDate", "roundTrip", "returnDate", "adultNumber", "childNumber"};
    private String[] preferenceFields = {"flightTakeoffTime", "flightArriveTime", "maxStops", "numberOfBaggage", "transitCountries"};

    String flightSearchPrefix = "does this dialogue contain departure, destination, departure date,  return date, round trip,number of adult,number of child, your response should only be json-format containing all the provided fields, the field names should be departure,destination,departureDate,returnDate,roundTrip,adultNumber,childNumber, if it is not a round trip set roundTrip false, else set roundTrip true , if the field's type is date,the return format should be YYYY-MM-DD HH:mm:ss,if year is not provided, use current year,  if month is not provided use current month,if day is not provided use 01 as default , if HH,mm,ss is not provided use 00 as default, the input date format can be DD/MM ,MM/DD ,DD-MM,MM-DD. if the dialogue do not contain a field information, please exclude it from the response,you should consider the previous dialogue,if previous dialogue contains duplicated message you should remove the duplicated information. remember the return should be parsable json format. content:";

    private String preferencePrefix = "does this dialogue contain flight takeoff time,flight arrive time, max stops ,number of baggage and transit countries, your response should be json-format containing all the provided fields, the field names should be flightTakeoffTime,flightArriveTime,maxStops,numberOfBaggage,transitCountries if the dialogue do not contain a field information, please exclude it from the response,for a field, if user give negative response,set the field to default null, if user do not have any preference,give all the fields default value, you should consider the previous dialogue ,if previous dialogue contains duplicated message you should remove the duplicated information, content: ";

    private String firstFlightSearchFieldQuestion = "Hi there! let me assist you to find best flight information firstly, please tell me: Where are you flying from , where would you like to go, when would you like to go, is it a round trip, if it is a round trip, when is the return date";
    private String firstPreferenceSearchFieldQuestion = "great,you have provided all necessary information,now could please provide your preferences regarding  when would like your flight to take off ?  when would like your flight to arrive ? how many stops do you like to take ?  how many baggage do you need to take ? if you flight need to transit to other countries, which countries do you prefer? ";

    private String updatedQuestions = "user is trying to update the departure, destination, departure date,  return date, round trip,number of adult,number of child ,flight takeoff time,flight arrive time, max stops ,number of baggage and transit countries. please return the updated  information in json-format , the field  should be departure,destination,departureDate,returnDate,roundTrip,adultNumber,childNumber, flightTakeoffTime,flightArriveTime,maxStops,numberOfBaggage,transitCountries, also add a flag named isChange to indicate if there are any change in the listed fields compared to before information. if field's type is date,the return format should be YYYY-MM-DD HH:mm:ss. your response should only be json-format, if previous dialogue contains duplicated message you should remove the duplicated information, content:  ";
    private String flightFieldChatType = "flightInfo";
    private String preferenceFieldChatType = "preference";
    private String updateChatType = "all";


    //indicate if all query necessary info is gathered?
    private String roundCompleted = "_necessity";

    private int redisKeyExpiringTime = 2;

    private static final String COMPLETION = "completion";


    public Object chat(Long userId, String sessionId, String prompt) {
        String allNecessaryInfoIsGathered = stringRedisTemplate.opsForValue().get(sessionId + roundCompleted);
        if (allNecessaryInfoIsGathered != null && allNecessaryInfoIsGathered.equals(COMPLETION)) {
            // user try to update  query and preference info
            String openAIResponse = askOpenAI(userId, sessionId, prompt, updatedQuestions, updateChatType);
            JSONObject updatedQueryInfo = JSON.parseObject(openAIResponse, JSONObject.class);
            boolean isUpdated = (Boolean) updatedQueryInfo.get("isChange");
            if(!isUpdated){
                String p = "looks like user did not updated the existed information, please give him/her a nice notice to update those information";
//                openAIResponse = askOpenAI(userId, sessionId, p, "", updateChatType);
                return p;
            }else {
               List<Map<String, Object>> amadeusResult =  searchAmadeus(updatedQueryInfo);
                return amadeusResult == null ? "error occur while search flight info" : amadeusResult;
            }

        } else {
            //does user provide any information about departure,destination,departure date
            String flightQueryInfoRaw = stringRedisTemplate.opsForValue().get(sessionId);
            String resp = null;
            if (flightQueryInfoRaw == null) {
                //first user build connection with the chatbot service
                stringRedisTemplate.opsForValue().set(sessionId, "{}", redisKeyExpiringTime, TimeUnit.HOURS);
                return firstFlightSearchFieldQuestion;
            } else {
                // flightQueryInfoRaw is not null, it means this is not the connection dialogue
                JSONObject flightQueryInfo = JSON.parseObject(flightQueryInfoRaw, JSONObject.class);
                Boolean roundTrip = flightQueryInfo.getBoolean("roundTrip");
                if (flightQueryInfo.size() == flightQueryFields.length || (roundTrip != null && !roundTrip && flightQueryInfo.size() == flightQueryFields.length - 1)) {
                    //user flight search info is satisfied, go to next level dialogue, to ask user's preference
                    String preferenceResult = extractPreferenceInfo(userId, sessionId, prompt);
                    if (preferenceResult.equals(COMPLETION)) {
                        stringRedisTemplate.opsForValue().set(sessionId + roundCompleted, COMPLETION, redisKeyExpiringTime, TimeUnit.HOURS);
                        //user preference request is satisfied, and to search amadeus
                        List<Map<String, Object>> amadeusResult = searchAmadeus(flightQueryInfo);
                        return amadeusResult == null ? "error occur while search flight info" : amadeusResult;
                    } else {
                        return preferenceResult;
                    }
                } else {
                    String flightInfoAskResp = askOpenAIForFlightInfo(userId, sessionId, prompt);
                    if (flightInfoAskResp.equals(COMPLETION)) {
                        // ask user's preferences
                        String preferenceResult = extractPreferenceInfo(userId, sessionId, prompt);
                        if (preferenceResult.equals(COMPLETION)) {
                            stringRedisTemplate.opsForValue().set(sessionId + roundCompleted, COMPLETION, redisKeyExpiringTime, TimeUnit.HOURS);
                            //user preference request is satisfied, and to search amadeus
                            List<Map<String, Object>> amadeusResult = searchAmadeus(flightQueryInfo);
                            return amadeusResult == null ? "error occur while search flight info" : amadeusResult;
                        } else {
                            return preferenceResult;
                        }
                    } else {
                        return flightInfoAskResp;
                    }
                }
            }
        }
    }


    private String askOpenAIToUpdateFlightQueryAndPreference(Long userId, String sessionId, String prompt) {


        return null;
    }

    private String askOpenAIForFlightInfo(Long userId, String sessionId, String prompt) {


        String resp;
        String flightQueryInfoRaw = stringRedisTemplate.opsForValue().get(sessionId);

        JSONObject flightQueryInfo = JSON.parseObject(flightQueryInfoRaw, JSONObject.class);

        String openAIResponse = askOpenAI(userId, sessionId, prompt, flightSearchPrefix, flightFieldChatType);
        flightQueryInfo = JSON.parseObject(openAIResponse, JSONObject.class);

        //first save openai response to redis
        stringRedisTemplate.opsForValue().set(sessionId, openAIResponse, redisKeyExpiringTime, TimeUnit.HOURS);
        List<String> unprovidedFieldList = new ArrayList<>();
        for (String field : flightQueryFields) {
            if (!flightQueryInfo.containsKey(field)) {
                unprovidedFieldList.add(field);
            }
        }

        Boolean roundTrip = flightQueryInfo.getBoolean("roundTrip");
        if (unprovidedFieldList.size() == 0 || (roundTrip != null && !roundTrip && flightQueryInfo.size() == flightQueryFields.length - 1)) {
            // go to next level dialogue, to ask user's preference
            resp = COMPLETION;
            return resp;
        } else {
            resp = "can your provide the below information: ";
            for (String unprovidedField : unprovidedFieldList) {
                switch (unprovidedField) {
                    case "departure":
                        resp = resp + " " + "what is your departure";
                        break;
                    case "destination":
                        resp = resp + " " + "what is your destination;";
                        break;
                    case "departureDate":
                        resp = resp + " " + "when is your departure date;";
                        break;
                    case "adultNumber":
                        resp = resp + " " + "how many adults will attend this flight;";
                        break;
                    case "childNumber":
                        resp = resp + " " + "how many children will attend this flight;";
                        break;
                    case "roundTrip":
                        resp = resp + " " + " is it a round trip";
                        break;
                    case "returnDate":
                        if (flightQueryInfo.getBoolean("roundTrip") != null &&
                                flightQueryInfo.getBoolean("roundTrip")) {
                            resp = resp + " " + "when is your return date";
                        }
                        break;
                }
            }
            return resp;
        }
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

        responseContent = responseContent.replace("json","").replace("```","");
        return responseContent;

    }


    private String extractPreferenceInfo(Long userId, String sessionId, String prompt) {

        String resp = COMPLETION;
        String preferenceRedisKey = sessionId + "_preference";

        String userPreferences = stringRedisTemplate.opsForValue().get(preferenceRedisKey);
        if (userPreferences == null) {
            stringRedisTemplate.opsForValue().set(preferenceRedisKey, "{}", redisKeyExpiringTime, TimeUnit.HOURS);
            return firstPreferenceSearchFieldQuestion;
        }
        JSONObject userPreferencesJson = JSON.parseObject(userPreferences, JSONObject.class);

        String openAIResponse = "";
        if (userPreferencesJson.size() != preferenceFields.length) {
            openAIResponse = askOpenAI(userId, sessionId, prompt, preferencePrefix, preferenceFieldChatType);
            JSONObject preferenceInfo = JSON.parseObject(openAIResponse, JSONObject.class);
            stringRedisTemplate.opsForValue().set(preferenceRedisKey, openAIResponse, redisKeyExpiringTime, TimeUnit.HOURS);

            // if user give all the information in the first round of conversation of preference
            if (preferenceInfo.size() == preferenceFields.length) {
                return resp;
            } else {
                // we continue conversation ........
                List<String> unprovidedPreferenceFieldList = new ArrayList<>();
                for (String field : preferenceFields) {
                    if (!preferenceInfo.containsKey(field)) {
                        unprovidedPreferenceFieldList.add(field);
                    }
                }

                resp = "would your like to provide answers to below questions regarding your preferences: ";
                for (String unprovidedField : unprovidedPreferenceFieldList) {
                    switch (unprovidedField) {
                        case "flightTakeoffTime":
                            resp = resp + " when would like your flight to take off ?";
                            break;
                        case "flightArriveTime":
                            resp = resp + " when would like your flight to arrive";
                            break;
                        case "maxStops":
                            resp = resp + " how many stops do you like";
                            break;
                        case "numberOfBaggage":
                            resp = resp + " how many baggages do you need to take";
                            break;
                        case "transitCountries":
                            resp = resp + " if you flight need to transit to other countries, which countries do you prefer";
                            break;
                    }
                }

                return resp;


            }
        } else {
            // all the preference information are provided
            return resp;
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
                returnDate = DateUtil.addYears(returnDate, 2);
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

