package com.github.naiithink.app.util.resources;

import java.util.List;

public interface DataSource<T> {

    List<T> readData() throws MalformedDataSourceException;

    void writeRecord(T data);
}
