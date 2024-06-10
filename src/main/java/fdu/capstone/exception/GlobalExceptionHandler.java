package fdu.capstone.exception;

import fdu.capstone.util.ResponseResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BaseException.class)
    public ResponseResult handleBusinessException(BaseException exception, HttpServletRequest request) {
        return ResponseResult.fail(exception.getCode(), exception.getMessage());
    }
}
