package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.model.Kullanici;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;

/**
 * Kullanıcı giriş ekranı
 */
public class LoginFrame extends JFrame {
    
    private JTextField txtKullaniciAdi;
    private JPasswordField txtSifre;
    private JButton btnGiris;
    private JComboBox<String> cmbKullaniciTipi;
    private KullaniciDAO kullaniciDAO;
    
    // Renk şeması
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Mavi
    private static final Color SECONDARY_COLOR = new Color(52, 152, 219); // Açık mavi
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245); // Açık gri
    private static final Color ACCENT_COLOR = new Color(26, 188, 156); // Yeşil
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // Koyu lacivert
    private static final Color BUTTON_COLOR = new Color(52, 152, 219); // Açık mavi
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    
    public LoginFrame() {
        kullaniciDAO = new KullaniciDAO();
        initComponents();
        setupListeners();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Giriş");
        setSize(450, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
        
        // Header Panel (Logo ve Başlık)
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Form Panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel, BorderLayout.CENTER);
        
        setContentPane(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(getWidth(), 120));
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Başlık etiketi
        JLabel lblTitle = new JLabel("Akademik Randevu Sistemi");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Alt başlık
        JLabel lblSubtitle = new JLabel("Sisteme giriş yapın");
        lblSubtitle.setForeground(new Color(255, 255, 255, 200));
        lblSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSubtitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Başlıkları yerleştir
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        titlePanel.add(lblSubtitle);
        
        headerPanel.add(titlePanel, BorderLayout.CENTER);
        
        return headerPanel;
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        
        // Kullanıcı Adı Alanı
        JLabel lblKullaniciAdi = new JLabel("Kullanıcı Adı");
        lblKullaniciAdi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKullaniciAdi.setForeground(TEXT_COLOR);
        
        txtKullaniciAdi = new JTextField();
        txtKullaniciAdi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtKullaniciAdi.setMargin(new Insets(8, 10, 8, 10));
        txtKullaniciAdi.setPreferredSize(new Dimension(formPanel.getWidth(), 40));
        txtKullaniciAdi.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        
        // Şifre Alanı
        JLabel lblSifre = new JLabel("Şifre");
        lblSifre.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSifre.setForeground(TEXT_COLOR);
        
        txtSifre = new JPasswordField();
        txtSifre.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtSifre.setMargin(new Insets(8, 10, 8, 10));
        txtSifre.setPreferredSize(new Dimension(formPanel.getWidth(), 40));
        txtSifre.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        
        // Kullanıcı Tipi Alanı
        JLabel lblKullaniciTipi = new JLabel("Kullanıcı Tipi");
        lblKullaniciTipi.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKullaniciTipi.setForeground(TEXT_COLOR);
        
        cmbKullaniciTipi = new JComboBox<>(new String[]{"Öğrenci", "Öğretim Üyesi", "Admin"});
        cmbKullaniciTipi.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbKullaniciTipi.setBackground(Color.WHITE);
        cmbKullaniciTipi.setForeground(TEXT_COLOR);
        cmbKullaniciTipi.setPreferredSize(new Dimension(formPanel.getWidth(), 40));
        cmbKullaniciTipi.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
        
        // Giriş Butonu
        btnGiris = new JButton("Giriş Yap");
        btnGiris.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGiris.setBackground(BUTTON_COLOR);
        btnGiris.setForeground(BUTTON_TEXT_COLOR);
        btnGiris.setFocusPainted(false);
        btnGiris.setBorderPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGiris.setPreferredSize(new Dimension(formPanel.getWidth(), 45));
        btnGiris.setMaximumSize(new Dimension(Short.MAX_VALUE, 45));
        
        // Alanları forma ekle
        formPanel.add(lblKullaniciAdi);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtKullaniciAdi);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        formPanel.add(lblSifre);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(txtSifre);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        formPanel.add(lblKullaniciTipi);
        formPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        formPanel.add(cmbKullaniciTipi);
        formPanel.add(Box.createRigidArea(new Dimension(0, 25)));
        
        formPanel.add(btnGiris);
        
        return formPanel;
    }
    
    private void setupListeners() {
        btnGiris.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                girisYap();
            }
        });
        
        // Enter tuşuna basıldığında giriş yap
        getRootPane().setDefaultButton(btnGiris);
        
        // Pencere kapatılırken veritabanı bağlantısını kapat
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                com.arel.Main.shutdown();
            }
        });
    }
    
    private void girisYap() {
        String kullaniciAdi = txtKullaniciAdi.getText();
        String sifre = new String(txtSifre.getPassword());
        
        if (kullaniciAdi.isEmpty() || sifre.isEmpty()) {
            showErrorMessage("Kullanıcı adı ve şifre boş olamaz!");
            return;
        }
        
        try {
            Kullanici kullanici = kullaniciDAO.girisYap(kullaniciAdi, sifre);
            
            if (kullanici == null) {
                showErrorMessage("Kullanıcı adı veya şifre hatalı!");
                return;
            }
            
            // Kullanıcı tipi seçimi ile kullanıcının rolü uyuşuyor mu kontrol et
            Kullanici.Rol beklenenRol = seciliRoluGetir();
            if (kullanici.getRol() != beklenenRol) {
                showErrorMessage("Seçtiğiniz kullanıcı tipine uygun bir hesabınız yok!");
                return;
            }
            
            // Başarılı giriş
            JOptionPane.showMessageDialog(this, 
                "Giriş başarılı! Hoş geldiniz, " + kullanici.getAd() + " " + kullanici.getSoyad(), 
                "Giriş Başarılı", JOptionPane.INFORMATION_MESSAGE);
            
            // Kullanıcı tipine göre uygun ekranı aç
            açAnaPencere(kullanici);
            
            // Giriş penceresini kapat
            dispose();
            
        } catch (SQLException ex) {
            showErrorMessage("Veritabanı hatası: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }
    
    private Kullanici.Rol seciliRoluGetir() {
        int seciliIndex = cmbKullaniciTipi.getSelectedIndex();
        switch (seciliIndex) {
            case 0:
                return Kullanici.Rol.OGRENCI;
            case 1:
                return Kullanici.Rol.OGRETIM_UYESI;
            case 2:
                return Kullanici.Rol.ADMIN;
            default:
                return Kullanici.Rol.OGRENCI;
        }
    }
    
    private void açAnaPencere(Kullanici kullanici) {
        SwingUtilities.invokeLater(() -> {
            switch (kullanici.getRol()) {
                case OGRENCI:
                    // Öğrenci ana ekranını aç
                    OgrenciFrame ogrenciFrame = new OgrenciFrame(kullanici);
                    ogrenciFrame.setVisible(true);
                    break;
                    
                case OGRETIM_UYESI:
                    // Öğretim üyesi ana ekranını aç
                    OgretimUyesiFrame ogretimUyesiFrame = new OgretimUyesiFrame(kullanici);
                    ogretimUyesiFrame.setVisible(true);
                    break;
                    
                case ADMIN:
                    // Admin ana ekranını aç
                    AdminFrame adminFrame = new AdminFrame(kullanici);
                    adminFrame.setVisible(true);
                    break;
            }
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
