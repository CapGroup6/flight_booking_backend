package fdu.capstone.system.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fdu.capstone.system.module.entity.UserSearchLog;
import fdu.capstone.system.module.entity.dto.UserSearchLogRequest;

import java.util.List;

/**
 * Author: Liping Yin
 * Date: 2024/6/12
 */
public interface UserSearchLogService extends IService<UserSearchLog> {

    public List<UserSearchLog> listUserSearchLogByUserId(Long userId);

    public void saveUserSearchLog(UserSearchLogRequest userSearchLogRequest);


}
