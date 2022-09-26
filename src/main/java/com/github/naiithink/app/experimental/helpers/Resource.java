package com.github.naiithink.app.experimental.helpers;

import java.io.Closeable;
import java.nio.file.Path;

import com.github.naiithink.app.helpers.Hotspot;
import com.github.naiithink.app.helpers.Hotspot.IECBinary;

public interface Resource<T>
        extends Closeable {

    long FILE_SIZE_THRESHOLD = 2L * Hotspot.Universal.IECBinary.GIBI.value();

    public Path getPath();

    default String byteArrayToHexString(byte[] bytes) {
        String result = new String();

        for (int i = 0, len = bytes.length; i < len; i++) {
            result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
        }

        return result;
    }
}
