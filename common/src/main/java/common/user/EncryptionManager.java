package common.user;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

public class EncryptionManager {
    public static final String algorithm = "SHA-256";

    public static String generateSalt(int saltLength){
        // Допустимые символы
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789$%?!*&^()[]<>.,#@";

        Random random = new Random();
        StringBuilder sb = new StringBuilder(saltLength);

        // Генерируем последовательность случайных символов
        for (int i = 0; i < saltLength; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }

    public static boolean verify(String pwd, byte[] encrypted) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return Arrays.equals(md.digest((pwd).getBytes(StandardCharsets.UTF_8)), encrypted);
    }

    public static boolean verify(String pwd, String encrypted) {
        return verify(pwd, hexStringToByteArray(encrypted));
    }

    public static String encryptHex(String pwd, String salt) {
        try {
            return encryptHex(pwd, salt, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String encryptHex(String pwd, String salt, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        byte[] res =  md.digest((pwd + salt).getBytes(StandardCharsets.UTF_8));
        return byteArrayToHexString(res);
    }

    public static byte[] encrypt(String pwd) {
        try {
            return encrypt(pwd, algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] encrypt(String pwd, String algorithm) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithm);
        return md.digest((pwd).getBytes(StandardCharsets.UTF_8));
    }

    public static String byteArrayToHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (byte datum : data) {
            String s = Integer.toHexString(0xff & datum);
            s = (s.length() == 1) ? "0" + s : s;
            sb.append(s);
        }
        return sb.toString();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}
