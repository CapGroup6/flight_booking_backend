package fdu.capstone.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * Author: Liping Yin
 * Date: 2024/6/12
 */
public class HttpServletContextUtils {

    public static HttpServletRequest getHttpServletRequest(){
        ServletRequestAttributes servletWebRequest = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return servletWebRequest.getRequest();
    }
}
