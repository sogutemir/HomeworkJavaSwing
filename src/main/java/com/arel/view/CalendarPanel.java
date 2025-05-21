package com.arel.view;

import com.arel.model.Randevu;
import com.arel.model.Musaitlik;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.time.format.TextStyle;

public class CalendarPanel extends JPanel {
    private static final int SLOT_HEIGHT = 30;
    private static final int TIME_WIDTH = 60;
    private static final int DAY_HEADER_HEIGHT = 30;
    private static final int MINUTES_PER_SLOT = 20;
    private static final LocalTime START_TIME = LocalTime.of(8, 0);
    private static final LocalTime END_TIME = LocalTime.of(17, 0);
    
    // Renkler
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color TIME_LABEL_COLOR = new Color(80, 80, 80);
    private static final Color DAY_HEADER_COLOR = new Color(240, 240, 240);
    private static final Color GRID_COLOR = new Color(230, 230, 230);
    private static final Color TODAY_COLOR = new Color(240, 248, 255); // Alice Blue
    private static final Color AVAILABLE_COLOR = new Color(144, 238, 144, 180); // Light Green with transparency
    private static final Color APPOINTMENT_COLOR = new Color(100, 149, 237, 200); // Cornflower Blue with transparency
    private static final Color SELECTED_COLOR = new Color(255, 255, 0, 100); // Yellow with transparency
    private static final Color CURRENT_TIME_COLOR = new Color(255, 0, 0, 150); // Red with transparency
    
    public enum ViewType {
        DAILY,
        WEEKLY
    }

    private ViewType viewType = ViewType.DAILY;
    private LocalDate currentDate = LocalDate.now();
    private List<LocalTime> musaitSaatler = new ArrayList<>();
    private List<Randevu> randevular = new ArrayList<>();
    private LocalDateTime selectedTime;
    
