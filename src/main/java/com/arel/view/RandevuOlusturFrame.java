package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.MusaitlikDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
import com.arel.model.Musaitlik;
import com.arel.model.Randevu;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RandevuOlusturFrame extends JFrame {
    private final Kullanici ogrenci;
    private final RandevuDAO randevuDAO;
    private final MusaitlikDAO musaitlikDAO;
    private final KullaniciDAO kullaniciDAO;
    
    private JComboBox<Kullanici> cmbOgretimUyesi;
    private CalendarPanel calendarPanel;
    private JToggleButton btnGunlukGorunum;
    private JToggleButton btnHaftalikGorunum;
    private JButton btnOncekiGun;
    private JButton btnSonrakiGun;
    private JTextField txtKonu;
    private JTextArea txtNotlar;
    private JButton btnRandevuOlustur;
    private JButton btnIptal;
    
    // Yeni bileşenler
    private JDateChooser dateChooser;
    private JComboBox<String> cmbSaatler;
    private List<LocalTime> musaitSaatler = new ArrayList<>();
    
    public RandevuOlusturFrame(Kullanici ogrenci) {
        this.ogrenci = ogrenci;
        this.randevuDAO = new RandevuDAO();
        this.musaitlikDAO = new MusaitlikDAO();
        this.kullaniciDAO = new KullaniciDAO();
        
        initComponents();
        setupListeners();
        ogretimUyeleriniYukle();
    }
    
    private void initComponents() {
        setTitle("Yeni Randevu Oluştur");
        setSize(1000, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setBackground(new Color(240, 240, 240));
        
        // Üst Panel - Öğretim Üyesi Seçimi ve Görünüm Kontrolleri
        JPanel ustPanel = new JPanel();
        ustPanel.setLayout(new BoxLayout(ustPanel, BoxLayout.Y_AXIS));
        ustPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(5, 5, 10, 5)));
        ustPanel.setBackground(new Color(245, 245, 245));
        
        // Başlık ve açıklama
        JPanel baslikPanel = new JPanel(new BorderLayout());
        baslikPanel.setOpaque(false);
        JLabel lblBaslik = new JLabel("Randevu Oluşturma");
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 18));
        lblBaslik.setForeground(new Color(70, 130, 180));
        
        JLabel lblAciklama = new JLabel("Lütfen bir öğretim üyesi seçin ve uygun tarih/saat belirleyin");
        lblAciklama.setFont(new Font("Arial", Font.PLAIN, 12));
        lblAciklama.setForeground(Color.DARK_GRAY);
        
        baslikPanel.add(lblBaslik, BorderLayout.WEST);
        baslikPanel.add(lblAciklama, BorderLayout.SOUTH);
        ustPanel.add(baslikPanel);
        ustPanel.add(Box.createVerticalStrut(10));
        
        // Kontrol paneli (öğretim üyesi seçimi ve görünüm kontrolleri)
        JPanel kontrolPanel = new JPanel(new BorderLayout(20, 0));
        kontrolPanel.setOpaque(false);
        
        // Öğretim Üyesi Seçimi
        JPanel ogretimUyesiPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ogretimUyesiPanel.setOpaque(false);
        JLabel lblOgretimUyesi = new JLabel("Öğretim Üyesi:");
        lblOgretimUyesi.setFont(new Font("Arial", Font.BOLD, 12));
        cmbOgretimUyesi = new JComboBox<>();
        cmbOgretimUyesi.setPreferredSize(new Dimension(200, 30));
        ogretimUyesiPanel.add(lblOgretimUyesi);
        ogretimUyesiPanel.add(cmbOgretimUyesi);
        kontrolPanel.add(ogretimUyesiPanel, BorderLayout.WEST);
        
        // Görünüm Kontrolleri
        JPanel gorunumPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        gorunumPanel.setOpaque(false);
        
        btnOncekiGun = new JButton("◀");
        btnOncekiGun.setFocusPainted(false);
        styleButton(btnOncekiGun);
        
        btnSonrakiGun = new JButton("▶");
        btnSonrakiGun.setFocusPainted(false);
        styleButton(btnSonrakiGun);
        
        ButtonGroup gorunumGroup = new ButtonGroup();
        btnGunlukGorunum = new JToggleButton("Günlük");
        btnGunlukGorunum.setFocusPainted(false);
        styleToggleButton(btnGunlukGorunum);
        
        btnHaftalikGorunum = new JToggleButton("Haftalık");
        btnHaftalikGorunum.setFocusPainted(false);
        styleToggleButton(btnHaftalikGorunum);
        
        gorunumGroup.add(btnGunlukGorunum);
        gorunumGroup.add(btnHaftalikGorunum);
        btnHaftalikGorunum.setSelected(true);
        
        gorunumPanel.add(btnOncekiGun);
        gorunumPanel.add(btnGunlukGorunum);
        gorunumPanel.add(btnHaftalikGorunum);
        gorunumPanel.add(btnSonrakiGun);
        kontrolPanel.add(gorunumPanel, BorderLayout.CENTER);
        
        ustPanel.add(kontrolPanel);
        
        // Takvim Paneli
        calendarPanel = new CalendarPanel();
        JScrollPane calendarScroll = new JScrollPane(calendarPanel);
        calendarScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        calendarScroll.setBorder(BorderFactory.createEmptyBorder());
        
        // Takvim panel başlığı
        JPanel calendarHeaderPanel = new JPanel(new BorderLayout());
        calendarHeaderPanel.setBackground(new Color(70, 130, 180));
        JLabel lblCalendarTitle = new JLabel("Müsaitlik Takvimi", JLabel.CENTER);
        lblCalendarTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblCalendarTitle.setForeground(Color.WHITE);
        lblCalendarTitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        calendarHeaderPanel.add(lblCalendarTitle, BorderLayout.CENTER);
        
        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.add(calendarHeaderPanel, BorderLayout.NORTH);
        calendarContainer.add(calendarScroll, BorderLayout.CENTER);
        calendarContainer.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Alt Panel - Randevu Detayları
        JPanel altPanel = new JPanel();
        altPanel.setLayout(new BoxLayout(altPanel, BoxLayout.Y_AXIS));
        altPanel.setBackground(new Color(245, 245, 245));
        altPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 130, 180), 1, true),
                "Randevu Detayları"
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Tarih ve Saat Seçimi Panel
        JPanel tarihSaatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        tarihSaatPanel.setOpaque(false);
        
        JLabel lblTarih = new JLabel("Tarih:");
        lblTarih.setFont(new Font("Arial", Font.BOLD, 12));
        lblTarih.setForeground(new Color(70, 70, 70));
        tarihSaatPanel.add(lblTarih);
        
        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(130, 30));
        dateChooser.setMinSelectableDate(new Date());
        dateChooser.setDate(new Date());
        dateChooser.setFont(new Font("Arial", Font.PLAIN, 12));
        tarihSaatPanel.add(dateChooser);
        
        JLabel lblSaat = new JLabel("Saat:");
        lblSaat.setFont(new Font("Arial", Font.BOLD, 12));
        lblSaat.setForeground(new Color(70, 70, 70));
        tarihSaatPanel.add(lblSaat);
        
        cmbSaatler = new JComboBox<>();
        cmbSaatler.setPreferredSize(new Dimension(90, 30));
        cmbSaatler.setFont(new Font("Arial", Font.PLAIN, 12));
        tarihSaatPanel.add(cmbSaatler);
        
        // Bilgi etiketi
        JLabel lblBilgi = new JLabel("(Randevular 20 dakikalık dilimler halinde düzenlenmektedir)");
        lblBilgi.setFont(new Font("Arial", Font.ITALIC, 11));
        lblBilgi.setForeground(new Color(120, 120, 120));
        
        JPanel tarihSaatContainer = new JPanel(new BorderLayout());
        tarihSaatContainer.setOpaque(false);
        tarihSaatContainer.add(tarihSaatPanel, BorderLayout.WEST);
        tarihSaatContainer.add(lblBilgi, BorderLayout.EAST);
        tarihSaatContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        altPanel.add(tarihSaatContainer);
        altPanel.add(Box.createVerticalStrut(10));
        
        // Konu Panel
        JPanel konuPanel = new JPanel(new BorderLayout(10, 0));
        konuPanel.setOpaque(false);
        JLabel lblKonu = new JLabel("Randevu Konusu:");
        lblKonu.setFont(new Font("Arial", Font.BOLD, 12));
        lblKonu.setForeground(new Color(70, 70, 70));
        
        txtKonu = new JTextField(40);
        txtKonu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        txtKonu.setFont(new Font("Arial", Font.PLAIN, 12));
        
        konuPanel.add(lblKonu, BorderLayout.WEST);
        konuPanel.add(txtKonu, BorderLayout.CENTER);
        konuPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        altPanel.add(konuPanel);
        altPanel.add(Box.createVerticalStrut(10));
        
        // Notlar Panel
        JPanel notlarPanel = new JPanel(new BorderLayout(5, 5));
        notlarPanel.setOpaque(false);
        JLabel lblNotlar = new JLabel("Notlar:");
        lblNotlar.setFont(new Font("Arial", Font.BOLD, 12));
        lblNotlar.setForeground(new Color(70, 70, 70));
        
        txtNotlar = new JTextArea(4, 40);
        txtNotlar.setLineWrap(true);
        txtNotlar.setWrapStyleWord(true);
        txtNotlar.setFont(new Font("Arial", Font.PLAIN, 12));
        txtNotlar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180)),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        JScrollPane notlarScroll = new JScrollPane(txtNotlar);
        notlarScroll.setBorder(BorderFactory.createEmptyBorder());
        
        notlarPanel.add(lblNotlar, BorderLayout.NORTH);
        notlarPanel.add(notlarScroll, BorderLayout.CENTER);
        altPanel.add(notlarPanel);
        altPanel.add(Box.createVerticalStrut(15));
        
        // Butonlar Panel
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        butonlarPanel.setOpaque(false);
        
        btnRandevuOlustur = new JButton("Randevu Oluştur");
        btnRandevuOlustur.setFont(new Font("Arial", Font.BOLD, 12));
        btnRandevuOlustur.setBackground(new Color(70, 130, 180));
        btnRandevuOlustur.setForeground(Color.WHITE);
        btnRandevuOlustur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60, 120, 170)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        btnRandevuOlustur.setFocusPainted(false);
        
        btnIptal = new JButton("İptal");
        btnIptal.setFont(new Font("Arial", Font.PLAIN, 12));
        btnIptal.setBackground(new Color(240, 240, 240));
        btnIptal.setForeground(new Color(80, 80, 80));
        btnIptal.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)));
        btnIptal.setFocusPainted(false);
        
        butonlarPanel.add(btnRandevuOlustur);
        butonlarPanel.add(btnIptal);
        
        // Renk açıklamaları
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        legendPanel.setOpaque(false);
        
        JPanel musaitLegend = new JPanel();
        musaitLegend.setPreferredSize(new Dimension(15, 15));
        musaitLegend.setBackground(new Color(144, 238, 144)); // Light Green
        
        JPanel doluLegend = new JPanel();
        doluLegend.setPreferredSize(new Dimension(15, 15));
        doluLegend.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        
        JPanel secilenLegend = new JPanel();
        secilenLegend.setPreferredSize(new Dimension(15, 15));
        secilenLegend.setBackground(new Color(255, 255, 0, 100)); // Yellow (transparent)
        
        legendPanel.add(musaitLegend);
        legendPanel.add(new JLabel("Müsait"));
        legendPanel.add(doluLegend);
        legendPanel.add(new JLabel("Dolu"));
        legendPanel.add(secilenLegend);
        legendPanel.add(new JLabel("Seçilen"));
        
        // Alt panel container
        JPanel altContainer = new JPanel(new BorderLayout(0, 10));
        altContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        altContainer.setOpaque(false);
        altContainer.add(altPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(legendPanel, BorderLayout.WEST);
        bottomPanel.add(butonlarPanel, BorderLayout.EAST);
        
        altContainer.add(bottomPanel, BorderLayout.SOUTH);
        
        // Panelleri ana panele ekle
        panel.add(ustPanel, BorderLayout.NORTH);
        panel.add(calendarContainer, BorderLayout.CENTER);
        panel.add(altContainer, BorderLayout.SOUTH);
        
        getContentPane().add(panel);
        
        // Minimum boyut ayarla
        setMinimumSize(new Dimension(800, 700));
        
        // Renklendirme
        getContentPane().setBackground(new Color(240, 240, 240));
    }
    
    private void setupListeners() {
        cmbOgretimUyesi.addActionListener(e -> {
            if (cmbOgretimUyesi.getSelectedItem() != null && cmbOgretimUyesi.getSelectedIndex() > 0) {
                Kullanici secilenOgretimUyesi = (Kullanici) cmbOgretimUyesi.getSelectedItem();
                yeniMusaitlikleriYukle(secilenOgretimUyesi.getId());
            } else {
                calendarPanel.clearAvailabilities();
                calendarPanel.repaint();
            }
        });
        
        dateChooser.addPropertyChangeListener("date", e -> {
            if (dateChooser.getDate() != null && cmbOgretimUyesi.getSelectedIndex() > 0) {
                musaitSaatleriGuncelle();
            }
        });
        
        btnGunlukGorunum.addActionListener(e -> {
            if (btnGunlukGorunum.isSelected()) {
                calendarPanel.setViewType(CalendarPanel.ViewType.DAILY);
                if (cmbOgretimUyesi.getSelectedIndex() > 0) {
                    yeniMusaitlikleriYukle(((Kullanici) cmbOgretimUyesi.getSelectedItem()).getId());
                }
            }
        });
        
        btnHaftalikGorunum.addActionListener(e -> {
            if (btnHaftalikGorunum.isSelected()) {
                calendarPanel.setViewType(CalendarPanel.ViewType.WEEKLY);
                if (cmbOgretimUyesi.getSelectedIndex() > 0) {
                    yeniMusaitlikleriYukle(((Kullanici) cmbOgretimUyesi.getSelectedItem()).getId());
                }
            }
        });
        
        btnOncekiGun.addActionListener(e -> {
            if (calendarPanel.getViewType() == CalendarPanel.ViewType.DAILY) {
                calendarPanel.previousDay();
            } else {
                calendarPanel.previousWeek();
            }
            if (cmbOgretimUyesi.getSelectedIndex() > 0) {
                yeniMusaitlikleriYukle(((Kullanici) cmbOgretimUyesi.getSelectedItem()).getId());
            }
        });
        
        btnSonrakiGun.addActionListener(e -> {
            if (calendarPanel.getViewType() == CalendarPanel.ViewType.DAILY) {
                calendarPanel.nextDay();
            } else {
                calendarPanel.nextWeek();
            }
            if (cmbOgretimUyesi.getSelectedIndex() > 0) {
                yeniMusaitlikleriYukle(((Kullanici) cmbOgretimUyesi.getSelectedItem()).getId());
            }
        });
        
        btnRandevuOlustur.addActionListener(e -> randevuOlustur());
        btnIptal.addActionListener(e -> dispose());
    }
    
    private void ogretimUyeleriniYukle() {
        try {
            List<Kullanici> ogretimUyeleri = kullaniciDAO.getRoleGoreKullanicilar(Kullanici.Rol.OGRETIM_UYESI);
            DefaultComboBoxModel<Kullanici> model = new DefaultComboBoxModel<>();
            
            // Varsayılan seçenek ekle
            model.addElement(new Kullanici() {
                @Override
                public String toString() {
                    return "Öğretim Üyesi Seçiniz";
                }
            });
            
            // Öğretim üyelerini ekle
            for (Kullanici ogretimUyesi : ogretimUyeleri) {
                model.addElement(ogretimUyesi);
            }
            
            cmbOgretimUyesi.setModel(model);
            
            // Varsayılan seçeneği seç
            cmbOgretimUyesi.setSelectedIndex(0);
            
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                "Öğretim üyeleri yüklenirken hata oluştu: " + ex.getMessage(),
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    private void yeniMusaitlikleriYukle(int ogretimUyesiId) {
        calendarPanel.clearAvailabilities();
        
        if (ogretimUyesiId <= 0) {
            return;
        }

        try {
            MusaitlikDAO musaitlikDAO = new MusaitlikDAO();
            List<Musaitlik> musaitlikler = musaitlikDAO.ogretimUyesininMusaitlikleriniGetir(ogretimUyesiId);
            
            // Müsait saatleri takvime yükle
            for (Musaitlik musaitlik : musaitlikler) {
                calendarPanel.addAvailableTime(musaitlik.getBaslangicSaati());
            }

            // Mevcut randevuları yükle
            RandevuDAO randevuDAO = new RandevuDAO();
            List<Randevu> randevular = randevuDAO.ogretimUyesininRandevulariniGetir(ogretimUyesiId);
            
            // Aktif randevuları takvime yükle
            for (Randevu randevu : randevular) {
                if (randevu.getDurum() != Randevu.Durum.IPTAL_EDILDI && 
                    randevu.getDurum() != Randevu.Durum.REDDEDILDI) {
                    calendarPanel.addAppointment(randevu);
                }
            }

            calendarPanel.repaint();
            
            // Tarih seçili ise müsait saatleri güncelle
            if (dateChooser.getDate() != null) {
                musaitSaatleriGuncelle();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Müsaitlik bilgileri yüklenirken bir hata oluştu: " + ex.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void musaitSaatleriGuncelle() {
        // ComboBox'ı temizle
        cmbSaatler.removeAllItems();
        musaitSaatler.clear();
        
        // Seçilen tarih ve öğretim üyesi kontrolü
        if (dateChooser.getDate() == null || cmbOgretimUyesi.getSelectedIndex() <= 0) {
            return;
        }
        
        try {
            // Seçili tarihi LocalDate'e dönüştür
            LocalDate secilenTarih = dateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            int ogretimUyesiId = ((Kullanici) cmbOgretimUyesi.getSelectedItem()).getId();
            
            // Öğretim üyesinin müsaitliklerini al
            List<Musaitlik> tumMusaitlikler = musaitlikDAO.ogretimUyesininMusaitlikleriniGetir(ogretimUyesiId);
            
            // Seçilen günün haftanın hangi günü olduğunu bul
            DayOfWeek secilenGun = secilenTarih.getDayOfWeek();
            
            // Müsait saatleri filtrele
            for (Musaitlik musaitlik : tumMusaitlikler) {
                if (musaitlik.isTekrarEden() && musaitlik.getGun() == secilenGun) {
                    // Tekrar eden müsaitlik ve seçilen gün uyuyor
                    ekleMusaitSaatler(musaitlik);
                } else if (!musaitlik.isTekrarEden() && musaitlik.getTarih() != null && 
                           musaitlik.getTarih().equals(secilenTarih)) {
                    // Tek seferlik müsaitlik ve tarih uyuyor
                    ekleMusaitSaatler(musaitlik);
                }
            }
            
            // Mevcut randevuları kontrol et
            List<Randevu> randevular = randevuDAO.ogretimUyesininRandevulariniGetir(ogretimUyesiId);
            
            // Çakışan saatleri kaldır
            List<LocalTime> kullanilmisSaatler = new ArrayList<>();
            
            for (Randevu randevu : randevular) {
                // Sadece aktif randevuları kontrol et
                if (randevu.getDurum() != Randevu.Durum.IPTAL_EDILDI && 
                    randevu.getDurum() != Randevu.Durum.REDDEDILDI) {
                    
                    // Randevu tarihi seçilen tarihle aynı mı kontrol et
                    if (randevu.getBaslangicZamani().toLocalDate().equals(secilenTarih)) {
                        kullanilmisSaatler.add(randevu.getBaslangicZamani().toLocalTime());
                    }
                }
            }
            
            // Kullanılmış saatleri müsait saatlerden çıkar
            musaitSaatler.removeAll(kullanilmisSaatler);
            
            // Müsait saatleri ComboBox'a ekle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            
            for (LocalTime saat : musaitSaatler) {
                cmbSaatler.addItem(saat.format(formatter));
            }
            
            if (cmbSaatler.getItemCount() == 0) {
                cmbSaatler.addItem("Müsait saat yok");
                btnRandevuOlustur.setEnabled(false);
            } else {
                btnRandevuOlustur.setEnabled(true);
            }
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Müsait saatler yüklenirken bir hata oluştu: " + ex.getMessage(), 
                "Hata", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void ekleMusaitSaatler(Musaitlik musaitlik) {
        LocalTime baslangic = musaitlik.getBaslangicSaati();
        LocalTime bitis = musaitlik.getBitisSaati();
        
        // 20 dakikalık dilimlerle müsait saatleri ekle
        LocalTime current = baslangic;
        while (current.plusMinutes(20).compareTo(bitis) <= 0) {
            musaitSaatler.add(current);
            current = current.plusMinutes(20);
        }
    }
    
    private void randevuOlustur() {
        if (cmbOgretimUyesi.getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Lütfen bir öğretim üyesi seçiniz.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this,
                "Lütfen bir tarih seçiniz.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cmbSaatler.getSelectedItem() == null || cmbSaatler.getSelectedItem().toString().equals("Müsait saat yok")) {
            JOptionPane.showMessageDialog(this,
                "Lütfen müsait bir saat seçiniz.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (txtKonu.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Lütfen randevu konusunu giriniz.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Seçilen tarihi alalım
            LocalDate secilenTarih = dateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            
            // Seçilen saati alalım
            String secilenSaatStr = cmbSaatler.getSelectedItem().toString();
            LocalTime secilenSaat = LocalTime.parse(secilenSaatStr, DateTimeFormatter.ofPattern("HH:mm"));
            
            // Randevu başlangıç zamanı
            LocalDateTime baslangicZamani = LocalDateTime.of(secilenTarih, secilenSaat);
            
            Kullanici ogretimUyesi = (Kullanici) cmbOgretimUyesi.getSelectedItem();
            
            Randevu randevu = new Randevu();
            randevu.setOgretimUyesiId(ogretimUyesi.getId());
            randevu.setOgrenciId(ogrenci.getId());
            randevu.setBaslangicZamani(baslangicZamani);
            randevu.setBitisZamani(baslangicZamani.plusMinutes(20));
            randevu.setKonu(txtKonu.getText().trim());
            randevu.setNotlar(txtNotlar.getText().trim());
            randevu.setDurum(Randevu.Durum.BEKLEMEDE);
            randevu.setOlusturulmaTarihi(LocalDateTime.now());

            randevuDAO.ekle(randevu);

            JOptionPane.showMessageDialog(this,
                "Randevu başarıyla oluşturuldu.",
                "Bilgi",
                JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Randevu oluşturulurken bir hata oluştu: " + ex.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Yardımcı metod - Butonları stillendirir
    private void styleButton(JButton button) {
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(new Color(70, 130, 180));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
    }
    
    // Yardımcı metod - Toggle butonları stillendirir
    private void styleToggleButton(JToggleButton button) {
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(new Color(70, 130, 180));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        button.setFont(new Font("Arial", Font.PLAIN, 12));
    }
} 