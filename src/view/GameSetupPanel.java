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

            var config = createConfig(GameMode.multiplayer, playerOne, playerTwo);
            var words = pickWords(config.wordLength(), 2);
            var state = gameController.startNewGame(config, words[0], words[1]);

            navigation.setGameState(state);
            navigation.showMultiplayerGame();
        });

        var solo = new JButton("Start Solo");
        solo.addActionListener(e -> {
            var player = new GamePlayer(new PlayerProfile("You", ""), true);
            var config = GameConfig.withDefaults(GameMode.solo, player, null);
            var words = pickWords(config.wordLength(), 1);
            var state = gameController.startNewGame(config, null, words[0]);
            
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
    
    private GameConfig createConfig(GameMode mode, GamePlayer playerOne, GamePlayer playerTwo) {
        return GameConfig.withDefaults(mode, playerOne, playerTwo);
    }

    private WordChoice[] pickWords(WordLength length, int count) {
        WordChoice[] choices = new WordChoice[count];
        for (int i = 0; i < count; i++) {
            String word;
            int attempts = 0;
            do {
                word = gameController.pickWord(length);
                attempts++;
            } while (i > 0 && word.equalsIgnoreCase(choices[i - 1].word()) && attempts < 3);
            choices[i] = new WordChoice(word, WordSource.rollTheDice);
        }
        return choices;
    }

    private final Navigation navigation;
    private final GameController gameController;
    static final long serialVersionUID = 1L;
}
