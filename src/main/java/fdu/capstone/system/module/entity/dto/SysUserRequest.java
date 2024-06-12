package fdu.capstone.system.module.entity.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
@Data
public class SysUserRequest {

    @NotBlank(message = "user's name can't be valid")
    private String username;

    @Schema(description = "password")
    private String password;

    @Schema(description = "nickName")
    private String nickName;

    @Schema(description = "phone")
    private String phone;

    @Schema(description = "email")
    @Email
    private String email;


}
