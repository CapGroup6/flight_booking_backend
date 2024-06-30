package fdu.capstone.system.module.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

/**
 * Author : Liping Yin
 * Date : 6/28/24
 */

@Data
@Builder
@TableName("user_preference")
public class UserPreferenceEntity extends CommonField{

    private Long userId;

    private String preferenceName;
    private String preferenceValue;
}
