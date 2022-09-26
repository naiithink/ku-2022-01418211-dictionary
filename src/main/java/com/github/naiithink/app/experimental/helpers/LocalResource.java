package com.github.naiithink.app.experimental.helpers;

import java.io.IOException;

public interface LocalResource<T>
        extends Resource<T> {

    T read() throws IOException;

    void write(T record);
}
