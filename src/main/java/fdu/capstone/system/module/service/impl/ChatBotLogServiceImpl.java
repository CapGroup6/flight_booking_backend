package fdu.capstone.system.module.service.impl;

/** import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fdu.capstone.system.module.dao.ChatBotLogMapper;
import fdu.capstone.system.module.entity.ChatBotLog;
import fdu.capstone.system.module.service.ChatBotLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

 * Author : Liping Yin
 * Date : 6/28/24
 */

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fdu.capstone.system.module.dao.ChatBotLogMapper;
import fdu.capstone.system.module.entity.ChatBotLog;
import fdu.capstone.system.module.service.ChatBotLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class ChatBotLogServiceImpl extends ServiceImpl<ChatBotLogMapper, ChatBotLog> implements ChatBotLogService {

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    private String chatLogRedisKeyPrefix = "chatlog_";

    @Override
    public boolean addChatbotLog(ChatBotLog chatBotLog) {
        String redisKey = chatLogRedisKeyPrefix+chatBotLog.getSessionId()+"_"+chatBotLog.getChatType();
        redisTemplate.opsForList().rightPush(redisKey, chatBotLog.getContent());
        redisTemplate.expire(redisKey,3,TimeUnit.HOURS);
        return this.save(chatBotLog);
    }

    @Override
    public List<ChatBotLog> getChatbotLogListBySessionId(String sessionId) {
        return this.list(new QueryWrapper<ChatBotLog>().eq("session_id",sessionId));
    }

    @Override
    public List<String> getChatbotLogListFromCacheBySessionId(String sessionId,String type) {
        return redisTemplate.opsForList().range(chatLogRedisKeyPrefix+sessionId+"_"+type,0,-1);
    }

    //interact wth opan ai api, added by Chi Xie
    @Override
    public String chat(Long userId, String sessionId, String prompt) {
        // OpenAI API
        String apiUrl = "https://api.openai.com/v1/engines/davinci-codex/completions";
        RestTemplate restTemplate = new RestTemplate();

        Map<String, Object> request = new HashMap<>();
        request.put("prompt", prompt);
        request.put("max_tokens", 100);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openaiApiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);

        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        String text = (String) choices.get(0).get("text");

        return text;
    }
}
