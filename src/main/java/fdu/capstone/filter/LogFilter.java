package fdu.capstone.filter;

import fdu.capstone.constant.UserLoginResult;
import fdu.capstone.util.SnowflakeIdUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.io.IOException;


/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String traceId = request.getHeader(UserLoginResult.TRACE_ID.getCode());
        if (StringUtils.isBlank(traceId)) {
            SnowflakeIdUtil snowflakeIdUtil = new SnowflakeIdUtil(0, 0);
            traceId = snowflakeIdUtil.nextId() + "";
        }
        MDC.put(UserLoginResult.TRACE_ID.getCode(), traceId);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        MDC.remove(UserLoginResult.TRACE_ID.getCode());
    }
}
