package com.arel.view;

import com.arel.database.MusaitlikDAO;
import com.arel.model.Musaitlik;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

public class MusaitlikEkleFrame extends JFrame {
    private final int ogretimUyesiId;
    private final MusaitlikDAO musaitlikDAO;
    
    // Renk sabitleri
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180); // Steelblue
    private static final Color SECONDARY_COLOR = new Color(240, 248, 255); // Aliceblue
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color TEXT_COLOR = new Color(50, 50, 50);
    private static final Color FIELD_BORDER_COLOR = new Color(180, 180, 180);
    
    private JRadioButton rbTekrarEden;
    private JRadioButton rbTekSeferlik;
    private JComboBox<String> cmbGun;
    private JDateChooser dateChooser;
    private JSpinner spnBaslangicSaati;
    private JSpinner spnBitisSaati;
    private JPanel pnlGun;
    private JPanel pnlTarih;
    private JButton btnKaydet;
    private JButton btnIptal;

    public MusaitlikEkleFrame(int ogretimUyesiId) {
        this.ogretimUyesiId = ogretimUyesiId;
        this.musaitlikDAO = new MusaitlikDAO();
        initComponents();
        initDefaultValues();
    }

    private void initComponents() {
        setTitle("Müsaitlik Ekle");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        
        // Başlık panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel lblHeader = new JLabel("Müsaitlik Saatleri Ekle");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 16));
        lblHeader.setForeground(Color.WHITE);
        
        JLabel lblSubHeader = new JLabel("Lütfen uygun saatlerinizi belirtin");
        lblSubHeader.setFont(new Font("Arial", Font.PLAIN, 12));
        lblSubHeader.setForeground(new Color(220, 220, 220));
        
        JPanel headerTextPanel = new JPanel(new BorderLayout());
        headerTextPanel.setOpaque(false);
        headerTextPanel.add(lblHeader, BorderLayout.NORTH);
        headerTextPanel.add(lblSubHeader, BorderLayout.CENTER);
        headerPanel.add(headerTextPanel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Ana panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(BACKGROUND_COLOR);

        // Müsaitlik tipi seçimi
        JPanel pnlTip = createSectionPanel("Müsaitlik Türü");
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        radioPanel.setOpaque(false);
        
        rbTekrarEden = new JRadioButton("Haftalık Tekrar Eden", true);
        styleRadioButton(rbTekrarEden);
        
        rbTekSeferlik = new JRadioButton("Tek Seferlik");
        styleRadioButton(rbTekSeferlik);
        
        ButtonGroup bgTip = new ButtonGroup();
        bgTip.add(rbTekrarEden);
        bgTip.add(rbTekSeferlik);
        
        radioPanel.add(rbTekrarEden);
        radioPanel.add(rbTekSeferlik);
        pnlTip.add(radioPanel);
        
        // Gün/Tarih seçimi paneli
        JPanel pnlTarihSecim = createSectionPanel("Gün / Tarih Seçimi");
        
        // Gün seçimi paneli
        pnlGun = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlGun.setOpaque(false);
        JLabel lblGun = new JLabel("Gün:");
        lblGun.setFont(new Font("Arial", Font.BOLD, 12));
        lblGun.setForeground(TEXT_COLOR);
        pnlGun.add(lblGun);
        
        cmbGun = new JComboBox<>(new String[]{"Pazartesi", "Salı", "Çarşamba", "Perşembe", "Cuma"});
        styleComboBox(cmbGun);
        cmbGun.setPreferredSize(new Dimension(180, 30));
        pnlGun.add(cmbGun);

        // Tarih seçimi paneli
        pnlTarih = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlTarih.setOpaque(false);
        JLabel lblTarih = new JLabel("Tarih:");
        lblTarih.setFont(new Font("Arial", Font.BOLD, 12));
        lblTarih.setForeground(TEXT_COLOR);
        pnlTarih.add(lblTarih);
        
        dateChooser = new JDateChooser();
        dateChooser.setPreferredSize(new Dimension(180, 30));
        dateChooser.setFont(new Font("Arial", Font.PLAIN, 12));
        dateChooser.setBackground(Color.WHITE);
        dateChooser.getDateEditor().getUiComponent().setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER_COLOR),
                BorderFactory.createEmptyBorder(2, 5, 2, 5)
            )
        );
        pnlTarih.add(dateChooser);
        pnlTarih.setVisible(false);
        
        pnlTarihSecim.add(pnlGun);
        pnlTarihSecim.add(pnlTarih);

        // Saat seçimi paneli
        JPanel pnlSaatSecim = createSectionPanel("Saat Aralığı");
        JPanel pnlSaat = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        pnlSaat.setOpaque(false);
        
        JLabel lblBaslangic = new JLabel("Başlangıç Saati:");
        lblBaslangic.setFont(new Font("Arial", Font.BOLD, 12));
        lblBaslangic.setForeground(TEXT_COLOR);
        pnlSaat.add(lblBaslangic);
        
        spnBaslangicSaati = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor baslangicEditor = new JSpinner.DateEditor(spnBaslangicSaati, "HH:mm");
        spnBaslangicSaati.setEditor(baslangicEditor);
        styleSpinner(spnBaslangicSaati);
        pnlSaat.add(spnBaslangicSaati);
        
        pnlSaat.add(Box.createHorizontalStrut(20));

        JLabel lblBitis = new JLabel("Bitiş Saati:");
        lblBitis.setFont(new Font("Arial", Font.BOLD, 12));
        lblBitis.setForeground(TEXT_COLOR);
        pnlSaat.add(lblBitis);
        
        spnBitisSaati = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor bitisEditor = new JSpinner.DateEditor(spnBitisSaati, "HH:mm");
        spnBitisSaati.setEditor(bitisEditor);
        styleSpinner(spnBitisSaati);
        pnlSaat.add(spnBitisSaati);
        
        pnlSaatSecim.add(pnlSaat);

        // Bilgi etiketi
        JPanel pnlInfo = new JPanel(new BorderLayout());
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(new EmptyBorder(5, 10, 5, 10));
        
        JLabel lblBilgi = new JLabel("<html><div style='padding:8px;'>"
            + "<span style='font-weight:bold;'>Bilgi:</span> Müsaitlik saatleri 08:00-17:00 arasında olmalıdır.<br>"
            + "Randevular 20 dakikalık dilimlere bölünerek öğrencilere sunulacaktır."
            + "</div></html>");
        lblBilgi.setFont(new Font("Arial", Font.PLAIN, 12));
        lblBilgi.setForeground(new Color(70, 70, 70));
        lblBilgi.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(230, 230, 230)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        lblBilgi.setBackground(new Color(245, 245, 245));
        lblBilgi.setOpaque(true);
        
        pnlInfo.add(lblBilgi, BorderLayout.CENTER);

        // Buton paneli
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlButtons.setOpaque(false);
        pnlButtons.setBorder(new EmptyBorder(15, 0, 5, 0));
        
        btnIptal = new JButton("İptal");
        styleSecondaryButton(btnIptal);
        btnIptal.addActionListener(e -> dispose());
        
        btnKaydet = new JButton("Kaydet");
        stylePrimaryButton(btnKaydet);
        btnKaydet.addActionListener(e -> kaydet());
        
        pnlButtons.add(btnIptal);
        pnlButtons.add(Box.createHorizontalStrut(10));
        pnlButtons.add(btnKaydet);

        // Radio button dinleyicileri
        rbTekrarEden.addActionListener(e -> {
            pnlGun.setVisible(true);
            pnlTarih.setVisible(false);
        });
        rbTekSeferlik.addActionListener(e -> {
            pnlGun.setVisible(false);
            pnlTarih.setVisible(true);
        });

        // Panelleri ana panele ekle
        mainPanel.add(pnlTip);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(pnlTarihSecim);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(pnlSaatSecim);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(pnlInfo);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(pnlButtons);

        // Scrollpane içine koy
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
        
        setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(null);
    }
    
    private void initDefaultValues() {
        // Başlangıç saati 08:00 olarak ayarla
        Calendar calStart = Calendar.getInstance();
        calStart.set(Calendar.HOUR_OF_DAY, 8);
        calStart.set(Calendar.MINUTE, 0);
        spnBaslangicSaati.setValue(calStart.getTime());
        
        // Bitiş saati 17:00 olarak ayarla
        Calendar calEnd = Calendar.getInstance();
        calEnd.set(Calendar.HOUR_OF_DAY, 17);
        calEnd.set(Calendar.MINUTE, 0);
        spnBitisSaati.setValue(calEnd.getTime());
        
        // Tarih seçiciyi bugün olarak ayarla
        dateChooser.setDate(new Date());
    }
    
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(new CompoundBorder(
            new CompoundBorder(
                new EmptyBorder(5, 0, 5, 0),
                new MatteBorder(0, 0, 1, 0, new Color(220, 220, 220))
            ),
            new EmptyBorder(0, 0, 10, 0)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 14));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBorder(new EmptyBorder(0, 5, 8, 0));
        
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        
        panel.add(titlePanel);
        
        return panel;
    }
    
    private void styleRadioButton(JRadioButton radioButton) {
        radioButton.setFont(new Font("Arial", Font.PLAIN, 13));
        radioButton.setForeground(TEXT_COLOR);
        radioButton.setFocusPainted(false);
        radioButton.setBackground(BACKGROUND_COLOR);
    }
    
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setFont(new Font("Arial", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER_COLOR),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Arial", Font.PLAIN, 13));
        spinner.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER_COLOR),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        spinner.setPreferredSize(new Dimension(100, 30));
        
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)editor).getTextField().setBackground(Color.WHITE);
        }
    }
    
    private void stylePrimaryButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(60, 120, 170)),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
    }
    
    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(TEXT_COLOR);
        button.setBackground(new Color(240, 240, 240));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(FIELD_BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));
        button.setFocusPainted(false);
    }

    private void kaydet() {
        try {
            LocalTime baslangicSaati = ((Date) spnBaslangicSaati.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();
            LocalTime bitisSaati = ((Date) spnBitisSaati.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalTime();

            // Saat kontrolü
            if (baslangicSaati.isBefore(LocalTime.of(8, 0)) || 
                bitisSaati.isAfter(LocalTime.of(17, 0)) ||
                baslangicSaati.isAfter(bitisSaati)) {
                JOptionPane.showMessageDialog(this, 
                    "Lütfen geçerli saat aralığı seçin (08:00-17:00 arası)",
                    "Hata",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            Musaitlik musaitlik = new Musaitlik();
            musaitlik.setOgretimUyesiId(ogretimUyesiId);
            musaitlik.setBaslangicSaati(baslangicSaati);
            musaitlik.setBitisSaati(bitisSaati);
            musaitlik.setTekrarEden(rbTekrarEden.isSelected());

            if (rbTekrarEden.isSelected()) {
                // Haftalık tekrar eden müsaitlik
                int gunIndex = cmbGun.getSelectedIndex();
                musaitlik.setGun(DayOfWeek.of(gunIndex + 1));
            } else {
                // Tek seferlik müsaitlik
                if (dateChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(this, 
                        "Lütfen bir tarih seçin",
                        "Hata",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                LocalDate secilenTarih = dateChooser.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
                musaitlik.setTarih(secilenTarih);
            }

            musaitlikDAO.ekle(musaitlik);
            JOptionPane.showMessageDialog(this, 
                "Müsaitlik başarıyla eklendi",
                "Bilgi",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Müsaitlik eklenirken bir hata oluştu: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}