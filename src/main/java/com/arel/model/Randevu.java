package com.arel.model;

import java.time.LocalDateTime;

/**
 * Öğrenci ve öğretim üyesi arasındaki randevuları temsil eden sınıf
 */
public class Randevu {
    public enum Durum {
        BEKLEMEDE,
        ONAYLANDI,
        REDDEDILDI,
        IPTAL_EDILDI,
        TAMAMLANDI
    }
    
    private int id;
    private int ogrenciId;
    private int ogretimUyesiId;
    private LocalDateTime baslangicZamani;
    private LocalDateTime bitisZamani;
    private String konu;
    private String notlar;
    private Durum durum;
    private LocalDateTime olusturulmaTarihi;
    
    // İlişkili nesneler (veritabanından çekildikten sonra doldurulacak)
    private Kullanici ogrenci;
    private Kullanici ogretimUyesi;
    
    public Randevu() {
        this.olusturulmaTarihi = LocalDateTime.now();
        this.durum = Durum.BEKLEMEDE;
    }
    
    public Randevu(int id, int ogrenciId, int ogretimUyesiId, LocalDateTime baslangicZamani, 
                  LocalDateTime bitisZamani, String konu, String notlar, Durum durum) {
        this.id = id;
        this.ogrenciId = ogrenciId;
        this.ogretimUyesiId = ogretimUyesiId;
        this.baslangicZamani = baslangicZamani;
        this.bitisZamani = bitisZamani;
        this.konu = konu;
        this.notlar = notlar;
        this.durum = durum;
        this.olusturulmaTarihi = LocalDateTime.now();
    }
    
    // Getter ve Setter metodları
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getOgrenciId() {
        return ogrenciId;
    }
    
    public void setOgrenciId(int ogrenciId) {
        this.ogrenciId = ogrenciId;
    }
    
    public int getOgretimUyesiId() {
        return ogretimUyesiId;
    }
    
    public void setOgretimUyesiId(int ogretimUyesiId) {
        this.ogretimUyesiId = ogretimUyesiId;
    }
    
    public LocalDateTime getBaslangicZamani() {
        return baslangicZamani;
    }
    
    public void setBaslangicZamani(LocalDateTime baslangicZamani) {
        this.baslangicZamani = baslangicZamani;
    }
    
    public LocalDateTime getBitisZamani() {
        return bitisZamani;
    }
    
    public void setBitisZamani(LocalDateTime bitisZamani) {
        this.bitisZamani = bitisZamani;
    }
    
    public String getKonu() {
        return konu;
    }
    
    public void setKonu(String konu) {
        this.konu = konu;
    }
    
    public String getNotlar() {
        return notlar;
    }
    
    public void setNotlar(String notlar) {
        this.notlar = notlar;
    }
    
    public Durum getDurum() {
        return durum;
    }
    
    public void setDurum(Durum durum) {
        this.durum = durum;
    }
    
    public LocalDateTime getOlusturulmaTarihi() {
        return olusturulmaTarihi;
    }
    
    public void setOlusturulmaTarihi(LocalDateTime olusturulmaTarihi) {
        this.olusturulmaTarihi = olusturulmaTarihi;
    }
    
    public Kullanici getOgrenci() {
        return ogrenci;
    }
    
    public void setOgrenci(Kullanici ogrenci) {
        this.ogrenci = ogrenci;
        if (ogrenci != null) {
            this.ogrenciId = ogrenci.getId();
        }
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
    
    public int getSureDakika() {
        return bitisZamani != null && baslangicZamani != null 
               ? (int) java.time.Duration.between(baslangicZamani, bitisZamani).toMinutes() 
               : 0;
    }
}
