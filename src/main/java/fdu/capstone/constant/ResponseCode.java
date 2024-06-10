package fdu.capstone.constant;

import lombok.Data;
import lombok.Getter;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
@Getter
public enum ResponseCode {
    SUCCESS(200, "success"),
    FAIL(201, "fail"),

    NO_ACCESS_PRIVILEGES(401, "no access privilege"),

    VALID_PARAMS_ERROR(1001, "invalid parameter"),


    SYS_USER_USERNAME_EXISTS(1101,"username already exist"),
    SYS_USER_PHONE_EXISTS(1102,"phone number already exist"),
    SYS_USER_NOT_EXISTS(1103,"user does not exist"),
    SYS_USER_PASSWORD_ERROR(1104, "username or password error"),
    SYS_USER_USERNAME_IS_NULL(1105, "username should not be null");

    private Integer code;
    private String message;

    private ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
