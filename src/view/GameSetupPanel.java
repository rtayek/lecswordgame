package view;

import controller.GameController;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Enums.Difficulty;
import model.Enums.GameMode;
import model.Enums.TimerDuration;
import model.Enums.WordLength;
import model.Enums.WordSource;
import model.GameState.GameConfig;
import model.Records.GamePlayer;
import model.Records.PlayerProfile;
import model.Records.WordChoice;

class GameSetupPanel extends JPanel {

    GameSetupPanel(Navigation navigation, GameController gameController) {
        this.navigation = navigation;
        this.gameController = gameController;
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
            
            var config = new GameConfig(GameMode.multiplayer, difficulty, wordLength, timerDuration, playerOne, playerTwo);

            var wordOneStr = gameController.pickWord(wordLength);
            var wordTwoStr = gameController.pickWord(wordLength);
            if (wordTwoStr.equals(wordOneStr)) {
                wordTwoStr = gameController.pickWord(wordLength);
            }
            var wordOne = new WordChoice(wordOneStr, WordSource.rollTheDice);
            var wordTwo = new WordChoice(wordTwoStr, WordSource.rollTheDice);

            var state = gameController.startNewGame(config, wordOne, wordTwo);

            navigation.setGameState(state);
            navigation.showMultiplayerGame();
        });

        var solo = new JButton("Start Solo");
        solo.addActionListener(e -> {
            var player = new GamePlayer(new PlayerProfile("You", ""), true);
            var difficulty = Difficulty.normal;
            var wordLength = WordLength.five;
            var timer = TimerDuration.none;

            var config = new GameConfig(GameMode.solo, difficulty, wordLength, timer, player, null);
            var targetWord = gameController.pickWord(wordLength);
            var wordChoice = new WordChoice(targetWord, WordSource.rollTheDice);

            var state = gameController.startNewGame(config, null, wordChoice);
            
            navigation.setGameState(state);
            navigation.showSoloGame();
        });

        options.add(multiplayer);
        options.add(solo);
        add(options, BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }
    
    private final Navigation navigation;
    private final GameController gameController;
    static final long serialVersionUID = 1L;
}
