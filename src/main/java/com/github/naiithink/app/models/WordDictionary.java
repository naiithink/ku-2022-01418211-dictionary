package com.github.naiithink.app.models;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.controllers.EventController;
import com.github.naiithink.app.hotspot.Hotspot;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public final class WordDictionary {
    protected WordDictionary() {}

    private static WordDictionary instance;

    private static Logger logger;

    private static Map<String, Word> wordTable;

    static {
        logger = Logger.getLogger(WordDictionary.class.getName());
        wordTable = new ConcurrentHashMap<>();
    }

    public static WordDictionary getDictionary() {
        if (instance == null) {
            synchronized (WordDictionary.class) {
                if (instance == null) {
                    instance = new WordDictionary();
                }
            }
        }

        return instance;
    }

    public void addWord(String wordEntry,
                        Word word) {

        if (wordTable.containsKey(wordEntry)) {
            ButtonType replaceButton = new ButtonType("Replace", ButtonData.YES);
            Dialog<String> duplicateWordDialog = new Dialog<>();

            duplicateWordDialog.setTitle("Duplicate Word");
            duplicateWordDialog.setContentText("Do you want to replace the existing word?");
            duplicateWordDialog.getDialogPane().getButtonTypes().add(replaceButton);

            duplicateWordDialog.showAndWait()
                               .filter(response -> response == "Replace")
                               .ifPresent(response -> wordTable.put(wordEntry, word));
        } else {
            wordTable.put(wordEntry, word);
        }

        if (Hotspot.isAppInitialized) {
            EventController.publish(Hotspot.EventType.RECORD_CHANGED, null);
            logger.log(Level.INFO, "Added word, published RECORD_CHANGED to Seteners");
        }
    }

    public void deleteWord(String wordEntry) {
        if (wordTable.containsKey(wordEntry)) {
            ButtonType replaceButton = new ButtonType("Delete", ButtonData.YES);
            Dialog<String> duplicateWordDialog = new Dialog<>();

            duplicateWordDialog.setTitle("Duplicate Word");
            duplicateWordDialog.setContentText("Do you want to delete the word '" + wordEntry + "' ?" + "\n"
                                               + "Deleted is not recoverable.");
            duplicateWordDialog.getDialogPane().getButtonTypes().add(replaceButton);

            duplicateWordDialog.showAndWait()
                               .filter(response -> response == "Delete")
                               .ifPresent(response -> wordTable.remove(wordEntry));
        } else {
            wordTable.remove(wordEntry);
        }

        if (Hotspot.isAppInitialized) {
            EventController.publish(Hotspot.EventType.RECORD_CHANGED, null);
            logger.log(Level.INFO, "Deleted word, published RECORD_CHANGED to Listeners");
        }
    }

    public boolean containsWord(String wordEntry) {
        return wordTable.containsKey(wordEntry);
    }

    public Word getWord(String wordEntry) {
        return wordTable.get(wordEntry);
    }

    public Set<String> getAllWordEntries() {
        return new ConcurrentSkipListSet<>(wordTable.keySet());
    }

    public Set<Word> getAllWords() {
        Set<Word> allWords = new CopyOnWriteArraySet<>();

        for (Word word : wordTable.values()) {
            allWords.add(word);
        }

        return allWords;
    }
}
