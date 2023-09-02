package com.teleostnacl.common.java.util;

import java.security.MessageDigest;

public class StringUtils {
    // 邮箱的正则表达式
    private static final String EMAIL_REGEX = "^([a-z\\dA-Z]+[-|\\.]?)+[a-z\\dA-Z]@([a-z\\dA-Z]+(-[a-z\\dA-Z]+)?\\.)+[a-zA-Z]{2,}$";

    // region MessageDigest algorithm
    public static final String ALGORITHM_MD2 = "MD2";
    public static final String ALGORITHM_MD5 = "MD5";
    public static final String ALGORITHM_SHA_1 = "SHA-1";
    public static final String ALGORITHM_SHA_224 = "SHA-1";
    public static final String ALGORITHM_SHA_256 = "SHA-256";
    public static final String ALGORITHM_SHA_384 = "SHA-384";
    public static final String ALGORITHM_SHA_512 = "SHA-512";
    public static final String ALGORITHM_SHA_512_224 = "SHA-512/224";
    public static final String ALGORITHM_SHA_512_256 = "SHA-512/256";
    // endregion

    public static String getOrBlank(String s) {
        return s == null ? "" : s;
    }

    public static boolean isEmail(String s) {
        return s != null && s.matches(EMAIL_REGEX);
    }

    /**
     * 使用MD5将数据加密
     *
     * @param bytes 待加密的数据
     * @return MD5加密后的字符串
     */
    public static String toMD5(byte[] bytes) {
        return toEncrypt(ALGORITHM_MD5, bytes);
    }

    /**
     * 使用SHA256将数据加密
     *
     * @param bytes 待加密的数据
     * @return SHA256加密后的字符串
     */
    public static String toSHA256(byte[] bytes) {
        return toEncrypt(ALGORITHM_SHA_256, bytes);
    }

    /**
     * 将数据以指定方式进行加密
     *
     * @param algorithm 指定方式
     * @param bytes     数据
     * @return 加密后的
     */
    public static String toEncrypt(String algorithm, byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.reset();
            messageDigest.update(bytes);
            byte[] byteArray = messageDigest.digest();
            for (byte b : byteArray) {
                if (Integer.toHexString(0xFF & b).length() == 1) {
                    stringBuilder.append("0").append(Integer.toHexString(0xFF & b));
                } else {
                    stringBuilder.append(Integer.toHexString(0xFF & b));
                }
            }
        } catch (Exception ignored) {
        }

        return stringBuilder.toString();
    }
}
