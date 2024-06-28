package fdu.capstone.system.module.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * Author: Liping Yin
 * Date: 2024/6/12
 */

@Data
public class UserSearchLogRequest {

    @Hidden
    private Long  userId;
    @NotBlank(message = "departure should be not null")
    private String departure;
    @NotBlank(message = "destination should be not null")
    private String destination;

    @NotBlank(message = "departureDate should be not null")
//    @JsonSerialize(using= LocalDateTimeSerializer.class)
//    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-mm-dd HH:mm:ss")
    private LocalDateTime departureDate;

    @NotBlank(message = "returnDate should be not null")
//    @JsonSerialize(using= LocalDateTimeSerializer.class)
//    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
//    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-mm-dd HH:mm:ss")
    private LocalDateTime returnDate;

    @NotBlank(message = "roundTrip should be not null")
    private int roundTrip;

    @NotBlank(message = "directFlight should be not null")
    private int directFlight;

    private int adultNum;

    private int childrenNum;


}
