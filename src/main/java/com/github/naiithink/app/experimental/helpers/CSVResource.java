package com.github.naiithink.app.experimental.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class CSVResource
        implements SequentialResource<String> {

    private Path path;

    public CSVResource(Path path) throws FileNotFoundException {
        if (Files.exists(path) == false) {
            throw new FileNotFoundException(path.toAbsolutePath().toString());
        }

        this.path = path;
    }

    @Override
    public Path getPath() {
        return path;
    }

    @Override
    public String readLine() {
        return 
    }
}
