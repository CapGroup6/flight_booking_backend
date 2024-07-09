package fdu.capstone.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String addYears(String dateStr, int years) {
        // Define the date format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input date string to LocalDate
        LocalDateTime dateTime = LocalDateTime.parse(dateStr, formatter);

        // Add two years to the date-time
        LocalDateTime newDateTime = dateTime.plusYears(years);

        // Format the new date-time back to a string
        return newDateTime.format(formatter);
    }

    public static String convertToLocalDate(String dateTimeStr) {
        // Define the date-time format
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Parse the input date-time string to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr, dateTimeFormatter);

        // Convert LocalDateTime to LocalDate
        LocalDate date = dateTime.toLocalDate();

        // Define the date format
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Format the LocalDate to a string
        return date.format(dateFormatter);
    }



}
