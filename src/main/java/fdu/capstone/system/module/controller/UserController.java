package fdu.capstone.system.module.controller;

import fdu.capstone.system.module.entity.dto.LoginUserRequest;
import fdu.capstone.util.ResponseResult;
import fdu.capstone.system.module.entity.dto.SysUserRequest;
import fdu.capstone.system.module.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;



/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */
@Tag(name = "user management")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private SysUserService sysUserService;


    @PostMapping("/login")
    public ResponseResult login(@Validated @RequestBody LoginUserRequest loginUserRequest) {
        String token = sysUserService.login(loginUserRequest);
        return ResponseResult.success(token);
    }

    @Operation(description = "user register")
    @PostMapping("/register")
    public ResponseResult addUser(@Validated @RequestBody SysUserRequest sysUserRequest) {

        sysUserService.adduser(sysUserRequest);

        return ResponseResult.success(null);
    }

    @Operation(description =  "update user profile")
    @PutMapping("/update/{id}")
    public ResponseResult updateUser(@PathVariable("id") Long id,
                                     @Validated @RequestBody SysUserRequest sysUserRequest) {

        if (Objects.isNull(id)) {
            return ResponseResult.fail("user id should not be null");
        }

        sysUserService.updateUser(id, sysUserRequest);

        return ResponseResult.success(null);
    }


}
