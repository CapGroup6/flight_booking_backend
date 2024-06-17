package fdu.capstone.exception;

import fdu.capstone.constant.ResponseCode;
import lombok.Data;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */

@Data
public class BaseException extends RuntimeException{
    private Integer code;

    public BaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BaseException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.code = responseCode.getCode();
    }


}
