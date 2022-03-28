package dev.mayuna.lostarkbot.util;

import lombok.SneakyThrows;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;

public class HashUtils {

    @SneakyThrows
    public static String hashMD5(String string) {
        byte[] bytes = MessageDigest.getInstance("MD5").digest(string.getBytes(StandardCharsets.UTF_8));
        BigInteger bigInteger = new BigInteger(1, bytes);
        return String.format("%0" + (bytes.length << 1) + "X", bigInteger);
    }

    public static boolean equalsHashString(String hash, String string) {
        return Objects.equals(hash, hashMD5(string));
    }

    public static boolean equalsHashHash(String hash1, String hash2) {
        return Objects.equals(hash1, hash2);
    }
}
