package fdu.capstone.system.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fdu.capstone.system.module.dao.UserPreferenceMapper;
import fdu.capstone.system.module.entity.UserPreferenceEntity;
import fdu.capstone.system.module.service.UserPreferenceService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */
@Service
public class UserPreferenceServiceImpl extends ServiceImpl<UserPreferenceMapper, UserPreferenceEntity> implements UserPreferenceService {


    public boolean saveUserPreference(UserPreferenceEntity userPreferenceEntity) {
        return this.saveOrUpdate(userPreferenceEntity);
    }


    @Override
    public List<UserPreferenceEntity> getUserPreferenceByUserId(Long userId) {
        List<UserPreferenceEntity> userPreferenceEntityList = this.list(new QueryWrapper<UserPreferenceEntity>().eq("user_id", userId));

        return userPreferenceEntityList;
    }

    @Override
    public UserPreferenceEntity getUserPreferenceByUserIdAndPreference(Long userId, String preferenceName) {
        List<UserPreferenceEntity> userPreferenceEntityList = this.list(new QueryWrapper<UserPreferenceEntity>().eq("user_id", userId)
                .eq("preference_name", preferenceName));

        if (userPreferenceEntityList != null && userPreferenceEntityList.size() > 0) {
            return userPreferenceEntityList.get(0);
        }

        return null;
    }
}
