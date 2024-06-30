package fdu.capstone.system.module.service.impl;

import com.alibaba.fastjson.JSON;
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

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */

@Service
public class ChatBotLogServiceImpl extends ServiceImpl<ChatBotLogMapper, ChatBotLog> implements ChatBotLogService {

    @Autowired
    RedisTemplate redisTemplate;

    private String chatLogRedisKeyPrefix = "chatlog_";

    @Override
    public boolean addChatbotLog(ChatBotLog chatBotLog) {
        redisTemplate.opsForList().rightPush(chatLogRedisKeyPrefix+chatBotLog.getSessionId()+"_"+chatBotLog.getChatType(), chatBotLog.getContent());
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
}
