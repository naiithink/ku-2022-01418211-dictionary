package com.github.naiithink.app.models;

public enum WordClass {

    NOUN            ("Noun"),
    VERB            ("Verb"),
    ADJECTIVE       ("Adjective"),
    ADVERB          ("Adverb"),
    PREPOSITION     ("Preposition"),
    PRONOUN         ("Pronoun"),
    DETERMINER      ("Determiner"),
    CONJUNCTION     ("Conjunction"),
    INTERJECTION    ("Interjection");

    private final String prettyPrinted;

    private WordClass(String prettyPrinted) {
        this.prettyPrinted = prettyPrinted;
    }

    public String getPrettyPrinted() {
        return this.prettyPrinted;
    }

    public static String[] getAllPrettyPrintedWordClass() {
        int len = WordClass.values().length;
        String[] result = new String[len];

        int i = 0;
        for (WordClass c : WordClass.values()) {
            result[i++] = c.prettyPrinted;
        }

        return result;
    }
}
