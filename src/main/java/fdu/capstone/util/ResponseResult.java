package fdu.capstone.util;

import fdu.capstone.constant.ResponseCode;
import lombok.Data;

/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */

@Data
public class ResponseResult<T>
{
    private Integer code;
    private String message;

    private T data;


    public static <T> ResponseResult<T> build(Integer code, String message, T data) {
        ResponseResult<T> result = new ResponseResult<>();

        if (data != null) {
            result.setData(data);
        }

        result.setCode(code);
        result.setMessage(message);

        return result;
    }


    public static <T> ResponseResult<T> success(T data) {
        return build(ResponseCode.SUCCESS.getCode(),null, data);
    }
    public static <T> ResponseResult<T> fail(Integer code, String message, T data) {
        return build(code, message, data);
    }
    public static <T> ResponseResult<T> fail(Integer code, String message) {
        return fail(code, message, null);
    }
    public static <T> ResponseResult<T> fail(String message) {
        return fail(ResponseCode.FAIL.getCode(), message, null);
    }

}
