package com.github.crypto.to.moon.trading.service.util;

import com.google.protobuf.ByteString;

import java.math.BigDecimal;
import java.math.BigInteger;

public class BigDecimalProtoUtils {

    public static ByteString bigDecimalToBytes(BigDecimal value) {
        if (value == null) {
            return ByteString.EMPTY;
        }
        BigInteger unscaledValue = value.unscaledValue();
        int scale = value.scale();
        byte[] unscaledBytes = unscaledValue.toByteArray();
        byte[] result = new byte[unscaledBytes.length + 4]; // 4 bytes for scale
        System.arraycopy(unscaledBytes, 0, result, 4, unscaledBytes.length);
        intToBytes(scale, result, 0);
        return ByteString.copyFrom(result);
    }

    public static BigDecimal bytesToBigDecimal(ByteString bytes) {
        if (bytes.isEmpty()) {
            return null;
        }
        byte[] byteArray = bytes.toByteArray();
        int scale = bytesToInt(byteArray, 0);
        byte[] unscaledBytes = new byte[byteArray.length - 4];
        System.arraycopy(byteArray, 4, unscaledBytes, 0, unscaledBytes.length);
        BigInteger unscaledValue = new BigInteger(unscaledBytes);
        return new BigDecimal(unscaledValue, scale);
    }

    private static void intToBytes(int value, byte[] bytes, int offset) {
        bytes[offset] = (byte) (value >> 24);
        bytes[offset + 1] = (byte) (value >> 16);
        bytes[offset + 2] = (byte) (value >> 8);
        bytes[offset + 3] = (byte) value;
    }

    private static int bytesToInt(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xFF) << 24) |
                ((bytes[offset + 1] & 0xFF) << 16) |
                ((bytes[offset + 2] & 0xFF) << 8) |
                (bytes[offset + 3] & 0xFF);
    }

}
