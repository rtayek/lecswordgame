package controller;

import controller.api.Navigation;
import controller.events.GameEvent;
import controller.events.GameEventListener;
import controller.events.GameLogEntryView;
import controller.events.HardWordEntryView;
import controller.events.PlayerProfileView;
import controller.events.WordChoiceView;
import controller.events.WordLengthView;
import controller.events.TimerDurationView;
import controller.events.WordSourceView;
import controller.events.DifficultyView;
import java.util.List;
import java.util.stream.Collectors;
import model.GamePlayer;
import model.GameState.GameConfig;
import model.PlayerProfile;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.GameMode;
import model.enums.TimerDuration;
import model.enums.WordLength;
import util.PersistenceService;

public class AppController {

    private final GameSessionService gameSessionService;
    private final WordSelectionFlow wordSelectionFlow = new WordSelectionFlow();
    private final ProfileService profileService;
    private final NavigationCoordinator navigationCoordinator = new NavigationCoordinator();

    public AppController(PersistenceService persistenceService, GameController gameController, TurnTimer turnTimer) {
        this.gameSessionService = new GameSessionService(gameController, turnTimer);
        this.profileService = new ProfileService(persistenceService);
    }
    
    public void addGameEventListener(GameEventListener listener) {
        gameSessionService.addEventListener(listener);
    }

    public void setNavigation(Navigation navigation) {
        navigationCoordinator.setNavigation(navigation);
    }

    public void requestNewGame(GameConfig config) {
        wordSelectionFlow.start(config);
        navigationCoordinator.showWordSelection(buildWordSelectionData(config, true));
    }
    
    public void playerOneWordSelected(WordChoiceView wordChoice) {
        var startRequest = wordSelectionFlow.playerOneSelected(toModel(wordChoice));
        if (startRequest != null) {
            startGame(startRequest.config(), startRequest.playerOne(), startRequest.playerTwo(), startRequest.playerOneWord(), startRequest.playerTwoWord());
        } else {
            var cfg = wordSelectionFlow.getPendingConfig();
            navigationCoordinator.showWordSelection(buildWordSelectionData(cfg, false));
        }
    }

    public void playerTwoWordSelected(WordChoiceView wordChoice) {
        var startRequest = wordSelectionFlow.playerTwoSelected(toModel(wordChoice));
        startGame(startRequest.config(), startRequest.playerOne(), startRequest.playerTwo(), startRequest.playerOneWord(), startRequest.playerTwoWord());
    }

    private void startGame(GameConfig config, GamePlayer playerOne, GamePlayer playerTwo, WordChoice p1Word, WordChoice p2Word) {
        var state = gameSessionService.startNewGame(config, p1Word, p2Word);

        if (config.mode() == GameMode.multiplayer) {
            navigationCoordinator.showMultiplayerGame();
        } else {
            navigationCoordinator.showSoloGame();
        }
        // Clear pending states
        wordSelectionFlow.clear();
    }
    
    public void submitGuess(String guess) {
        if (gameSessionService.getCurrentGameState() == null) {
            throw new IllegalStateException("Start a new game first.");
        }
        gameSessionService.submitGuess(guess);
    }

    public PlayerProfileView getCurrentProfile() {
        var profile = profileService.getCurrentProfile();
        if (profile == null) return new PlayerProfileView("", "");
        return new PlayerProfileView(profile.username(), profile.avatarPath());
    }

    public void setCurrentProfile(PlayerProfileView currentProfile) {
        if (currentProfile == null) return;
        profileService.saveProfile(new PlayerProfile(currentProfile.username(), currentProfile.avatarPath()));
    }

    public List<GameLogEntryView> getGameLog() {
        return profileService.getGameLogs().stream()
                .map(e -> new GameLogEntryView(
                        e.playerOneName(),
                        e.playerTwoName(),
                        e.difficulty() == null ? "" : e.difficulty().name(),
                        e.wordLength() == null ? 0 : e.wordLength().length(),
                        e.resultSummary()))
                .collect(Collectors.toList());
    }

    public void addGameLogEntry(model.GameLogEntry entry) {
        profileService.addGameLogEntry(entry);
    }
    
    public List<HardWordEntryView> getHardestWords() {
        return profileService.getHardestWords().stream()
                .map(e -> new HardWordEntryView(e.rank(), e.word(), e.hardnessScore()))
                .collect(Collectors.toList());
    }

    public String pickWord(WordLengthView length) {
        return gameSessionService.pickWord(toModel(length));
    }

    public boolean isValidWord(String word, WordLengthView length) {
        return gameSessionService.isValidWord(word, toModel(length));
    }

    public void reportWinnerKnowledge(boolean winnerKnewWord) {
        gameSessionService.applyWinnerKnowledge(winnerKnewWord);
    }

    public WordSelectionViewData buildWordSelectionData(GameConfig config, boolean isPlayerOneTurn) {
        if (config == null) return null;
        var opponent = isPlayerOneTurn ? config.playerTwo() : config.playerOne();
        var name = opponent != null && opponent.profile() != null ? opponent.profile().username() : "Player";
        boolean isMultiplayer = config.mode() == GameMode.multiplayer;
        return new WordSelectionViewData(name, config.wordLength().length(), isMultiplayer, isPlayerOneTurn);
    }

    public void startMultiplayerGame(String playerOneName, String playerTwoName, DifficultyView difficulty,
                                     WordLengthView wordLength, TimerDurationView timer) {
        var p1 = new model.GamePlayer(new model.PlayerProfile(playerOneName, ""), true);
        var p2 = new model.GamePlayer(new model.PlayerProfile(playerTwoName, ""), true);
        var config = new GameConfig(model.enums.GameMode.multiplayer, toModel(difficulty), toModel(wordLength), toModel(timer), p1, p2);
        requestNewGame(config);
    }

    public void startSoloGame(String playerName, DifficultyView difficulty,
                              WordLengthView wordLength, TimerDurationView timer) {
        var human = new model.GamePlayer(new model.PlayerProfile(playerName, ""), true);
        var cpu = new model.GamePlayer(new model.PlayerProfile("Computer", ""), false);
        var config = new GameConfig(model.enums.GameMode.solo, toModel(difficulty), toModel(wordLength), toModel(timer), human, cpu);
        requestNewGame(config);
    }

    private WordChoice toModel(WordChoiceView view) {
        if (view == null) return null;
        var source = view.source() == WordSourceView.rollTheDice ? model.enums.WordSource.rollTheDice : model.enums.WordSource.manual;
        return new WordChoice(view.word(), source);
    }

    private Difficulty toModel(DifficultyView view) {
        if (view == null) return Difficulty.normal;
        return switch (view) {
            case normal -> Difficulty.normal;
            case hard -> Difficulty.hard;
            case expert -> Difficulty.expert;
        };
    }

    private WordLength toModel(WordLengthView view) {
        if (view == null) return WordLength.five;
        return switch (view) {
            case three -> WordLength.three;
            case four -> WordLength.four;
            case five -> WordLength.five;
            case six -> WordLength.six;
        };
    }

    private TimerDuration toModel(TimerDurationView view) {
        if (view == null) return TimerDuration.none;
        return switch (view) {
            case none -> TimerDuration.none;
            case oneMinute -> TimerDuration.oneMinute;
            case threeMinutes -> TimerDuration.threeMinutes;
            case fourMinutes -> TimerDuration.fourMinutes;
            case fiveMinutes -> TimerDuration.fiveMinutes;
        };
    }
}
