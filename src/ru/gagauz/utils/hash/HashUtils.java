package ru.gagauz.utils.hash;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class HashUtils {
    private static final String BASE_32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private static final int[] BASE_32_LOOKUP = {0xFF, 0xFF, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F, // '0',
            // '1',
            // '2',
            // '3',
            // '4',
            // '5',
            // '6',
            // '7'
            0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // '8', '9', ':',
            // ';', '<', '=',
            // '>', '?'
            0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '@', 'A', 'B',
            // 'C', 'D', 'E',
            // 'F', 'G'
            0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'H', 'I', 'J',
            // 'K', 'L', 'M',
            // 'N', 'O'
            0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'P', 'Q', 'R',
            // 'S', 'T', 'U',
            // 'V', 'W'
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, // 'X', 'Y', 'Z',
            // '[', '\', ']',
            // '^', '_'
            0xFF, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, // '`', 'a', 'b',
            // 'c', 'd', 'e',
            // 'f', 'g'
            0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, // 'h', 'i', 'j',
            // 'k', 'l', 'm',
            // 'n', 'o'
            0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, // 'p', 'q', 'r',
            // 's', 't', 'u',
            // 'v', 'w'
            0x17, 0x18, 0x19, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF // 'x', 'y', 'z',
            // '{', '|', '}',
            // '~', 'DEL'
    };

    private static final ThreadLocal<MessageDigest> MD5_DIGEST_HOLDER = new ThreadLocal<MessageDigest>() {

        @Override
        protected MessageDigest initialValue() {
            try {
                return MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    private static Mac HMAC_SHA256;

    static {
        try {
            HMAC_SHA256 = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static final byte[] HEX_CHAR_TABLE = {
            (byte) '0',
            (byte) '1',
            (byte) '2',
            (byte) '3',
            (byte) '4',
            (byte) '5',
            (byte) '6',
            (byte) '7',
            (byte) '8',
            (byte) '9',
            (byte) 'a',
            (byte) 'b',
            (byte) 'c',
            (byte) 'd',
            (byte) 'e',
            (byte) 'f'
    };

    private HashUtils() {
    }

    public static String getHexString(byte... raw) throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }

        return new String(hex, "ASCII");
    }

    public static String encodeBase32(final byte... bytes) {
        int i = 0, index = 0, digit;
        int currByte, nextByte;
        StringBuilder base32 = new StringBuilder((bytes.length + 7) * 8 / 5);

        while (i < bytes.length) {
            currByte = (bytes[i] >= 0) ? bytes[i] : (bytes[i] + 256); // unsign

            /* Is the current digit going to span a byte boundary? */
            if (index > 3) {
                if ((i + 1) < bytes.length) {
                    nextByte = (bytes[i + 1] >= 0) ? bytes[i + 1] : (bytes[i + 1] + 256);
                } else {
                    nextByte = 0;
                }

                digit = currByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0) {
                    i++;
                }
            }
            base32.append(BASE_32_CHARS.charAt(digit));
        }

        return base32.toString();
    }

    public static byte[] decodeBase32(final String base32) {
        final String baseInUpper = base32.toUpperCase();
        int i, index, lookup, offset, digit;
        byte[] bytes = new byte[baseInUpper.length() * 5 / 8];

        for (i = 0, index = 0, offset = 0; i < baseInUpper.length(); i++) {
            lookup = baseInUpper.charAt(i) - '0';

            /* Skip chars outside the lookup table */
            if (lookup < 0 || lookup >= BASE_32_LOOKUP.length) {
                continue;
            }

            digit = BASE_32_LOOKUP[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit == 0xFF) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length) {
                        break;
                    }
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }

    public static String md5(final String data) {
        try {
            MessageDigest md5 = MD5_DIGEST_HOLDER.get();
            md5.reset();
            return getHexString(md5.digest(data.getBytes()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String sha256(String key, String data) {
        try {
            final SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
            HMAC_SHA256.init(secret_key);
            final byte[] macData = HMAC_SHA256.doFinal(data.getBytes());
            return getHexString(macData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Encodes data using base64 encoding.
     *
     * @param abyte0 data for encoding
     * @return encoded string
     */
    public static String encodeBase64(final byte[] abyte0) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < abyte0.length; i += 3) {
            stringBuilder.append(encodeBlock(abyte0, i));
        }

        return stringBuilder.toString();
    }

    private static char[] encodeBlock(final byte[] abyte0, final int i) {
        int j = 0;
        final int k = abyte0.length - i - 1;
        final int l = k < 2 ? k : 2;

        for (int i1 = 0; i1 <= l; i1++) {
            final byte byte0 = abyte0[i + i1];
            final int j1 = byte0 >= 0 ? ((int) (byte0)) : byte0 + 256;
            j += j1 << 8 * (2 - i1);
        }

        char[] ac = new char[4];

        for (int k1 = 0; k1 < 4; k1++) {
            final int l1 = j >>> 6 * (3 - k1) & 0x3f;
            ac[k1] = getChar(l1);
        }

        if (k < 1) {
            ac[2] = '=';
        }

        if (k < 2) {
            ac[3] = '=';
        }

        return ac;
    }

    private static char getChar(final int i) {

        if (i >= 0 && i <= 25) {
            return (char) (65 + i);
        }

        if (i >= 26 && i <= 51) {
            return (char) (97 + (i - 26));
        }

        if (i >= 52 && i <= 61) {
            return (char) (48 + (i - 52));
        }

        if (i == 62) {
            return '+';
        }

        return i != 63 ? '?' : '/';
    }

    /**
     * Decode string using Base64 encoding.
     *
     * @param s string for decoding
     * @return decoded data
     */
    public static byte[] decodeBase64(final String s) {

        if (s.length() == 0) {
            return new byte[0];
        }

        int i = 0;

        for (int j = s.length() - 1; j > 0 && s.charAt(j) == '='; j--) {
            i++;
        }

        final int k = (s.length() * 6) / 8 - i;
        byte[] abyte0 = new byte[k];
        int l = 0;
        for (int i1 = 0; i1 < s.length(); i1 += 4) {
            final int j1 = (getValue(s.charAt(i1)) << 18) + (getValue(s.charAt(i1 + 1)) << 12) + (getValue(s.charAt(i1 + 2)) << 6) + getValue(s.charAt(i1 + 3));
            for (int k1 = 0; k1 < 3 && l + k1 < abyte0.length; k1++) {
                abyte0[l + k1] = (byte) (j1 >> 8 * (2 - k1) & 0xff);
            }

            l += 3;
        }

        return abyte0;
    }

    private static int getValue(final char c) {

        if (c >= 'A' && c <= 'Z') {
            return c - 65;
        }

        if (c >= 'a' && c <= 'z') {
            return (c - 97) + 26;
        }

        if (c >= '0' && c <= '9') {
            return (c - 48) + 52;
        }

        if (c == '+') {
            return 62;
        }

        if (c == '/') {
            return 63;
        }

        return c != '=' ? -1 : 0;
    }

    static {
        String testSha = sha256("134250D8D304360F478CE5AB4927188DADFCEC52",
                "certificate=256ea0dc91370e401b6521773ee639f8,email=example@example.com,login=example,order_number=666-ФА-1/ЦЛФ-123");

        if (!testSha.equals("60769a73f307b8351bbd9ea60ec114185d416c8d5b8e5b04f74f1e7999a6981f")) {
            throw new IllegalStateException("sha256 test failed");
        }
    }
}
