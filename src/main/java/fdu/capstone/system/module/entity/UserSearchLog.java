package fdu.capstone.system.module.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Author: Liping Yin
 * Date: 2024/6/12
 */

@Data
@TableName("user_search_log")
public class UserSearchLog extends CommonField{


    @TableField("userid")
    private Long userId;

    private String departure;
    private String destination;

    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-mm-dd HH:mm:ss")
    @TableField("departure_date")
    private LocalDateTime departureDate;

    @JsonSerialize(using= LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonFormat(pattern = "yyyy-mm-dd HH:mm:ss")
    @TableField("return_date")
    private LocalDateTime returnDate;

    @TableField("round_trip")
    private int roundTrip;

    @TableField("direct_flight")
    private int directFlight;

    @TableField("adult_num")
    private int adultNum;

    @TableField("children_num")
    private int childrenNum;
}
