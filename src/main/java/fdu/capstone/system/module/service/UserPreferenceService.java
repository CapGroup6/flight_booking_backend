package fdu.capstone.system.module.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fdu.capstone.system.module.entity.UserPreferenceEntity;

import java.util.List;

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */
public interface UserPreferenceService extends IService<UserPreferenceEntity> {
    public boolean saveUserPreference(UserPreferenceEntity userPreferenceEntity);
    public List<UserPreferenceEntity> getUserPreferenceByUserId(Long userId);

}
