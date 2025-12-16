package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Enums.Difficulty;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Enums.WordSource;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;
import model.Records.WordChoice;

class GameSetupPanel extends JPanel {

    GameSetupPanel(Navigation navigation, GameController gameController) {
        setLayout(new BorderLayout(8, 8));
        add(new JLabel("Game Setup"), BorderLayout.NORTH);

        var options = new JPanel(new GridLayout(0, 1, 6, 6));

        var multiplayer = new JButton("Start Multiplayer");
        multiplayer.addActionListener(e -> {
            var playerOne = new GamePlayer(new PlayerProfile("Player 1", ""), true);
            var playerTwo = new GamePlayer(new PlayerProfile("Player 2", ""), true);

            var difficulty = Difficulty.normal;
            var wordLength = WordLength.five;
            var timerDuration = TimerDuration.none;
            var wordOne = gameController.pickWord(wordLength);
            var wordTwo = gameController.pickWord(wordLength);
            // Avoid identical words when possible.
            if (wordTwo.equals(wordOne)) {
                wordTwo = gameController.pickWord(wordLength);
            }

            gameController.startNewMultiplayerGame(
                    playerOne,
                    playerTwo,
                    difficulty,
                    wordLength,
                    timerDuration
            );

            var state = gameController.getGameState();
            state.setPlayerOneWord(new WordChoice(wordOne, WordSource.rollTheDice));
            state.setPlayerTwoWord(new WordChoice(wordTwo, WordSource.rollTheDice));

            navigation.showMultiplayerGame();
        });

        var solo = new JButton("Start Solo");
        solo.addActionListener(e -> navigation.showSoloGame());

        options.add(multiplayer);
        options.add(solo);
        add(options, BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    static final long serialVersionUID = 1L;
}
