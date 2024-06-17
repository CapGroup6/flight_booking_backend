package fdu.capstone.system.module.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fdu.capstone.constant.ResponseCode;
import fdu.capstone.system.module.entity.SysUser;
import fdu.capstone.system.module.dao.SysUserMapper;
import fdu.capstone.system.module.entity.dto.LoginUserRequest;
import fdu.capstone.system.module.entity.dto.SysUserRequest;
import fdu.capstone.exception.BaseException;
import fdu.capstone.system.module.service.SysUserService;
import fdu.capstone.util.HashUtils;
import fdu.capstone.util.JwtUtil;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Value("${system.config.auth.tokenSignSecret}")
    private String tokenSignSecret;

    @Value("${system.config.auth.tokenExpiredTime}")
    private Long tokenExpiredTime;
    @Override
    public String login(LoginUserRequest loginUserRequest) {
        SysUser sysUser = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginUserRequest.getUsername()));
        if (Objects.isNull(sysUser)) {
            throw new BaseException(ResponseCode.SYS_USER_NOT_EXISTS);
        }

        String encryptedPassword= HashUtils.getMD5HashWithSalt(loginUserRequest.getPassword(),sysUser.getSalt());

        if(!StringUtils.equals(encryptedPassword,sysUser.getPassword())){
            throw new BaseException(ResponseCode.SYS_USER_PASSWORD_ERROR);
        }


        Map<String, Object> info = new HashMap<>();
        info.put("username", sysUser.getUsername());
        info.put("userId", sysUser.getId());
        String token = JwtUtil.generateToken(sysUser.getUsername(), info, tokenSignSecret, tokenExpiredTime);

        return token;
    }

    @Override
    public void adduser(SysUserRequest sysUserRequest) {

        checkUniqueUsername(sysUserRequest.getUsername());

        checkUniquePhone(sysUserRequest.getPhone());

        SysUser sysUser = new SysUser();
        BeanUtil.copyProperties(sysUserRequest, sysUser);
        String salt = RandomStringUtils.randomAlphabetic(20);
        sysUser.setSalt(salt);
        String encryptedPassword = HashUtils.getMD5HashWithSalt(sysUser.getPassword(),salt);
        sysUser.setPassword(encryptedPassword);

        save(sysUser);

    }

    @Override
    public void updateUser(Long id, SysUserRequest sysUserRequest) {
        SysUser sysUser = getById(id);
        if (Objects.isNull(sysUser)) {
            throw new BaseException(ResponseCode.SYS_USER_NOT_EXISTS);
        }

        if (StringUtils.isNotBlank(sysUserRequest.getUsername())
                && StringUtils.isNotBlank(sysUser.getUsername())
                && !sysUser.getUsername().equals(sysUserRequest.getUsername())) {
            checkUniqueUsername(sysUserRequest.getUsername());
        }

        if (StringUtils.isNotBlank(sysUserRequest.getPhone())
                && StringUtils.isNotBlank(sysUser.getPhone())
                && !sysUser.getPhone().equals(sysUserRequest.getPhone())) {
            checkUniquePhone(sysUserRequest.getPhone());
        }

        if (StringUtils.isNotBlank(sysUserRequest.getEmail())
                && StringUtils.isNotBlank(sysUser.getEmail())
                && !sysUser.getEmail().equals(sysUserRequest.getEmail())) {
            checkUniqueEmail(sysUserRequest.getEmail());
        }
        BeanUtil.copyProperties(sysUserRequest, sysUser);

        if(StringUtils.isNotBlank(sysUserRequest.getPassword())){
            String encryptedPassword = HashUtils.getMD5HashWithSalt(sysUser.getPassword(),sysUser.getSalt());
            sysUser.setPassword(encryptedPassword);
        }

        updateById(sysUser);
    }

    @Override
    public SysUser getUserByUsername(String username) {
        SysUser sysUser = this.getOne(new QueryWrapper<SysUser>().eq("username",username));
        if (sysUser!=null){
            return sysUser;
        }else {
            return null;
        }
    }

    @Override
    public SysUser getUserByPhone(String phone) {
        SysUser sysUser = getOne(new QueryWrapper<SysUser>().eq("phone",phone));
        if (sysUser!=null){
            return sysUser;
        }else {
            return null;
        }
    }

    @Override
    public SysUser getUserByEmail(String email) {
        SysUser sysUser = getOne(new QueryWrapper<SysUser>().eq("email",email));
        if (sysUser!=null){
            return sysUser;
        }else {
            return null;
        }
    }

    private void checkUniqueUsername(String username) {
        SysUser userByUsername = getUserByUsername(username);
        if (Objects.nonNull(userByUsername)) {
            throw new BaseException(ResponseCode.SYS_USER_USERNAME_EXISTS);
        }
    }


    private void checkUniquePhone(String phone) {
        SysUser userByPhone = getUserByPhone(phone);
        if (Objects.nonNull(userByPhone)) {
            throw new  BaseException(ResponseCode.SYS_USER_PHONE_EXISTS);
        }
    }


    private void checkUniqueEmail(String email) {
        SysUser userByPhone = getUserByEmail(email);
        if (Objects.nonNull(userByPhone)) {
            throw new  BaseException(ResponseCode.SYS_USER_PHONE_EXISTS);
        }
    }
}
