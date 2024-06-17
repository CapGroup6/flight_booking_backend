package fdu.capstone.system.module.controller;

import com.google.common.net.HttpHeaders;
import fdu.capstone.system.module.entity.UserSearchLog;
import fdu.capstone.system.module.entity.dto.LoginUserRequest;
import fdu.capstone.system.module.entity.dto.SysUserRequest;
import fdu.capstone.system.module.entity.dto.UserSearchLogRequest;
import fdu.capstone.system.module.service.SysUserService;
import fdu.capstone.system.module.service.UserSearchLogService;
import fdu.capstone.util.HttpServletContextUtils;
import fdu.capstone.util.JwtUtil;
import fdu.capstone.util.ResponseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;
import java.util.Objects;


/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */
@Slf4j
@Tag(name = "user search log")
@RestController
@RequestMapping("/userSearchLog")
public class UserSearchLogController {

    @Autowired
    private UserSearchLogService userSearchLogService;

    @Value("${system.config.auth.tokenKey}")
    private String tokenKey;

    @Value("${system.config.auth.tokenSignSecret}")
    private String tokenSignSecret;



    @PostMapping("/save")
    public ResponseResult save(@RequestBody UserSearchLogRequest userSearchLogRequest) {

        HttpServletRequest httpServletRequest = HttpServletContextUtils.getHttpServletRequest();
        String token = httpServletRequest.getHeader(tokenKey);
        if(StringUtils.isNotBlank(token)){
            Object userIdObj = JwtUtil.getProperty(token,tokenSignSecret,"userId");
            if(Objects.nonNull(userIdObj)){
                try{
                    userSearchLogRequest.setUserId(Long.parseLong(userIdObj.toString()));
                    userSearchLogService.saveUserSearchLog(userSearchLogRequest);
                    return ResponseResult.success("add user search log success");
                }catch (Exception e){
                    log.error("parse jwt error",e);
                    return ResponseResult.fail("add user search log error: "+e.getMessage());
                }

            }else {
                return ResponseResult.fail("can not find target user id ");
            }
        }else {
            return ResponseResult.fail("token is null ");
        }

    }


    @Operation(description =  "get user search log")
    @GetMapping("/search")
    public ResponseResult  getUserSearchLog() {

        HttpServletRequest httpServletRequest = HttpServletContextUtils.getHttpServletRequest();
        String token = httpServletRequest.getHeader(tokenKey);
        if(StringUtils.isNotBlank(token)){
            Object userIdObj = JwtUtil.getProperty(token,tokenSignSecret,"userId");
            if(Objects.nonNull(userIdObj)){
                try{
                    List<UserSearchLog> searchLogList = userSearchLogService.listUserSearchLogByUserId(Long.parseLong(userIdObj.toString()));
                    return ResponseResult.success(searchLogList);
                }catch (Exception e){
                    log.error("parse jwt error",e);
                    return ResponseResult.fail("fetch user search log error: "+e.getMessage());
                }

            }else {
                return ResponseResult.fail("can not find target user id ");
            }
        }else {
            return ResponseResult.fail("token is null ");
        }
    }


}
