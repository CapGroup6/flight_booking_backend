package fdu.capstone.constant;

import lombok.Getter;
import org.checkerframework.checker.index.qual.GTENegativeOne;

/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */

@Getter
public enum UserLoginResult {

    NORMAL("0", "success"),
    DELETED("1", "delete"),

    ENABLE("0", "enable"),
    DISABLE("1", "disable"),

    TRACE_ID("TRACE_ID", "log trace id"),

    THREAD_LOCAL_LOGIN_USER_KEY("user", "user login key");

    private String code;
    private  String name;

    private UserLoginResult(String code,String name){
        this.code = code;
        this.name = name;
    }
}
