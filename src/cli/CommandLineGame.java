package cli;

import java.util.Scanner;

import controller.DictionaryService;
import controller.GameController;
import model.GamePlayer;
import model.GameState;
import model.GuessOutcome;
import model.PlayerProfile;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.GameMode;
import model.enums.GameStatus;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.enums.WordSource;

/**
 * Minimal command-line driver for a solo game against the computer.
 * Keeps the UI surface fully separate from Swing.
 */
public final class CommandLineGame {

    public static void main(String[] args) {
        System.out.println("=== Word Guessing Game (CLI) ===");

        try (var scanner = new Scanner(System.in)) {
            var wordService = new DictionaryService();
            var gameController = new GameController(wordService);

            var human = new GamePlayer(new PlayerProfile(prompt(scanner, "Enter your name"), ""), true);
            var cpu = new GamePlayer(new PlayerProfile("Computer", ""), false);

            var config = new GameState.GameConfig(GameMode.solo, Difficulty.normal, WordLength.five, TimerDuration.none, human, cpu);

            boolean playAgain = true;
            while (playAgain) {
                String cpuWord = wordService.pickWord(config.wordLength());
                var state = gameController.startNewGame(config, null, new WordChoice(cpuWord, WordSource.manual));

                System.out.println("A 5-letter word has been chosen. Start guessing!");
                System.out.println(cpuWord);

                while (state.getStatus() != GameStatus.finished) {
                    System.out.print("> ");
                    String guess = scanner.nextLine();
                    if (guess == null) return;
                    guess = guess.trim();
                    if (guess.equalsIgnoreCase("quit")) {
                        System.out.println("Exiting game.");
                        return;
                    }
                    if (guess.isEmpty()) {
                        System.out.println("Please enter a guess or type 'quit'.");
                        continue;
                    }
                    try {
                        GuessOutcome outcome = gameController.submitGuess(state, human, guess);
                        printFeedback(outcome);
                        if (outcome.status() == GameStatus.finished) {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                if (state.getWinner() != null && state.getWinner().equals(human)) {
                    System.out.println("You win! The word was: " + cpuWord.toUpperCase());
                } else {
                    System.out.println("You lost. The word was: " + cpuWord.toUpperCase());
                }

                System.out.print("Play again? (y/n): ");
                String answer = scanner.nextLine();
                if (answer == null || !answer.trim().toLowerCase().startsWith("y")) {
                    playAgain = false;
                }
            }
        }
    }

    private static void printFeedback(GuessOutcome outcome) {
        var result = outcome.entry().result();
        System.out.print("Feedback: ");
        for (int i = 0; i < result.feedback().size(); i++) {
            var fb = result.feedback().get(i);
            char c = result.guess().charAt(i);
            String marker = switch (fb) {
                case correct -> "[+" + c + "]";
                case present -> "[~" + c + "]";
                case notPresent -> "[ " + c + "]";
                default -> "[ " + c + "]";
            };
            System.out.print(marker);
        }
        System.out.println(" (" + result.correctLetterCount() + " correct letters)");
    }

    private static String prompt(Scanner scanner, String message) {
        System.out.print(message + ": ");
        String line = scanner.nextLine();
        if (line == null || line.isBlank()) {
            return "Player";
        }
        return line.trim();
    }
}
