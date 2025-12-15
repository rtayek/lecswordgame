package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.*;
import model.Enums.*;

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

            gameController.startNewMultiplayerGame(
                    playerOne,
                    playerTwo,
                    difficulty,
                    wordLength,
                    timerDuration
            );

            var state = gameController.getGameState();
            state.setPlayerOneWord(new WordChoice("apple", WordSource.manual));
            state.setPlayerTwoWord(new WordChoice("grape", WordSource.manual));

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
