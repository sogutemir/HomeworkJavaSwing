package com.arel.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Öğretim üyelerinin müsait olduğu zamanları temsil eden sınıf
 */
public class Musaitlik {
    private int id;
    private int ogretimUyesiId;
    private DayOfWeek gun;
    private LocalDate tarih;
    private LocalTime baslangicSaati;
    private LocalTime bitisSaati;
    private boolean tekrarEden;
    
    // İlişki
    private Kullanici ogretimUyesi;
    
    public Musaitlik() {
    }
    
    public Musaitlik(int id, int ogretimUyesiId, DayOfWeek gun, LocalDate tarih, LocalTime baslangicSaati, LocalTime bitisSaati, boolean tekrarEden) {
        this.id = id;
        this.ogretimUyesiId = ogretimUyesiId;
        this.gun = gun;
        this.tarih = tarih;
        this.baslangicSaati = baslangicSaati;
        this.bitisSaati = bitisSaati;
        this.tekrarEden = tekrarEden;
    }
    
    // Getter ve Setter metodları
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getOgretimUyesiId() {
        return ogretimUyesiId;
    }
    
    public void setOgretimUyesiId(int ogretimUyesiId) {
        this.ogretimUyesiId = ogretimUyesiId;
    }
    
    public DayOfWeek getGun() {
        return gun;
    }
    
    public void setGun(DayOfWeek gun) {
        this.gun = gun;
    }
    
    public LocalDate getTarih() {
        return tarih;
    }
    
    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }
    
    public LocalTime getBaslangicSaati() {
        return baslangicSaati;
    }
    
    public void setBaslangicSaati(LocalTime baslangicSaati) {
        this.baslangicSaati = baslangicSaati;
    }
    
    public LocalTime getBitisSaati() {
        return bitisSaati;
    }
    
    public void setBitisSaati(LocalTime bitisSaati) {
        this.bitisSaati = bitisSaati;
    }
    
    public boolean isTekrarEden() {
        return tekrarEden;
    }
    
    public void setTekrarEden(boolean tekrarEden) {
        this.tekrarEden = tekrarEden;
    }
    
    public Kullanici getOgretimUyesi() {
        return ogretimUyesi;
    }
    
    public void setOgretimUyesi(Kullanici ogretimUyesi) {
        this.ogretimUyesi = ogretimUyesi;
        if (ogretimUyesi != null) {
            this.ogretimUyesiId = ogretimUyesi.getId();
        }
    }
    
    @Override
    public String toString() {
        if (tekrarEden) {
            return String.format("%s günü %s - %s", gun, baslangicSaati, bitisSaati);
        } else {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            return tarih.format(dateFormatter) + " " + baslangicSaati + " - " + bitisSaati;
        }
    }
    
    /**
     * Türkçe gün adını döndürür
     */
    public String getGunAdi() {
        if (gun == null) return "";
        switch(gun) {
            case MONDAY: return "Pazartesi";
            case TUESDAY: return "Salı";
            case WEDNESDAY: return "Çarşamba";
            case THURSDAY: return "Perşembe";
            case FRIDAY: return "Cuma";
            case SATURDAY: return "Cumartesi";
            case SUNDAY: return "Pazar";
            default: return "";
        }
    }
}
