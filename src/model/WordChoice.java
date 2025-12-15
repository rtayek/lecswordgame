package model;

import model.Enums.WordSource;

public class WordChoice {

    public WordChoice(String word, WordSource source) {
        this.word = word;
        this.source = source;
    }

    public String getWord() {
        return word;
    }

    public WordSource getSource() {
        return source;
    }

    final String word;
    final WordSource source;
}
