package fdu.capstone.system.module.entity.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Author: Liping Yin
 * Date: 2024/6/6
 */

@Data
public class LoginUserRequest {

    @NotBlank(message = "user name should not be empty")
    private String username;

    /** 密码 */
    @NotBlank(message = "password should not be  empty")
    private String password;
}
