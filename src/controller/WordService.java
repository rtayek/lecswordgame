package controller;

import model.enums.WordLength;

public interface WordService {
    String pickWord(WordLength wordLength);
    boolean isValidWord(String word, WordLength wordLength);
}
