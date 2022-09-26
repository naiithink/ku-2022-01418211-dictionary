package com.github.naiithink.app.experimental.helpers;

public interface SequentialResource<T>
        extends Resource<T> {

    T readLine();

    void writeLine(T line);
}
