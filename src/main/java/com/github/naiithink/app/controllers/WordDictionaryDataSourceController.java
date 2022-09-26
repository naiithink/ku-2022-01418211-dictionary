package com.github.naiithink.app.controllers;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.services.WordDictionaryDataSource;

public final class WordDictionaryDataSourceController
        extends WordDictionaryDataSource {

    private static WordDictionaryDataSourceController instance;

    private WordDictionaryDataSourceController(Path path) {
        super(path);
    }

    public static WordDictionaryDataSourceController getInstance() {
        if (instance == null) {
            synchronized (WordDictionaryDataSourceController.class) {
                if (instance == null) {
                    instance = new WordDictionaryDataSourceController(Paths.get(Hotspot.Resource.DataSourceReference.DATA_SOURCE_FILE));
                }
            }
        }

        return instance;
    }
}
