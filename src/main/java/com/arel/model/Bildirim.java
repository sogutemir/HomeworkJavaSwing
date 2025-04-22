package com.arel.model;

import java.time.LocalDateTime;

/**
 * Kullanıcılara gönderilen bildirimleri temsil eden sınıf
 */
public class Bildirim {
    public enum Tip {
        RANDEVU_OLUSTURULDU,
        RANDEVU_ONAYLANDI,
        RANDEVU_REDDEDILDI,
        RANDEVU_IPTAL_EDILDI,
        HATIRLATMA,
        SISTEM
    }
    
    private int id;
    private int kullaniciId;
    private int randevuId;  // İlgili randevu (opsiyonel, sistem bildirimleri için null olabilir)
    private String baslik;
    private String mesaj;
    private Tip tip;
    private boolean okundu;
    private LocalDateTime olusturulmaTarihi;
    
    // İlişki
    private Kullanici kullanici;
    private Randevu randevu;
    
    public Bildirim() {
        this.okundu = false;
        this.olusturulmaTarihi = LocalDateTime.now();
    }
    
    public Bildirim(int id, int kullaniciId, int randevuId, String baslik, String mesaj, Tip tip) {
        this.id = id;
        this.kullaniciId = kullaniciId;
        this.randevuId = randevuId;
        this.baslik = baslik;
        this.mesaj = mesaj;
        this.tip = tip;
        this.okundu = false;
        this.olusturulmaTarihi = LocalDateTime.now();
    }
    
    // Getter ve Setter metodları
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getKullaniciId() {
        return kullaniciId;
    }
    
    public void setKullaniciId(int kullaniciId) {
        this.kullaniciId = kullaniciId;
    }
    
    public int getRandevuId() {
        return randevuId;
    }
    
    public void setRandevuId(int randevuId) {
        this.randevuId = randevuId;
    }
    
    public String getBaslik() {
        return baslik;
    }
    
    public void setBaslik(String baslik) {
        this.baslik = baslik;
    }
    
    public String getMesaj() {
        return mesaj;
    }
    
    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }
    
    public Tip getTip() {
        return tip;
    }
    
    public void setTip(Tip tip) {
        this.tip = tip;
    }
    
    public boolean isOkundu() {
        return okundu;
    }
    
    public void setOkundu(boolean okundu) {
        this.okundu = okundu;
    }
    
    public LocalDateTime getOlusturulmaTarihi() {
        return olusturulmaTarihi;
    }
    
    public void setOlusturulmaTarihi(LocalDateTime olusturulmaTarihi) {
        this.olusturulmaTarihi = olusturulmaTarihi;
    }
    
    public Kullanici getKullanici() {
        return kullanici;
    }
    
    public void setKullanici(Kullanici kullanici) {
        this.kullanici = kullanici;
        if (kullanici != null) {
            this.kullaniciId = kullanici.getId();
        }
    }
    
    public Randevu getRandevu() {
        return randevu;
    }
    
    public void setRandevu(Randevu randevu) {
        this.randevu = randevu;
        if (randevu != null) {
            this.randevuId = randevu.getId();
        }
    }
}
