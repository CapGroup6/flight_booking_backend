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
    FAIL(440, "fail"),

    NO_ACCESS_PRIVILEGES(441, "no access privilege"),

    VALID_PARAMS_ERROR(442, "invalid parameter"),


    SYS_USER_USERNAME_EXISTS(451,"username already exist"),
    SYS_USER_PHONE_EXISTS(452,"phone number already exist"),
    SYS_USER_NOT_EXISTS(453,"user does not exist"),
    SYS_USER_PASSWORD_ERROR(454, "username or password error"),
    SYS_USER_USERNAME_IS_NULL(455, "username should not be null");

    private Integer code;
    private String message;

    private ResponseCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
