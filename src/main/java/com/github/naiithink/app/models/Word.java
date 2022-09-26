package com.github.naiithink.app.models;

import java.util.List;

public class Word {

    private String wordLiteral;

    private WordClass wordClass;

    private String wordDefinition;

    private List<String> exampleInSentences;

    public Word(String wordLiteral,
                WordClass wordClass,
                String wordDefinition,
                List<String> exampleInSentences) {

        this.wordLiteral = wordLiteral.toLowerCase();
        this.wordClass = wordClass;
        this.wordDefinition = wordDefinition;
        this.exampleInSentences = exampleInSentences;
    }

    public String getWordLiteral() {
        return wordLiteral;
    }

    public WordClass getWordClass() {
        return wordClass;
    }

    public String getWordDefinition() {
        return wordDefinition;
    }

    public List<String> getExampleInSentences() {
        return exampleInSentences;
    }

    public void setWordDefinition(String wordDefinition) {
        this.wordDefinition = wordDefinition;
    }

    public void setExampleInSentences(List<String> exampleInSentences) {
        this.exampleInSentences = exampleInSentences;
    }

    public void addExampleInSentence(String exampleInSentence) {
        this.exampleInSentences.add(exampleInSentence);
    }

    public void addExampleInSentences(List<String> exampleInSentences) {
        this.exampleInSentences.addAll(exampleInSentences);
    }
}
