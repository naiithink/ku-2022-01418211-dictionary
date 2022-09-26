package com.github.naiithink.app.experimental.models;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.naiithink.app.models.WordClass;

import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

public class Word {

    private static Logger logger;

    private String wordLiteral;

    private Map<WordClass, Synonym> synonymPool;

    private class Synonym {

        private WordClass wordClass;

        private String wordDefinition;

        private List<String> exampleInSentences;

        public Synonym(WordClass wordClass,
                       String wordDefinition,
                       List<String> exampleInSentences) {

            this.wordClass = wordClass;
            this.wordDefinition = String.valueOf(wordDefinition);
            this.exampleInSentences.addAll(exampleInSentences);
        }

        public void addExampleSentence(String exampleSentence) {
            this.exampleInSentences.add(this.exampleInSentences.size(), exampleSentence);
        }

        public void addExampleSentence(List<String> exampleSentences) {
            this.exampleInSentences.addAll(exampleSentences);
        }

        public void removeExampleSentence(int sentenceIndex) {
            this.exampleInSentences.remove(sentenceIndex);
        }

        public String getWordDefinition() {
            return this.wordDefinition;
        }

        public List<String> getExampleInSentences() {
            return this.exampleInSentences;
        }
    }

    static {
        logger = Logger.getLogger(Word.class.getName());
    }

    public Word(String wordLiteral,
                WordClass wordClass,
                String wordDefinition,
                List<String> exampleInSentences) {

        this.synonymPool = new ConcurrentHashMap<>();

        this.wordLiteral = String.valueOf(wordLiteral);
        this.synonymPool.put(wordClass, new Synonym(wordClass, wordDefinition, exampleInSentences));
    }

    public String getWordLiteral() {
        return wordLiteral;
    }

    public String getDefinition(WordClass wordClass) {
        if (synonymPool.containsKey(wordClass) == false) {
            logger.log(Level.WARNING, "Requesting undefined word '" + this.wordLiteral + "' if class '" + wordClass.getPrettyPrinted() + "'");
            return null;
        }

        return synonymPool.get(wordClass).getWordDefinition();
    }

    public void addDefinition(WordClass wordClass,
                              String wordDefinition,
                              List<String> exampleInSentences) {

        if (synonymPool.containsKey(wordClass)) {
            ButtonType replaceButton = new ButtonType("Replace", ButtonData.YES);
            Dialog<String> duplicateWordDialog = new Dialog<>();

            duplicateWordDialog.setTitle("Word Already Exist");
            duplicateWordDialog.setContentText("Do you want to replace the existing word '"+ wordLiteral +"' of type "+ wordClass.getPrettyPrinted() + "?");
            duplicateWordDialog.getDialogPane().getButtonTypes().add(replaceButton);

            duplicateWordDialog.showAndWait()
                               .filter(response -> response == "Replace")
                               .ifPresent(response -> synonymPool.replace(wordClass, new Synonym(wordClass, wordDefinition, exampleInSentences)));

            return;
        }

        synonymPool.put(wordClass, new Synonym(wordClass, wordDefinition, exampleInSentences));
    }

    @Override
    public String toString() {
        return wordLiteral + " - synonym count: " + synonymPool.keySet().size() + " entries";
    }
}
