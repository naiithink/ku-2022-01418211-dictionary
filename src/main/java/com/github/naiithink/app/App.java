package com.github.naiithink.app;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.controllers.StageController;
import com.github.naiithink.app.controllers.WordDictionaryDataSourceController;
import com.github.naiithink.app.helpers.ResourcePrefix;
import com.github.naiithink.app.hotspot.Hotspot;
import com.github.naiithink.app.models.Word;
import com.github.naiithink.app.models.WordDictionary;
import com.github.naiithink.app.util.resources.MalformedDataSourceException;

import javafx.application.Application;
import javafx.stage.Stage;

// @MainAppObject
public final class App extends Application {

    private Logger logger;

    @Override
    public void start(Stage primaryStage) {
        configLogger();

        Hotspot.Resource.ResourceIndex.readResourceIndex();
        loadDictionaryDataSource();

        configStageController(primaryStage);

        Hotspot.isAppInitialized = true;
    }

    private void configLogger() {
        logger = Logger.getLogger(App.class.getName());
        logger.log(Level.INFO, "Logger configuration has been set");
    }

    private void configStageController(Stage primaryStage) {
        StageController stageController = StageController.getInstance();

        stageController.dispatch(ResourcePrefix.getPrefix().resolve(Hotspot.Resource.ResourceIndex.getProperty("index.dir.fxml"))
                                                           .resolve(Hotspot.Resource.ResourceIndex.getProperty("index.file.fxml")),
                                 ResourcePrefix.getPrefix().resolve(Hotspot.Resource.ResourceIndex.getProperty("dir.fxml")),
                                 this,
                                 primaryStage,
                                 Hotspot.UI.APP_TITLE,
                                 Hotspot.UI.STAGE_WIDTH,
                                 Hotspot.UI.STAGE_HEIGHT);

        stageController.activate();
    }

    private void loadDictionaryDataSource() {
        WordDictionary wordDictionary = WordDictionary.getDictionary();
        WordDictionaryDataSourceController wordDictionaryDataSource = WordDictionaryDataSourceController.getInstance();

        Optional<List<Word>> optWordList = Optional.empty();

        try {
            optWordList = Optional.ofNullable(wordDictionaryDataSource.readData());
        } catch (MalformedDataSourceException e) {
            logger.log(Level.SEVERE, "Data source file is malformed, stop");

            System.exit(1);
        }

        List<Word> wordList = optWordList.get();

        for (Word word : wordList) {
            wordDictionary.addWord(word.getWordLiteral(), word);
        }

        logger.log(Level.INFO, "Loaded data from source: " + Hotspot.Resource.DataSourceReference.DATA_SOURCE_FILE);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
