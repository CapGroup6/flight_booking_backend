package fdu.capstone.system.module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */

@Data
@Builder
@TableName("chatbot_log")
public class ChatBotLog extends CommonField{

    private Long userId;
    private String sessionId;
    private String chatType;
    private String content;


}
