package fdu.capstone.system.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fdu.capstone.system.module.entity.ChatBotLog;

import java.util.List;

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */
public interface ChatBotLogService extends IService<ChatBotLog> {


    public boolean addChatbotLog(ChatBotLog chatBotLog);

    public List<ChatBotLog> getChatbotLogListBySessionId(String sessionId);

    public List<String> getChatbotLogListFromCacheBySessionId(String sessionId,String type);

    String chat(Long userId, String sessionId, String prompt);

}
