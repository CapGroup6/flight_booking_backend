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
@Schema(description = "SysUser")
public class SysUserRequest {

    @NotBlank(message = "user's name can't be valid")
    @Schema(description = "username")
    @TableField("username")
    private String username;

    @Schema(description = "password")
    @TableField("password")
    private String password;

    @Schema(description = "nickName")
    @TableField("nick_name")
    private String nickName;

    @Schema(description = "phone")
    @TableField("phone")
    private String phone;

    @Schema(description = "email")
    @Email
    private String email;


}
