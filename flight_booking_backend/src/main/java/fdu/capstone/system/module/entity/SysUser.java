package fdu.capstone.system.module.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
@Data
@TableName("sys_user")
public class SysUser extends CommonField{
    private static final  long serialVersionUID=1L;

    @TableField("username")
    private String username;
    @TableField("password")
    private String password;
     @TableField("nick_name")
    private String nickName;
     @TableField("email")
     private String email;
     @TableField("phone")
     private String phone;
     @TableField("salt")
     private String salt;

}
