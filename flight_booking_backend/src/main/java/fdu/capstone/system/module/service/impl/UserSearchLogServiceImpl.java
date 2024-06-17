package fdu.capstone.system.module.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fdu.capstone.system.module.dao.UserSearchLogMapper;
import fdu.capstone.system.module.entity.SysUser;
import fdu.capstone.system.module.entity.UserSearchLog;
import fdu.capstone.system.module.entity.dto.UserSearchLogRequest;
import fdu.capstone.system.module.service.UserSearchLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author: Liping Yin
 * Date: 2024/6/12
 */

@Slf4j
@Service
public class UserSearchLogServiceImpl extends ServiceImpl<UserSearchLogMapper, UserSearchLog> implements UserSearchLogService {
    @Override
    public List<UserSearchLog> listUserSearchLogByUserId(Long userId) {
        return this.list(new QueryWrapper<UserSearchLog>().eq("userid",userId));
    }

    @Override
    public void saveUserSearchLog(UserSearchLogRequest userSearchLogRequest) {
        UserSearchLog userSearchLog = new UserSearchLog();
        BeanUtil.copyProperties(userSearchLogRequest, userSearchLog);
        this.save(userSearchLog);
    }
}