    public CalendarPanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(BACKGROUND_COLOR);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }
    
    private void handleMouseClick(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();
        
        // Zaman çizelgesinin başlangıç koordinatları
        int timelineStartX = TIME_WIDTH;
        int timelineStartY = DAY_HEADER_HEIGHT;
        
        // Tıklanan noktanın zaman dilimini hesapla
        if (x > timelineStartX && y > timelineStartY) {
            // Slot hesaplama
            int slot = (y - timelineStartY) / SLOT_HEIGHT;
            int minutes = slot * MINUTES_PER_SLOT;
            LocalTime slotTime = START_TIME.plusMinutes(minutes);
            
            // Eğer seçilen saat END_TIME'dan sonra ise işlemi iptal et
            if (slotTime.isAfter(END_TIME)) {
                return;
            }
            
            // Gün hesaplama
            int dayWidth = (getWidth() - TIME_WIDTH) / (viewType == ViewType.WEEKLY ? 5 : 1);
            int dayIndex = (x - timelineStartX) / dayWidth;
            
            if (dayIndex >= 0 && dayIndex < (viewType == ViewType.WEEKLY ? 5 : 1)) {
                LocalDate selectedDate = viewType == ViewType.WEEKLY ? 
                    currentDate.with(DayOfWeek.MONDAY).plusDays(dayIndex) :
                    currentDate;
                
                // Kullanıcının isteği üzerine takvimden direkt seçim ve hata mesajları kaldırıldı.
                // Takvim sadece görsel referans amaçlıdır. Seçim JDateChooser ve JComboBox ile yapılır.
                /*
                // Seçilen tarihin bugün veya gelecekte olup olmadığını kontrol et
                if (selectedDate.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this,
                        "Geçmiş tarihlere randevu oluşturulamaz.",
                        "Uyarı",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Seçilen zaman diliminin müsait olup olmadığını kontrol et
                if (isTimeSlotAvailable(LocalDateTime.of(selectedDate, slotTime))) {
                    selectedTime = LocalDateTime.of(selectedDate, slotTime);
                    repaint();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Seçilen zaman dilimi müsait değil.",
                        "Uyarı",
                        JOptionPane.WARNING_MESSAGE);
                }
                */
            }
        }
    }
    
    public boolean isTimeSlotAvailable(LocalDateTime dateTime) {
        // Eğer seçilen zaman geçmiş bir zamansa müsait değil
        if (dateTime.isBefore(LocalDateTime.now())) {
            return false;
        }
        
        // Sadece bilgi amaçlı gösterim için, her zaman true dön
        // Asıl müsaitlik kontrolü RandevuOlusturFrame sınıfında yapılmaktadır
        return true;
    }
    
    public int getSelectedRow() {
        if (selectedTime == null) {
            return -1;
        }
        return (selectedTime.getHour() - 8) * (60 / MINUTES_PER_SLOT) + 
               (selectedTime.getMinute() / MINUTES_PER_SLOT);
    }
    
    public LocalDateTime getSelectedTime() {
        return selectedTime;
    }
    
    public void setMusaitSaatler(List<LocalTime> musaitSaatler) {
        this.musaitSaatler = musaitSaatler;
        repaint();
    }
    
    public void setRandevular(List<Randevu> randevular) {
        this.randevular = randevular;
        repaint();
    }
    
    public void setViewType(ViewType viewType) {
        this.viewType = viewType;
        repaint();
    }
    
    public ViewType getViewType() {
        return viewType;
    }
    
    public LocalDate getCurrentDate() {
        return currentDate;
    }
    
    public void setCurrentDate(LocalDate date) {
        this.currentDate = date;
        repaint();
    }
    
    public void clearAvailabilities() {
        musaitSaatler.clear();
        randevular.clear();
        selectedTime = null;
        repaint();
    }
    
    public LocalDateTime getSelectedTime(Point point) {
        // Tıklanan noktanın hangi zaman dilimine denk geldiğini hesapla
        int x = point.x;
        int y = point.y;
        
        // Takvim grid'inin başlangıç koordinatları
        int gridStartX = 100; // Sol kenar boşluğu
        int gridStartY = 50;  // Üst kenar boşluğu
        
        // Eğer tıklanan nokta grid dışındaysa null dön
        if (x < gridStartX || y < gridStartY) {
            return null;
        }
        
        // Her bir zaman diliminin yüksekliği (20 dakika = 1 birim)
        int timeSlotHeight = 30;
        
        // Tıklanan satırı hesapla
        int row = (y - gridStartY) / timeSlotHeight;
        
        // Geçerli bir satır değilse null dön
        if (row < 0 || row >= 24) {
            return null;
        }
        
        // Başlangıç saatini hesapla (08:00'dan başlayarak)
        LocalTime time = LocalTime.of(8, 0).plusMinutes(row * 20);
        
        // Eğer saat mesai saatleri dışındaysa null dön
        if (time.isBefore(LocalTime.of(8, 0)) || time.isAfter(LocalTime.of(17, 0))) {
            return null;
        }
        
        return LocalDateTime.of(currentDate, time);
    }
    
    public void previousDay() {
        currentDate = currentDate.minusDays(1);
        repaint();
    }
    
    public void nextDay() {
        currentDate = currentDate.plusDays(1);
        repaint();
    }
    
    public void previousWeek() {
        currentDate = currentDate.minusWeeks(1);
        repaint();
    }
    
    public void nextWeek() {
        currentDate = currentDate.plusWeeks(1);
        repaint();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Arka planı çiz
        g2.setColor(BACKGROUND_COLOR);
        g2.fillRect(0, 0, width, height);
        
        // Saat çizgilerini çiz
        drawTimeLines(g2, width, height);
        
        // Günleri çiz
        if (viewType == ViewType.WEEKLY) {
            drawWeekView(g2, width, height);
        } else {
            drawDayView(g2, width, height);
        }
        
        // Şu anki zamanı gösteren çizgi
        drawCurrentTimeLine(g2, width);
    }
    
    private void drawTimeLines(Graphics2D g2, int width, int height) {
        // Saat çizgileri
        g2.setColor(GRID_COLOR);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        LocalTime currentTime = START_TIME;
        int y = DAY_HEADER_HEIGHT;
        
        // Zaman paneli arka planı
        g2.setColor(new Color(250, 250, 250));
        g2.fillRect(0, DAY_HEADER_HEIGHT, TIME_WIDTH, height - DAY_HEADER_HEIGHT);
        
        // Saat çizgileri ve etiketleri
        while (currentTime.isBefore(END_TIME) || currentTime.equals(END_TIME)) {
            // Saat etiketi
            g2.setColor(TIME_LABEL_COLOR);
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            g2.drawString(currentTime.format(timeFormatter), 5, y + 15);
            
            // Yatay çizgi
            g2.setColor(GRID_COLOR);
            g2.drawLine(TIME_WIDTH, y, width, y);
            
            currentTime = currentTime.plusMinutes(MINUTES_PER_SLOT);
            y += SLOT_HEIGHT;
        }
        
        // Dikey sınır çizgisi
        g2.setColor(new Color(220, 220, 220));
        g2.drawLine(TIME_WIDTH, 0, TIME_WIDTH, height);
    }
    
    private void drawWeekView(Graphics2D g2, int width, int height) {
        LocalDate weekStart = currentDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);
        int dayWidth = (width - TIME_WIDTH) / 5; // Sadece hafta içi günler (5 gün)
        
        // Başlık paneli arka planı
        g2.setColor(DAY_HEADER_COLOR);
        g2.fillRect(TIME_WIDTH, 0, width - TIME_WIDTH, DAY_HEADER_HEIGHT);
        
        // Gün başlıkları
        for (int i = 0; i < 5; i++) { // Sadece hafta içi günler
            LocalDate date = weekStart.plusDays(i);
            
            // Bugün mü kontrolü
            boolean isToday = date.equals(LocalDate.now());
            
            // Gün arka planı (bugünse vurgulanır)
            if (isToday) {
                g2.setColor(TODAY_COLOR);
                g2.fillRect(TIME_WIDTH + (i * dayWidth), 0, dayWidth, DAY_HEADER_HEIGHT);
            }
            
            // Gün adı ve tarih
            String dayName = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("tr"));
            String dayNumber = String.valueOf(date.getDayOfMonth());
            String monthName = date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("tr"));
            
            g2.setColor(isToday ? new Color(0, 0, 150) : new Color(70, 70, 70));
            g2.setFont(new Font("Arial", isToday ? Font.BOLD : Font.PLAIN, 12));
            
            int x = TIME_WIDTH + (i * dayWidth) + 5;
            g2.drawString(dayName + " " + dayNumber + " " + monthName, x, 20);
            
            // Dikey çizgiler
            g2.setColor(GRID_COLOR);
            g2.drawLine(TIME_WIDTH + (i * dayWidth), 0, TIME_WIDTH + (i * dayWidth), height);
            
            // Bugünün kolonunu vurgulama
            if (isToday) {
                g2.setColor(new Color(240, 248, 255, 80)); // Alice Blue hafif saydamlıkla
                g2.fillRect(TIME_WIDTH + (i * dayWidth), DAY_HEADER_HEIGHT, dayWidth, height - DAY_HEADER_HEIGHT);
            }
        }
        
        // Son dikey çizgi
        g2.setColor(GRID_COLOR);
        g2.drawLine(TIME_WIDTH + (5 * dayWidth), 0, TIME_WIDTH + (5 * dayWidth), height);
        
        // Müsait saatleri çiz
        if (musaitSaatler != null) {
            g2.setColor(AVAILABLE_COLOR);
            for (LocalTime saat : musaitSaatler) {
                drawMusaitlikBlock(g2, saat, dayWidth);
            }
        }
        
        // Randevuları çiz
        if (randevular != null) {
            g2.setColor(APPOINTMENT_COLOR);
            for (Randevu randevu : randevular) {
                LocalDate randevuTarihi = randevu.getBaslangicZamani().toLocalDate();
                DayOfWeek randevuGunu = randevuTarihi.getDayOfWeek();
                
                // Sadece hafta içi günlerdeki randevuları göster
                if (randevuGunu.getValue() <= 5 && 
                    randevuTarihi.isAfter(weekStart.minusDays(1)) && 
                    randevuTarihi.isBefore(weekStart.plusDays(5))) {
                    drawRandevuBlock(g2, randevu, dayWidth);
                }
            }
        }
    }
    
    private void drawDayView(Graphics2D g2, int width, int height) {
        // Başlık paneli arka planı
        g2.setColor(DAY_HEADER_COLOR);
        g2.fillRect(TIME_WIDTH, 0, width - TIME_WIDTH, DAY_HEADER_HEIGHT);
        
        // Bugün mü kontrolü
        boolean isToday = currentDate.equals(LocalDate.now());
        
        // Gün adı ve tarih
        String dayName = currentDate.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("tr"));
        String dayNumber = String.valueOf(currentDate.getDayOfMonth());
        String monthName = currentDate.getMonth().getDisplayName(TextStyle.FULL, new Locale("tr"));
        String yearNumber = String.valueOf(currentDate.getYear());
        
        g2.setColor(isToday ? new Color(0, 0, 150) : new Color(70, 70, 70));
        g2.setFont(new Font("Arial", isToday ? Font.BOLD : Font.PLAIN, 13));
        g2.drawString(dayName + ", " + dayNumber + " " + monthName + " " + yearNumber, TIME_WIDTH + 10, 20);
        
        // Bugünün arka planını vurgulama
        if (isToday) {
            g2.setColor(new Color(240, 248, 255, 80)); // Alice Blue hafif saydamlıkla
            g2.fillRect(TIME_WIDTH, DAY_HEADER_HEIGHT, width - TIME_WIDTH, height - DAY_HEADER_HEIGHT);
        }
        
        // Müsait saatleri çiz
        if (musaitSaatler != null) {
            g2.setColor(AVAILABLE_COLOR);
            for (LocalTime saat : musaitSaatler) {
                drawMusaitlikBlock(g2, saat, width - TIME_WIDTH);
            }
        }
        
        // Randevuları çiz
        if (randevular != null) {
            g2.setColor(APPOINTMENT_COLOR);
            for (Randevu randevu : randevular) {
                if (randevu.getBaslangicZamani().toLocalDate().equals(currentDate)) {
                    drawRandevuBlock(g2, randevu, width - TIME_WIDTH);
                }
            }
        }
    }
    
    private void drawRandevuBlock(Graphics2D g2, Randevu randevu, int dayWidth) {
        LocalTime baslangic = randevu.getBaslangicZamani().toLocalTime();
        LocalTime bitis = randevu.getBitisZamani().toLocalTime();
        
        int dayIndex = randevu.getBaslangicZamani().getDayOfWeek().getValue() - 1;
        int x = TIME_WIDTH + (dayIndex * dayWidth) + 1;
        int y = timeToY(baslangic);
        int blockWidth = dayWidth - 2;
        int blockHeight = timeToY(bitis) - y;
        
        // Randevu bloğu arka planı
        g2.setColor(APPOINTMENT_COLOR);
        g2.fillRoundRect(x, y, blockWidth, blockHeight, 6, 6);
        
        // Randevu bloğu kenarları
        g2.setColor(new Color(80, 120, 200));
        g2.drawRoundRect(x, y, blockWidth, blockHeight, 6, 6);
        
        // Randevu detayları
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.drawString(randevu.getKonu(), x + 5, y + 15);
        
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.drawString(baslangic.format(DateTimeFormatter.ofPattern("HH:mm")) + 
                     " - " + bitis.format(DateTimeFormatter.ofPattern("HH:mm")), 
                     x + 5, y + 30);
    }
    
    private void drawMusaitlikBlock(Graphics2D g2, LocalTime saat, int dayWidth) {
        int y = timeToY(saat);
        int height = SLOT_HEIGHT;
        
        if (viewType == ViewType.WEEKLY) {
            // Haftalık görünümde sadece hafta içi günleri göster (Pazartesi-Cuma)
            int x = TIME_WIDTH;
            for (int i = 0; i < 5; i++) { // 0: Pazartesi, 4: Cuma
                LocalDate guncelGun = currentDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), i + 1);
                if (guncelGun.getDayOfWeek() == currentDate.getDayOfWeek()) {
                    int blockWidth = dayWidth - 2;
                    g2.fillRoundRect(x + 1, y, blockWidth, height, 4, 4);
                }
                x += dayWidth;
            }
        } else {
            // Günlük görünümde tüm müsait saatleri göster
            int blockWidth = dayWidth - 2;
            g2.fillRoundRect(TIME_WIDTH + 1, y, blockWidth, height, 4, 4);
        }
        
        // Seçili zaman dilimini vurgula
        if (selectedTime != null && 
            selectedTime.toLocalTime().equals(saat) &&
            selectedTime.toLocalDate().equals(currentDate)) {
            g2.setColor(SELECTED_COLOR);
            if (viewType == ViewType.WEEKLY) {
                int dayIndex = selectedTime.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
                if (dayIndex >= 0 && dayIndex < 5) {
                    int x = TIME_WIDTH + (dayIndex * dayWidth);
                    int blockWidth = dayWidth - 2;
                    g2.fillRoundRect(x + 1, y, blockWidth, height, 4, 4);
                    
                    // Seçilen bloğun kenarı
                    g2.setColor(new Color(200, 150, 0));
                    g2.drawRoundRect(x + 1, y, blockWidth, height, 4, 4);
                }
            } else {
                int blockWidth = dayWidth - 2;
                g2.fillRoundRect(TIME_WIDTH + 1, y, blockWidth, height, 4, 4);
                
                // Seçilen bloğun kenarı
                g2.setColor(new Color(200, 150, 0));
                g2.drawRoundRect(TIME_WIDTH + 1, y, blockWidth, height, 4, 4);
            }
        }
    }
    
    private void drawCurrentTimeLine(Graphics2D g2, int width) {
        // Sadece bugünü gösteriyorsa şu anki zamanı çiz
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        if ((viewType == ViewType.DAILY && currentDate.equals(today)) ||
            (viewType == ViewType.WEEKLY && 
             currentDate.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), today.getDayOfWeek().getValue()).equals(today))) {
            
            // Şu anki zaman çizgisi
            if (now.isAfter(START_TIME) && now.isBefore(END_TIME)) {
                int y = timeToY(now);
                g2.setColor(CURRENT_TIME_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                
                if (viewType == ViewType.WEEKLY) {
                    // Haftalık görünümde bugünün sütununda çiz
                    int dayIndex = today.getDayOfWeek().getValue() - 1;
                    int dayWidth = (width - TIME_WIDTH) / 5;
                    int x = TIME_WIDTH + (dayIndex * dayWidth);
                    g2.drawLine(x, y, x + dayWidth, y);
                    
                    // Zaman göstergesi (daire)
                    g2.fillOval(x - 4, y - 4, 8, 8);
                } else {
                    // Günlük görünümde tüm genişlik boyunca çiz
                    g2.drawLine(TIME_WIDTH, y, width, y);
                    
                    // Zaman göstergesi (daire)
                    g2.fillOval(TIME_WIDTH - 4, y - 4, 8, 8);
                }
                
                // Reset stroke
                g2.setStroke(new BasicStroke(1.0f));
            }
        }
    }
    
    private int timeToY(LocalTime time) {
        long minutesSinceStart = java.time.Duration.between(START_TIME, time).toMinutes();
        return DAY_HEADER_HEIGHT + ((int) minutesSinceStart * SLOT_HEIGHT / MINUTES_PER_SLOT);
    }

    public void addAvailableTime(LocalTime time) {
        musaitSaatler.add(time);
        repaint();
    }
    
    public void addAvailableTime(Musaitlik musaitlik) {
        musaitSaatler.add(musaitlik.getBaslangicSaati());
        repaint();
    }

    public void addAppointment(Randevu randevu) {
        randevular.add(randevu);
        repaint();
    }
} 