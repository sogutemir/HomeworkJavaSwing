package com.arel.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Şifre güvenliği için yardımcı sınıf
 */
public class PasswordUtil {
    
    private static final int SALT_LENGTH = 16;
    
    /**
     * Rastgele tuz değeri oluşturur
     */
    public static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    /**
     * Şifreyi SHA-256 ile hashler
     * 
     * @param password Kullanıcı şifresi
     * @param salt Tuz değeri
     * @return Hashlenen şifre
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes(StandardCharsets.UTF_8));
            byte[] hashedPassword = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedPassword);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Şifre hashleme hatası", e);
        }
    }
    
    /**
     * Şifreleri karşılaştırır
     * 
     * @param password Kullanıcın girdiği şifre
     * @param salt Veritabanından alınan tuz
     * @param hashedPassword Veritabanından alınan hashlenmiş şifre
     * @return Şifreler eşleşiyorsa true, eşleşmiyorsa false
     */
    public static boolean verifyPassword(String password, String salt, String hashedPassword) {
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(hashedPassword);
    }
    
    /**
     * Rastgele şifre oluşturur
     * 
     * @param length Şifre uzunluğu
     * @return Rastgele şifre
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(chars.length());
            sb.append(chars.charAt(randomIndex));
        }
        
        return sb.toString();
    }
    
    /**
     * Şifre gücünü kontrol eder
     * 
     * @param password Kontrol edilecek şifre
     * @return Şifre yeterince güçlüyse true, değilse false
     */
    public static boolean isStrongPassword(String password) {
        // En az 8 karakter
        if (password.length() < 8) {
            return false;
        }
        
        // En az bir büyük harf
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // En az bir küçük harf
        if (!password.matches(".*[a-z].*")) {
            return false;
        }
        
        // En az bir rakam
        if (!password.matches(".*\\d.*")) {
            return false;
        }
        
        // En az bir özel karakter
        if (!password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:'\",.<>/?].*")) {
            return false;
        }
        
        return true;
    }
} 