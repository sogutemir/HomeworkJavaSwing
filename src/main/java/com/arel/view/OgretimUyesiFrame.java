package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.MusaitlikDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
import com.arel.model.Musaitlik;
import com.arel.model.Randevu;
import com.arel.util.DateTimeUtil;
import com.arel.util.EmailSender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Öğretim üyesi kullanıcı arayüzü
 */
public class OgretimUyesiFrame extends JFrame {
    
    private Kullanici ogretimUyesi;
    private RandevuDAO randevuDAO;
    private MusaitlikDAO musaitlikDAO;
    private KullaniciDAO kullaniciDAO;
    
    private JTabbedPane tabbedPane;
    private JTable tblRandevular;
    private JTable tblMusaitlikler;
    private DefaultTableModel randevuTableModel;
    private DefaultTableModel musaitlikTableModel;
    
    private JButton btnRandevuOnayla;
    private JButton btnRandevuReddet;
    private JButton btnMusaitlikEkle;
    private JButton btnMusaitlikSil;
    private JButton btnYenile;
    private JButton btnCikis;
    
    // Renk şeması
    private static final Color PRIMARY_COLOR = new Color(26, 188, 156); // Yeşil
    private static final Color SECONDARY_COLOR = new Color(22, 160, 133); // Koyu yeşil
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Açık gri
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // Koyu lacivert
    private static final Color BUTTON_COLOR = new Color(22, 160, 133); // Koyu yeşil
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color DANGER_COLOR = new Color(231, 76, 60); // Kırmızı
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113); // Parlak yeşil
    
    public OgretimUyesiFrame(Kullanici ogretimUyesi) {
        this.ogretimUyesi = ogretimUyesi;
        this.randevuDAO = new RandevuDAO();
        this.musaitlikDAO = new MusaitlikDAO();
        this.kullaniciDAO = new KullaniciDAO();
        
        initComponents();
        setupListeners();
        verileriYukle();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Öğretim Üyesi: " + ogretimUyesi.getTamAd());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Üst Panel - Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Sekmeli panel
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(BACKGROUND_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        
        // Randevular Sekmesi
        JPanel randevularPanel = createRandevularPanel();
        tabbedPane.addTab("Randevular", randevularPanel);
        
        // Müsaitlik Zamanları Sekmesi
        JPanel musaitliklerPanel = createMusaitliklerPanel();
        tabbedPane.addTab("Müsaitlik Zamanları", musaitliklerPanel);
        
        // Ana panele ekle
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblHosgeldiniz = new JLabel("Hoş geldiniz, " + ogretimUyesi.getTamAd());
        lblHosgeldiniz.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHosgeldiniz.setForeground(Color.WHITE);
        
        JLabel lblRol = new JLabel("Öğretim Üyesi Paneli");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(new Color(255, 255, 255, 200));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PRIMARY_COLOR);
        textPanel.add(lblHosgeldiniz);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lblRol);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        
        btnCikis = createStyledButton("Çıkış", new Color(149, 165, 166), BUTTON_TEXT_COLOR);
        btnCikis.setPreferredSize(new Dimension(100, 30));
        
        // Sağ panel oluşturup çıkış butonunu ekleyelim hizalama için
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setBackground(PRIMARY_COLOR); // Header ile aynı arka plan
        rightPanel.add(btnCikis);
        
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    private JPanel createRandevularPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Tablo başlığı
        JLabel lblRandevular = new JLabel("Randevu Talepleri ve Programınız");
        lblRandevular.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRandevular.setForeground(TEXT_COLOR);
        
        // Tablo - Randevular
        String[] kolonlar = {"ID", "Öğrenci", "Tarih", "Saat", "Konu", "Durum"};
        randevuTableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRandevular = new JTable(randevuTableModel);
        tblRandevular.setRowHeight(30);
        tblRandevular.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblRandevular.setSelectionBackground(SECONDARY_COLOR);
        tblRandevular.setSelectionForeground(Color.WHITE);
        tblRandevular.setShowGrid(false);
        tblRandevular.setIntercellSpacing(new Dimension(0, 0));
        tblRandevular.getTableHeader().setReorderingAllowed(false);
        tblRandevular.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Tablo başlık ayarları
        JTableHeader header = tblRandevular.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        JScrollPane scrollPane = new JScrollPane(tblRandevular);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Butonlar Paneli
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        butonlarPanel.setBackground(BACKGROUND_COLOR);
        butonlarPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        btnRandevuOnayla = createStyledButton("Randevuyu Onayla", SUCCESS_COLOR, BUTTON_TEXT_COLOR);
        btnRandevuReddet = createStyledButton("Randevuyu Reddet", DANGER_COLOR, BUTTON_TEXT_COLOR);
        btnYenile = createStyledButton("Yenile", new Color(52, 152, 219), BUTTON_TEXT_COLOR);
        
        butonlarPanel.add(btnRandevuOnayla);
        butonlarPanel.add(btnRandevuReddet);
        butonlarPanel.add(btnYenile);
        
        // Panelleri ana panele ekle
        panel.add(lblRandevular, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(butonlarPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createMusaitliklerPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Tablo başlığı
        JLabel lblMusaitlikler = new JLabel("Müsaitlik Zamanlarınız");
        lblMusaitlikler.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblMusaitlikler.setForeground(TEXT_COLOR);
        
        // Tablo - Müsaitlikler
        String[] kolonlar = {"ID", "Gün", "Başlangıç Saati", "Bitiş Saati"};
        musaitlikTableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblMusaitlikler = new JTable(musaitlikTableModel);
        tblMusaitlikler.setRowHeight(30);
        tblMusaitlikler.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tblMusaitlikler.setSelectionBackground(SECONDARY_COLOR);
        tblMusaitlikler.setSelectionForeground(Color.WHITE);
        tblMusaitlikler.setShowGrid(false);
        tblMusaitlikler.setIntercellSpacing(new Dimension(0, 0));
        tblMusaitlikler.getTableHeader().setReorderingAllowed(false);
        tblMusaitlikler.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Tablo başlık ayarları
        JTableHeader header = tblMusaitlikler.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(SECONDARY_COLOR);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));
        
        JScrollPane scrollPane = new JScrollPane(tblMusaitlikler);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        // Butonlar Paneli
        JPanel butonlarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        butonlarPanel.setBackground(BACKGROUND_COLOR);
        butonlarPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        btnMusaitlikEkle = createStyledButton("Yeni Müsaitlik Ekle", BUTTON_COLOR, BUTTON_TEXT_COLOR);
        btnMusaitlikSil = createStyledButton("Müsaitliği Sil", DANGER_COLOR, BUTTON_TEXT_COLOR);
        
        butonlarPanel.add(btnMusaitlikEkle);
        butonlarPanel.add(btnMusaitlikSil);
        
        // Panelleri ana panele ekle
        panel.add(lblMusaitlikler, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(butonlarPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(200, 40));
        
        return button;
    }
    
    private void setupListeners() {
        btnRandevuOnayla.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenRandevuyuOnayla();
            }
        });
        
        btnRandevuReddet.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenRandevuyuReddet();
            }
        });
        
        btnMusaitlikEkle.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yeniMusaitlikEkle();
            }
        });
        
        btnMusaitlikSil.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenMusaitligiSil();
            }
        });
        
        btnYenile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                verileriYukle();
            }
        });
        
        btnCikis.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cikisYap();
            }
        });
        
        // Pencere kapatılırken veritabanı bağlantısını kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                com.arel.Main.shutdown();
            }
        });
    }
    
    /**
     * Randevuları ve müsaitlik zamanlarını yükler
     */
    private void verileriYukle() {
        randevulariYukle();
        musaitlikleriYukle();
    }
    
    /**
     * Öğretim üyesinin randevularını yükler
     */
    private void randevulariYukle() {
        try {
            // Tabloyu temizle
            randevuTableModel.setRowCount(0);
            
            // Randevuları getir
            List<Randevu> randevular = randevuDAO.ogretimUyesininRandevulariniGetir(ogretimUyesi.getId());
            
            // Tabloyu doldur
            for (Randevu randevu : randevular) {
                Kullanici ogrenci = randevu.getOgrenci();
                String ogrenciAdi = ogrenci != null ? ogrenci.getTamAd() : "";
                
                Object[] satir = {
                    randevu.getId(),
                    ogrenciAdi,
                    DateTimeUtil.formatDate(randevu.getBaslangicZamani().toLocalDate()),
                    DateTimeUtil.formatTime(randevu.getBaslangicZamani().toLocalTime()) + " - " + 
                    DateTimeUtil.formatTime(randevu.getBitisZamani().toLocalTime()),
                    randevu.getKonu(),
                    randevu.getDurum().toString()
                };
                
                randevuTableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            showErrorMessage("Randevular yüklenirken hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Öğretim üyesinin müsaitlik zamanlarını yükler
     */
    private void musaitlikleriYukle() {
        try {
            // Tabloyu temizle
            musaitlikTableModel.setRowCount(0);
            
            // Müsaitlikleri getir
            List<Musaitlik> musaitlikler = musaitlikDAO.ogretimUyesininMusaitlikleriniGetir(ogretimUyesi.getId());
            
            // Tabloyu doldur
            for (Musaitlik musaitlik : musaitlikler) {
                Object[] satir = {
                    musaitlik.getId(),
                    musaitlik.getGunAdi(),
                    DateTimeUtil.formatTime(musaitlik.getBaslangicSaati()),
                    DateTimeUtil.formatTime(musaitlik.getBitisSaati())
                };
                
                musaitlikTableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            showErrorMessage("Müsaitlik zamanları yüklenirken hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Seçilen randevuyu onaylar
     */
    private void secilenRandevuyuOnayla() {
        int selectedRow = tblRandevular.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Lütfen onaylamak istediğiniz randevuyu seçin.");
            return;
        }
        
        int randevuId = (int) randevuTableModel.getValueAt(selectedRow, 0);
        String durum = (String) randevuTableModel.getValueAt(selectedRow, 5);
        
        // Onaylanabilir mi kontrol et
        if (!durum.equals(Randevu.Durum.BEKLEMEDE.toString())) {
            showWarningMessage("Sadece beklemede olan randevular onaylanabilir.");
            return;
        }
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "Seçili randevuyu onaylamak istediğinize emin misiniz?", 
            "Randevu Onay", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Randevu durumunu güncelle
                boolean sonuc = randevuDAO.durumGuncelle(randevuId, Randevu.Durum.ONAYLANDI);
                
                if (sonuc) {
                    showInfoMessage("Randevu başarıyla onaylandı.");
                    
                    // E-posta gönderimi
                    try {
                        Randevu randevu = randevuDAO.getirById(randevuId);
                        if (randevu != null && randevu.getOgrenci() != null && randevu.getOgrenci().getEmail() != null && !randevu.getOgrenci().getEmail().isEmpty()) {
                            EmailSender.sendRandevuOnayEmail(
                                randevu.getOgrenci().getEmail(),
                                randevu.getOgrenci().getTamAd(),
                                ogretimUyesi.getTamAd(), // Mevcut öğretim üyesi objesi
                                DateTimeUtil.formatDate(randevu.getBaslangicZamani().toLocalDate()),
                                DateTimeUtil.formatTime(randevu.getBaslangicZamani().toLocalTime()),
                                randevu.getKonu()
                            );
                        } else {
                            System.err.println("Öğrenci veya e-posta adresi bulunamadı.");
                        }
                    } catch (Exception mailEx) {
                        System.err.println("Randevu onay e-postası gönderilirken hata: " + mailEx.getMessage());
                    }
                    
                    // Tabloyu yenile
                    randevulariYukle();
                } else {
                    showErrorMessage("Randevu onaylanırken bir hata oluştu.");
                }
                
            } catch (SQLException ex) {
                showErrorMessage("Randevu onaylanırken hata oluştu: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Seçilen randevuyu reddeder
     */
    private void secilenRandevuyuReddet() {
        int selectedRow = tblRandevular.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Lütfen reddetmek istediğiniz randevuyu seçin.");
            return;
        }
        
        int randevuId = (int) randevuTableModel.getValueAt(selectedRow, 0);
        String durum = (String) randevuTableModel.getValueAt(selectedRow, 5);
        
        // Reddedilebilir mi kontrol et
        if (!durum.equals(Randevu.Durum.BEKLEMEDE.toString())) {
            showWarningMessage("Sadece beklemede olan randevular reddedilebilir.");
            return;
        }
        
        // Red nedeni sor
        String redNedeni = JOptionPane.showInputDialog(this, 
            "Randevuyu reddetme nedeniniz:", 
            "Randevu Reddetme", JOptionPane.QUESTION_MESSAGE);
        
        if (redNedeni != null && !redNedeni.isEmpty()) {
            try {
                // Randevu durumunu güncelle
                boolean sonuc = randevuDAO.durumGuncelle(randevuId, Randevu.Durum.REDDEDILDI);
                
                if (sonuc) {
                    showInfoMessage("Randevu başarıyla reddedildi.");
                    
                    // E-posta gönderimi
                    try {
                        Randevu randevu = randevuDAO.getirById(randevuId);
                        if (randevu != null && randevu.getOgrenci() != null && randevu.getOgrenci().getEmail() != null && !randevu.getOgrenci().getEmail().isEmpty()) {
                            EmailSender.sendRandevuRedEmail(
                                randevu.getOgrenci().getEmail(),
                                randevu.getOgrenci().getTamAd(),
                                ogretimUyesi.getTamAd(), // Mevcut öğretim üyesi objesi
                                DateTimeUtil.formatDate(randevu.getBaslangicZamani().toLocalDate()),
                                DateTimeUtil.formatTime(randevu.getBaslangicZamani().toLocalTime()),
                                randevu.getKonu(),
                                redNedeni
                            );
                        } else {
                            System.err.println("Öğrenci veya e-posta adresi bulunamadı.");
                        }
                    } catch (Exception mailEx) {
                        System.err.println("Randevu red e-postası gönderilirken hata: " + mailEx.getMessage());
                    }
                    
                    // Tabloyu yenile
                    randevulariYukle();
                } else {
                    showErrorMessage("Randevu reddedilirken bir hata oluştu.");
                }
                
            } catch (SQLException ex) {
                showErrorMessage("Randevu reddedilirken hata oluştu: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Yeni müsaitlik zamanı ekler
     */
    private void yeniMusaitlikEkle() {
        MusaitlikEkleFrame musaitlikEkleFrame = new MusaitlikEkleFrame(ogretimUyesi.getId());
        musaitlikEkleFrame.setVisible(true);
        musaitlikEkleFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                musaitlikleriYukle();
            }
        });
    }
    
    /**
     * Seçilen müsaitlik zamanını siler
     */
    private void secilenMusaitligiSil() {
        int selectedRow = tblMusaitlikler.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Lütfen silmek istediğiniz müsaitlik zamanını seçin.");
            return;
        }
        
        int musaitlikId = (int) musaitlikTableModel.getValueAt(selectedRow, 0);
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "Seçili müsaitlik zamanını silmek istediğinize emin misiniz?", 
            "Müsaitlik Silme", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Müsaitliği sil
                boolean sonuc = musaitlikDAO.sil(musaitlikId);
                
                if (sonuc) {
                    showInfoMessage("Müsaitlik zamanı başarıyla silindi.");
                    
                    // Tabloyu yenile
                    musaitlikleriYukle();
                } else {
                    showErrorMessage("Müsaitlik zamanı silinirken bir hata oluştu.");
                }
                
            } catch (SQLException ex) {
                showErrorMessage("Müsaitlik zamanı silinirken hata oluştu: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Çıkış yaparak giriş ekranına döner
     */
    private void cikisYap() {
        int secim = JOptionPane.showConfirmDialog(this, 
            "Çıkış yapmak istediğinize emin misiniz?", 
            "Çıkış", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (secim == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame().setVisible(true);
        }
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showWarningMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Uyarı", JOptionPane.WARNING_MESSAGE);
    }
    
    private void showInfoMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }
} 