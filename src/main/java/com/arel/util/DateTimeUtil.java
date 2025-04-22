package com.arel.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Tarih ve zaman işlemleri için yardımcı sınıf
 */
public class DateTimeUtil {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    
    /**
     * Tarihi formatlar
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * Saati formatlar
     */
    public static String formatTime(LocalTime time) {
        if (time == null) return "";
        return time.format(TIME_FORMATTER);
    }
    
    /**
     * Tarih ve saati formatlar
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    /**
     * String'den LocalDate'e dönüştürür
     */
    public static LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) return null;
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
    
    /**
     * String'den LocalTime'a dönüştürür
     */
    public static LocalTime parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) return null;
        return LocalTime.parse(timeString, TIME_FORMATTER);
    }
    
    /**
     * String'den LocalDateTime'a dönüştürür
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
    }
    
    /**
     * Belirli bir zaman aralığını istenen dakika dilimlerine bölerek liste halinde döner
     * Örneğin: 08:00 - 10:00 aralığını 20'şer dakikalık dilimlere bölmek için
     */
    public static List<LocalTime> getTimeSlots(LocalTime startTime, LocalTime endTime, int slotMinutes) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime currentTime = startTime;
        
        while (currentTime.isBefore(endTime)) {
            slots.add(currentTime);
            currentTime = currentTime.plusMinutes(slotMinutes);
        }
        
        return slots;
    }
    
    /**
     * Haftanın gününün Türkçe karşılığını döndürür
     */
    public static String getDayOfWeekInTurkish(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "Pazartesi";
            case TUESDAY:
                return "Salı";
            case WEDNESDAY:
                return "Çarşamba";
            case THURSDAY:
                return "Perşembe";
            case FRIDAY:
                return "Cuma";
            case SATURDAY:
                return "Cumartesi";
            case SUNDAY:
                return "Pazar";
            default:
                return "";
        }
    }
    
    /**
     * Belirli bir tarih için haftanın gününü Türkçe olarak döndürür
     */
    public static String getDayOfWeekInTurkish(LocalDate date) {
        return getDayOfWeekInTurkish(date.getDayOfWeek());
    }
    
    /**
     * İki tarih arasında gün farkını hesaplar
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }
    
    /**
     * İki zaman arasında dakika farkını hesaplar
     */
    public static long minutesBetween(LocalTime startTime, LocalTime endTime) {
        return java.time.temporal.ChronoUnit.MINUTES.between(startTime, endTime);
    }
    
    /**
     * İki tarih/saat arasında dakika farkını hesaplar
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return java.time.temporal.ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }
    
    /**
     * Geçerli tarihin gelecekte olup olmadığını kontrol eder
     */
    public static boolean isFutureDate(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * Geçerli tarih/saatin gelecekte olup olmadığını kontrol eder
     */
    public static boolean isFutureDateTime(LocalDateTime dateTime) {
        return dateTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * Bugünün tarihini döndürür
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
    
    /**
     * Şimdiki zamanı döndürür
     */
    public static LocalTime now() {
        return LocalTime.now();
    }
    
    /**
     * Bugünün tarih ve saatini döndürür
     */
    public static LocalDateTime todayNow() {
        return LocalDateTime.now();
    }
}
