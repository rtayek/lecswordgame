package controller;

import controller.events.WordChoiceView;
import controller.events.WordSourceView;

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

    public WordChoiceView buildChoice(String word) {
        String normalized = word == null ? "" : word.toUpperCase();
        boolean useDice = rolledByDice && normalized.equals(lastRolledWord);
        return new WordChoiceView(normalized, useDice ? WordSourceView.rollTheDice : WordSourceView.manual);
    }

    public void clear() {
        lastRolledWord = null;
        rolledByDice = false;
    }
}
