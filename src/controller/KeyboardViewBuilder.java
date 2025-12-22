package controller;

import controller.events.KeyboardView;
import controller.events.LetterFeedbackView;
import java.util.HashMap;
import java.util.Map;
import model.GameState;

/**
 * Builds a keyboard view from game state guesses and difficulty.
 */
class KeyboardViewBuilder {

    KeyboardView build(GameState state) {
        var keyStates = new HashMap<Character, LetterFeedbackView>();
        var difficulty = state.getConfig().difficulty();

        state.getGuesses().forEach(g -> {
            var guess = g.result().guess();
            var feedback = g.result().feedback();

            if (difficulty == model.enums.Difficulty.expert) {
                for (int i = 0; i < guess.length(); i++) {
                    char c = Character.toUpperCase(guess.charAt(i));
                    merge(keyStates, c, LetterFeedbackView.absent); // gray for used
                }
                return;
            }

            int n = Math.min(guess.length(), feedback.size());
            for (int i = 0; i < n; i++) {
                char c = Character.toUpperCase(guess.charAt(i));
                var fb = feedback.get(i);
                if (fb == null) continue;

                LetterFeedbackView v = switch (fb) {
                    case correct -> LetterFeedbackView.correct;
                    case present -> LetterFeedbackView.present;
                    case notPresent -> LetterFeedbackView.absent;
                    default -> LetterFeedbackView.absent;
                };

                merge(keyStates, c, v);
            }
        });

        return new KeyboardView(Map.copyOf(keyStates));
    }

    private void merge(Map<Character, LetterFeedbackView> map, char c, LetterFeedbackView newV) {
        LetterFeedbackView oldV = map.get(c);
        if (oldV == null || strength(newV) > strength(oldV)) {
            map.put(c, newV);
        }
    }

    private int strength(LetterFeedbackView v) {
        return switch (v) {
            case correct -> 3;
            case present -> 2;
            case absent -> 1;
            case unused -> 0;
        };
    }
}
