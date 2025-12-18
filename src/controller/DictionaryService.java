package controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import model.enums.WordLength;
import util.Constants; // Correctly placed import

/**
 * A service responsible for providing words from a dictionary.
 */
public class DictionaryService {

    private static final Map<WordLength, Set<String>> WORD_BANK = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // Hardcoded word bank for testing and fallback
    private static final Map<WordLength, Set<String>> DEFAULT_WORD_BANK = Map.of(
            WordLength.three, Set.of("CAT", "SUN", "MAP"),
            WordLength.four, Set.of("TREE", "LION", "BOAT"),
            WordLength.five, Set.of("APPLE", "GRAPE", "PLANE", "BREAD"),
            WordLength.six, Set.of("ORANGE", "PLANET", "STREAM")
    );

    public DictionaryService() {
        loadWordBank();
        if (WORD_BANK.isEmpty()) { // If loading from file failed, use default
            System.out.println("Using default word bank as file loading failed or returned empty.");
            DEFAULT_WORD_BANK.forEach((wordLength, words) ->
                WORD_BANK.put(wordLength, Collections.unmodifiableSet(new java.util.HashSet<>(words)))
            );
        }
    }

    private void loadWordBank() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(Constants.RESOURCES_PATH + "words.txt")))) {
            Map<WordLength, List<String>> tempMap = reader.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .collect(Collectors.groupingBy(word -> {
                        for (WordLength wl : WordLength.values()) {
                            if (wl.length() == word.length()) {
                                return wl;
                            }
                        }
                        return null;
                    }));

            tempMap.forEach((wordLength, words) -> {
                if (wordLength != null) {
                    WORD_BANK.put(wordLength, Collections.unmodifiableSet(new java.util.HashSet<>(words)));
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to load word bank from file: " + e.getMessage());
            // Fallback will be handled in the constructor
        }
    }

    public String pickWord(WordLength wordLength) {
        Set<String> words = WORD_BANK.get(wordLength);
        if (words == null || words.isEmpty()) {
            throw new IllegalStateException("No words available for length " + wordLength.length());
        }
        int idx = random.nextInt(words.size());
        return words.toArray(new String[0])[idx];
    }

    public boolean isValidWord(String word, WordLength wordLength) {
        if (word == null || word.isEmpty()) {
            return false;
        }
        Set<String> words = WORD_BANK.get(wordLength);
        return words != null && words.contains(word.toUpperCase());
    }
}
