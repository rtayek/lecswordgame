package controller;

import java.util.Map;
import java.util.Random;
import model.Enums.WordLength;

/**
 * A service responsible for providing words from a dictionary.
 */
public class DictionaryService {

    public String pickWord(WordLength wordLength) {
        String[] words = WORD_BANK.get(wordLength);
        if (words == null || words.length == 0) {
            throw new IllegalStateException("No words available for length " + wordLength.length());
        }
        int idx = random.nextInt(words.length);
        return words[idx];
    }

    private static final Map<WordLength, String[]> WORD_BANK = Map.of(
            WordLength.three, new String[]{"CAT", "SUN", "MAP"},
            WordLength.four, new String[]{"TREE", "LION", "BOAT"},
            WordLength.five, new String[]{"APPLE", "GRAPE", "PLANE", "BREAD"},
            WordLength.six, new String[]{"ORANGE", "PLANET", "STREAM"}
    );
    private final Random random = new Random();
}
