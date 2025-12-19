package controller;

import model.WordChoice;
import model.enums.WordSource;

/**
 * Tracks word selection state (rolled vs manual) and builds WordChoice.
 */
public class WordSelectionModel {
    private String lastRolledWord;
    private boolean rolledByDice;

    public void recordRoll(String word) {
        this.lastRolledWord = word == null ? "" : word.toUpperCase();
        this.rolledByDice = true;
    }

    public void onInputChanged(String text) {
        String current = text == null ? "" : text.trim().toUpperCase();
        if (!current.equals(lastRolledWord)) {
            rolledByDice = false;
        }
    }

    public WordChoice buildChoice(String word) {
        String normalized = word == null ? "" : word.toUpperCase();
        boolean useDice = rolledByDice && normalized.equals(lastRolledWord);
        return new WordChoice(normalized, useDice ? WordSource.rollTheDice : WordSource.manual);
    }

    public void clear() {
        lastRolledWord = null;
        rolledByDice = false;
    }
}
