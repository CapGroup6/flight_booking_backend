package fdu.capstone.system.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fdu.capstone.system.module.entity.SysUser;
import fdu.capstone.system.module.entity.dto.LoginUserRequest;
import fdu.capstone.system.module.entity.dto.SysUserRequest;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
public interface SysUserService extends IService<SysUser> {


    public void adduser(SysUserRequest SysUserRequest);

    public void updateUser(Long id, SysUserRequest sysUserRequest);

    public SysUser getUserByUsername(String username);

    /**
     * 根据手机号查询
     *
     * @param phone
     * @return
     */
    public SysUser getUserByPhone(String phone);

    /**
     * 根据邮箱查询
     *
     * @param email
     * @return
     */
    public SysUser getUserByEmail(String email);

    public String login(LoginUserRequest loginUserRequest);

}
