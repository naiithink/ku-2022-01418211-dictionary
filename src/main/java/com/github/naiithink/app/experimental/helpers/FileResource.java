package com.github.naiithink.app.experimental.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FileResource
        implements LocalResource<String> {

    private Path path;

    @Override
    public String read() throws IOException {
        String result;

        if (Files.size(path) > FILE_SIZE_THRESHOLD) {
            ByteBuffer resultBytes = ByteBuffer.allocate(Files.size(path));
            StringBuffer resultStringBuffer = new StringBuffer();

            try (InputStream in = Files.newInputStream(path)) {
                Objects.nonNull(in);

                while (in.available() > 0) {
                    resultBytes. in.read();
                }
            } catch (IOException e) {
                throw new IOException("", e);
            }
        } else {
            byte[] resultBytes = Files.readAllBytes(path);
        }

        return result;
    }
}
