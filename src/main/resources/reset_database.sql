-- Mevcut tabloları sil
DROP TABLE IF EXISTS bildirimler;
DROP TABLE IF EXISTS randevular;
DROP TABLE IF EXISTS musaitlikler;
DROP TABLE IF EXISTS kullanicilar;

-- Kullanıcılar tablosunu oluştur
CREATE TABLE kullanicilar (
    id SERIAL PRIMARY KEY,
    ad VARCHAR(100) NOT NULL,
    soyad VARCHAR(100) NOT NULL,
    kullanici_adi VARCHAR(50) NOT NULL UNIQUE,
    sifre VARCHAR(100) NOT NULL,
    salt VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    rol VARCHAR(20) NOT NULL
);

-- Müsaitlikler tablosunu oluştur
CREATE TABLE musaitlikler (
    id SERIAL PRIMARY KEY,
    ogretim_uyesi_id INTEGER NOT NULL,
    gun INTEGER,
    tarih DATE,
    baslangic_saati TIME NOT NULL,
    bitis_saati TIME NOT NULL,
    tekrar_eden BOOLEAN NOT NULL DEFAULT true,
    FOREIGN KEY (ogretim_uyesi_id) REFERENCES kullanicilar(id) ON DELETE CASCADE,
    UNIQUE(ogretim_uyesi_id, gun, tarih, baslangic_saati, bitis_saati)
);

-- Randevular tablosunu oluştur
CREATE TABLE randevular (
    id SERIAL PRIMARY KEY,
    ogrenci_id INTEGER NOT NULL,
    ogretim_uyesi_id INTEGER NOT NULL,
    baslangic_zamani TIMESTAMP NOT NULL,
    bitis_zamani TIMESTAMP NOT NULL,
    konu VARCHAR(200),
    notlar TEXT,
    durum VARCHAR(20) NOT NULL,
    olusturulma_tarihi TIMESTAMP NOT NULL,
    FOREIGN KEY (ogrenci_id) REFERENCES kullanicilar(id) ON DELETE CASCADE,
    FOREIGN KEY (ogretim_uyesi_id) REFERENCES kullanicilar(id) ON DELETE CASCADE
);

-- Bildirimler tablosunu oluştur
CREATE TABLE bildirimler (
    id SERIAL PRIMARY KEY,
    kullanici_id INTEGER NOT NULL,
    randevu_id INTEGER,
    baslik VARCHAR(200) NOT NULL,
    mesaj TEXT NOT NULL,
    tip VARCHAR(50) NOT NULL,
    okundu BOOLEAN DEFAULT false,
    olusturulma_tarihi TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (kullanici_id) REFERENCES kullanicilar(id) ON DELETE CASCADE,
    FOREIGN KEY (randevu_id) REFERENCES randevular(id) ON DELETE CASCADE
); 