package fdu.capstone.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fdu.capstone.constant.ResponseCode;
import fdu.capstone.constant.UserLoginResult;
import fdu.capstone.util.ResponseResult;
import fdu.capstone.util.JwtUtil;
import fdu.capstone.util.ThreadLocalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Map;

/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Value("${system.config.auth.tokenKey}")
    private String tokenKey;

    @Value("${system.config.auth.tokenSignSecret}")
    private String tokenSignSecret;



    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {


        ResponseResult<Object> result = ResponseResult.build(null, null, null);

        String token = request.getHeader(tokenKey);

        //先验证是否有白名单放行
        if (StringUtils.isNotBlank(token)) {
            try {
                Map<String, Object> userInfo = JwtUtil.validate(token, tokenSignSecret);
                ThreadLocalUtil.set(UserLoginResult.THREAD_LOCAL_LOGIN_USER_KEY.getCode(), userInfo.get("username"));
                return true;
            } catch (TokenExpiredException e) {
                e.printStackTrace();
                result = ResponseResult.fail(ResponseCode.NO_ACCESS_PRIVILEGES.getCode(), tokenKey + "已经过期");
            } catch (SignatureVerificationException e) {
                e.printStackTrace();
                result = ResponseResult.fail(ResponseCode.NO_ACCESS_PRIVILEGES.getCode(), tokenKey + "签名错误");
            } catch (AlgorithmMismatchException e) {
                e.printStackTrace();
                result = ResponseResult.fail(ResponseCode.NO_ACCESS_PRIVILEGES.getCode(), tokenKey + "加密算法不匹配");
            } catch (Exception e) {
                e.printStackTrace();
                result = ResponseResult.fail(ResponseCode.NO_ACCESS_PRIVILEGES.getCode(), tokenKey + "无效");
            }
            String json = new ObjectMapper().writeValueAsString(result);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().println(json);
            return false;
        } else {
            return true;
        }
    }


    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        ThreadLocalUtil.remove();
    }
}
