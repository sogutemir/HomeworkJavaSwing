package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.database.RandevuDAO;
import com.arel.model.Kullanici;
import com.arel.model.Randevu;
import com.arel.util.DateTimeUtil;

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
 * Öğrenci kullanıcı arayüzü
 */
public class OgrenciFrame extends JFrame {
    
    private Kullanici ogrenci;
    private RandevuDAO randevuDAO;
    private KullaniciDAO kullaniciDAO;
    
    private JTable tblRandevular;
    private DefaultTableModel tableModel;
    private JButton btnYeniRandevu;
    private JButton btnRandevuIptal;
    private JButton btnYenile;
    private JButton btnCikis;
    
    // Renk şeması
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Mavi
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // Açık mavi
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Açık gri
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // Koyu lacivert
    private static final Color BUTTON_COLOR = new Color(52, 152, 219); // Açık mavi
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final Color DANGER_COLOR = new Color(231, 76, 60); // Kırmızı
    
    public OgrenciFrame(Kullanici ogrenci) {
        this.ogrenci = ogrenci;
        this.randevuDAO = new RandevuDAO();
        this.kullaniciDAO = new KullaniciDAO();
        
        initComponents();
        setupListeners();
        randevulariYukle();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Öğrenci: " + ogrenci.getTamAd());
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Ana panel
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(15, 15));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(BACKGROUND_COLOR);
        
        // Üst Panel - Hoş geldiniz mesajı ve profil bilgisi
        JPanel headerPanel = createHeaderPanel();
        
        // İstatistik kartları
        JPanel statsPanel = createStatsPanel();
        
        // Tablo - Randevular
        JPanel tablePanel = createTablePanel();
        
        // Butonlar Paneli
        JPanel buttonsPanel = createButtonsPanel();
        
        // Panelleri ana panele ekle
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        panel.add(tablePanel, BorderLayout.CENTER);
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        setContentPane(panel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblHosgeldiniz = new JLabel("Hoş geldiniz, " + ogrenci.getTamAd());
        lblHosgeldiniz.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblHosgeldiniz.setForeground(Color.WHITE);
        
        JLabel lblRol = new JLabel("Öğrenci Paneli");
        lblRol.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblRol.setForeground(new Color(255, 255, 255, 200));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(PRIMARY_COLOR);
        textPanel.add(lblHosgeldiniz);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lblRol);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        statsPanel.setBorder(new EmptyBorder(15, 0, 15, 0));
        
        // Bu panel kullanılmıyor şu an, gerekirse eklenebilir
        
        return statsPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout(0, 10));
        tablePanel.setBackground(BACKGROUND_COLOR);
        
        JLabel lblRandevular = new JLabel("Randevularım");
        lblRandevular.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblRandevular.setForeground(TEXT_COLOR);
        
        // Tablo - Randevular
        String[] kolonlar = {"ID", "Öğretim Üyesi", "Tarih", "Saat", "Konu", "Durum"};
        tableModel = new DefaultTableModel(kolonlar, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tblRandevular = new JTable(tableModel);
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
        
        tablePanel.add(lblRandevular, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        buttonsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        btnYeniRandevu = createStyledButton("Yeni Randevu", BUTTON_COLOR, BUTTON_TEXT_COLOR);
        btnRandevuIptal = createStyledButton("Randevuyu İptal Et", DANGER_COLOR, BUTTON_TEXT_COLOR);
        btnYenile = createStyledButton("Yenile", new Color(46, 204, 113), BUTTON_TEXT_COLOR);
        btnCikis = createStyledButton("Çıkış", new Color(149, 165, 166), BUTTON_TEXT_COLOR);
        
        buttonsPanel.add(btnYeniRandevu);
        buttonsPanel.add(btnRandevuIptal);
        buttonsPanel.add(btnYenile);
        buttonsPanel.add(btnCikis);
        
        return buttonsPanel;
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
        btnYeniRandevu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                yeniRandevuOlustur();
            }
        });
        
        btnRandevuIptal.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secilenRandevuyuIptalEt();
            }
        });
        
        btnYenile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                randevulariYukle();
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
     * Öğrencinin randevularını yükler
     */
    private void randevulariYukle() {
        try {
            // Tabloyu temizle
            tableModel.setRowCount(0);
            
            // Randevuları getir
            List<Randevu> randevular = randevuDAO.ogrencininRandevulariniGetir(ogrenci.getId());
            
            // Tabloyu doldur
            for (Randevu randevu : randevular) {
                Kullanici ogretimUyesi = randevu.getOgretimUyesi();
                String ogretimUyesiAdi = ogretimUyesi != null ? ogretimUyesi.getTamAd() : "";
                
                Object[] satir = {
                    randevu.getId(),
                    ogretimUyesiAdi,
                    DateTimeUtil.formatDate(randevu.getBaslangicZamani().toLocalDate()),
                    DateTimeUtil.formatTime(randevu.getBaslangicZamani().toLocalTime()) + " - " + 
                    DateTimeUtil.formatTime(randevu.getBitisZamani().toLocalTime()),
                    randevu.getKonu(),
                    randevu.getDurum().toString()
                };
                
                tableModel.addRow(satir);
            }
            
        } catch (SQLException ex) {
            showErrorMessage("Randevular yüklenirken hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    /**
     * Yeni randevu oluşturma ekranını açar
     */
    private void yeniRandevuOlustur() {
        RandevuOlusturFrame randevuFrame = new RandevuOlusturFrame(ogrenci);
        randevuFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                randevulariYukle();
            }
        });
        randevuFrame.setVisible(true);
    }
    
    /**
     * Seçilen randevuyu iptal eder
     */
    private void secilenRandevuyuIptalEt() {
        int selectedRow = tblRandevular.getSelectedRow();
        if (selectedRow == -1) {
            showWarningMessage("Lütfen iptal etmek istediğiniz randevuyu seçin.");
            return;
        }
        
        int randevuId = (int) tableModel.getValueAt(selectedRow, 0);
        String durum = (String) tableModel.getValueAt(selectedRow, 5);
        
        // İptal edilebilir mi kontrol et
        if (durum.equals(Randevu.Durum.TAMAMLANDI.toString()) || 
            durum.equals(Randevu.Durum.IPTAL_EDILDI.toString())) {
            showWarningMessage("Bu randevu iptal edilemez.");
            return;
        }
        
        // Onay iste
        int secim = JOptionPane.showConfirmDialog(this, 
            "Seçili randevuyu iptal etmek istediğinize emin misiniz?", 
            "Randevu İptal", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        
        if (secim == JOptionPane.YES_OPTION) {
            try {
                // Randevu durumunu güncelle
                boolean sonuc = randevuDAO.durumGuncelle(randevuId, Randevu.Durum.IPTAL_EDILDI);
                
                if (sonuc) {
                    showInfoMessage("Randevu başarıyla iptal edildi.");
                    
                    // Tabloyu yenile
                    randevulariYukle();
                } else {
                    showErrorMessage("Randevu iptal edilirken bir hata oluştu.");
                }
                
            } catch (SQLException ex) {
                showErrorMessage("Randevu iptal edilirken hata oluştu: " + ex.getMessage());
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