package com.arel.view;

import com.arel.database.KullaniciDAO;
import com.arel.model.Kullanici;

import javax.swing.*;
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
    
    public LoginFrame() {
        kullaniciDAO = new KullaniciDAO();
        initComponents();
        setupListeners();
    }
    
    private void initComponents() {
        setTitle("Akademik Randevu Sistemi - Giriş");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Başlık
        JLabel lblBaslik = new JLabel("Akademik Randevu Sistemi");
        lblBaslik.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(lblBaslik, gbc);
        
        // Kullanıcı Adı
        JLabel lblKullaniciAdi = new JLabel("Kullanıcı Adı:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(lblKullaniciAdi, gbc);
        
        txtKullaniciAdi = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(txtKullaniciAdi, gbc);
        
        // Şifre
        JLabel lblSifre = new JLabel("Şifre:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblSifre, gbc);
        
        txtSifre = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(txtSifre, gbc);
        
        // Kullanıcı Tipi
        JLabel lblKullaniciTipi = new JLabel("Kullanıcı Tipi:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblKullaniciTipi, gbc);
        
        cmbKullaniciTipi = new JComboBox<>(new String[]{"Öğrenci", "Öğretim Üyesi", "Admin"});
        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(cmbKullaniciTipi, gbc);
        
        // Giriş Butonu
        btnGiris = new JButton("Giriş Yap");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(btnGiris, gbc);
        
        getContentPane().add(panel);
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
            JOptionPane.showMessageDialog(this, 
                "Kullanıcı adı ve şifre boş olamaz!", 
                "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            Kullanici kullanici = kullaniciDAO.girisYap(kullaniciAdi, sifre);
            
            if (kullanici == null) {
                JOptionPane.showMessageDialog(this, 
                    "Kullanıcı adı veya şifre hatalı!", 
                    "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Kullanıcı tipi seçimi ile kullanıcının rolü uyuşuyor mu kontrol et
            Kullanici.Rol beklenenRol = seciliRoluGetir();
            if (kullanici.getRol() != beklenenRol) {
                JOptionPane.showMessageDialog(this, 
                    "Seçtiğiniz kullanıcı tipine uygun bir hesabınız yok!", 
                    "Giriş Başarısız", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, 
                "Veritabanı hatası: " + ex.getMessage(), 
                "Hata", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
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
