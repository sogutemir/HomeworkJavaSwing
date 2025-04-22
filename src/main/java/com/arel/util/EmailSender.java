package com.arel.util;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

/**
 * E-posta gönderimi için yardımcı sınıf
 */
public class EmailSender {
    private static final String HOST = "smtp.gmail.com";
    private static final String PORT = "587";
    private static final String USERNAME = "akademik.randevu@gmail.com"; // Örnek e-posta adresi, değiştirin
    private static final String PASSWORD = "your_app_password"; // Gmail için uygulama şifresi gerekir, güvensiz olmasın diye buraya yazmayalım
    
    private static Properties getProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        return props;
    }
    
    private static Session getSession() {
        return Session.getInstance(getProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });
    }
    
    /**
     * E-posta gönderir
     *
     * @param to Alıcı e-posta adresi
     * @param subject Konu başlığı
     * @param body İçerik
     * @return Gönderim başarılı ise true, değilse false
     */
    public static boolean sendEmail(String to, String subject, String body) {
        try {
            Session session = getSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
            System.out.println("E-posta gönderildi: " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("E-posta gönderilirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * HTML formatında e-posta gönderir
     *
     * @param to Alıcı e-posta adresi
     * @param subject Konu başlığı
     * @param htmlBody HTML içeriği
     * @return Gönderim başarılı ise true, değilse false
     */
    public static boolean sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            Session session = getSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html; charset=utf-8");
            Transport.send(message);
            System.out.println("HTML e-posta gönderildi: " + to);
            return true;
        } catch (MessagingException e) {
            System.err.println("HTML e-posta gönderilirken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Randevu onaylandığında gönderilecek e-posta
     */
    public static boolean sendRandevuOnayEmail(String to, String ogrenciAdi, String ogretimUyesiAdi, 
                                              String tarih, String saat, String konu) {
        String subject = "Randevu Talebiniz Onaylandı";
        String body = "Sayın " + ogrenciAdi + ",\n\n" +
                "Sayın " + ogretimUyesiAdi + " ile " + tarih + " tarihinde saat " + saat + 
                " için oluşturduğunuz randevu talebiniz onaylanmıştır.\n\n" +
                "Randevu Konusu: " + konu + "\n\n" +
                "İyi çalışmalar dileriz.\n" +
                "Akademik Randevu Sistemi";
        
        return sendEmail(to, subject, body);
    }
    
    /**
     * Randevu reddedildiğinde gönderilecek e-posta
     */
    public static boolean sendRandevuRedEmail(String to, String ogrenciAdi, String ogretimUyesiAdi, 
                                             String tarih, String saat, String konu, String redNedeni) {
        String subject = "Randevu Talebiniz Reddedildi";
        String body = "Sayın " + ogrenciAdi + ",\n\n" +
                "Sayın " + ogretimUyesiAdi + " ile " + tarih + " tarihinde saat " + saat + 
                " için oluşturduğunuz randevu talebiniz reddedilmiştir.\n\n" +
                "Randevu Konusu: " + konu + "\n" +
                "Red Nedeni: " + redNedeni + "\n\n" +
                "Başka bir zaman dilimi için yeniden randevu oluşturabilirsiniz.\n" +
                "İyi çalışmalar dileriz.\n" +
                "Akademik Randevu Sistemi";
        
        return sendEmail(to, subject, body);
    }
    
    /**
     * Yeni randevu talebi oluşturulduğunda öğretim üyesine gönderilecek e-posta
     */
    public static boolean sendYeniRandevuTalebiEmail(String to, String ogretimUyesiAdi, String ogrenciAdi, 
                                                  String tarih, String saat, String konu) {
        String subject = "Yeni Randevu Talebi";
        String body = "Sayın " + ogretimUyesiAdi + ",\n\n" +
                "Öğrenci " + ogrenciAdi + " tarafından " + tarih + " tarihinde saat " + saat + 
                " için yeni bir randevu talebi oluşturulmuştur.\n\n" +
                "Randevu Konusu: " + konu + "\n\n" +
                "Randevu talebini onaylamak veya reddetmek için lütfen Akademik Randevu Sistemine giriş yapınız.\n" +
                "İyi çalışmalar dileriz.\n" +
                "Akademik Randevu Sistemi";
        
        return sendEmail(to, subject, body);
    }
} 