package fdu.capstone.system.module.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Liping Yin
 * Date: 2024/6/5
 */
@Data
public class CommonField {
    @TableId(type= IdType.AUTO)
    private Long id;

    @Schema(hidden = true)
    @TableField(fill= FieldFill.INSERT)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateDeserializer.class)
    @JsonFormat(pattern="yyyy-mm-dd HH:mm:ss")
    private LocalDateTime createTime;


    @Schema(hidden = true)
    @TableField(fill=FieldFill.INSERT_UPDATE)
    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using=LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-mm-dd HH:mm:ss")
    private LocalDateTime updateTime;


}
