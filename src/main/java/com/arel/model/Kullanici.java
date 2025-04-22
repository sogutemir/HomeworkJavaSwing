package com.arel.model;

/**
 * Sisteme giriş yapan kullanıcıları temsil eden sınıf
 */
public class Kullanici {
    public enum Rol {
        OGRENCI,
        OGRETIM_UYESI,
        ADMIN
    }
    
    private int id;
    private String kullaniciAdi;
    private String sifre;
    private String salt;
    private String ad;
    private String soyad;
    private String email;
    private Rol rol;
    
    public Kullanici() {
    }
    
    public Kullanici(int id, String kullaniciAdi, String sifre, String salt, String ad, String soyad, String email, Rol rol) {
        this.id = id;
        this.kullaniciAdi = kullaniciAdi;
        this.sifre = sifre;
        this.salt = salt;
        this.ad = ad;
        this.soyad = soyad;
        this.email = email;
        this.rol = rol;
    }
    
    // Getter ve Setter metodları
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getKullaniciAdi() {
        return kullaniciAdi;
    }
    
    public void setKullaniciAdi(String kullaniciAdi) {
        this.kullaniciAdi = kullaniciAdi;
    }
    
    public String getSifre() {
        return sifre;
    }
    
    public void setSifre(String sifre) {
        this.sifre = sifre;
    }
    
    public String getSalt() {
        return salt;
    }
    
    public void setSalt(String salt) {
        this.salt = salt;
    }
    
    public String getAd() {
        return ad;
    }
    
    public void setAd(String ad) {
        this.ad = ad;
    }
    
    public String getSoyad() {
        return soyad;
    }
    
    public void setSoyad(String soyad) {
        this.soyad = soyad;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    public String getTamAd() {
        return ad + " " + soyad;
    }
    
    @Override
    public String toString() {
        return getTamAd();
    }
}
