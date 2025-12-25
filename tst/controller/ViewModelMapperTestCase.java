package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import controller.events.DifficultyView;
import controller.events.GameLogEntryView;
import controller.events.TimerDurationView;
import controller.events.WordChoiceView;
import controller.events.WordLengthView;
import controller.events.WordSourceView;
import model.enums.Difficulty;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.enums.WordSource;
import org.junit.jupiter.api.Test;

class ViewModelMapperTestCase {

    private final ViewModelMapper mapper = new ViewModelMapper();

    @Test
    void mapsWordChoice() {
        var view = new WordChoiceView("APPLE", WordSourceView.rollTheDice);
        var model = mapper.toModel(view);
        assertEquals("APPLE", model.word());
        assertEquals(WordSource.rollTheDice, model.source());
    }

    @Test
    void mapsDifficultyWithDefault() {
        assertEquals(Difficulty.normal, mapper.toModel((DifficultyView) null));
        assertEquals(Difficulty.hard, mapper.toModel(DifficultyView.hard));
        assertEquals(Difficulty.expert, mapper.toModel(DifficultyView.expert));
    }

    @Test
    void mapsWordLengthWithDefault() {
        assertEquals(WordLength.five, mapper.toModel((WordLengthView) null));
        assertEquals(WordLength.three, mapper.toModel(WordLengthView.three));
        assertEquals(WordLength.six, mapper.toModel(WordLengthView.six));
    }

    @Test
    void mapsTimerDurationWithDefault() {
        assertEquals(TimerDuration.none, mapper.toModel((TimerDurationView) null));
        assertEquals(TimerDuration.oneMinute, mapper.toModel(TimerDurationView.oneMinute));
        assertEquals(TimerDuration.fiveMinutes, mapper.toModel(TimerDurationView.fiveMinutes));
    }

    @Test
    void mapsGameLogEntryWithFallbacks() {
        var view = new GameLogEntryView("A", "B", DifficultyView.hard, 4, "won");
        var model = mapper.toModel(view);
        assertNull(model.gameId());
        assertEquals("A", model.playerOneName());
        assertEquals("B", model.playerTwoName());
        assertEquals(Difficulty.hard, model.difficulty());
        assertEquals(WordLength.four, model.wordLength());
        assertEquals("won", model.resultSummary());
    }

    @Test
    void handlesNullDifficultyAsNull() {
        var view = new GameLogEntryView("A", "B", null, 5, "won");
        var model = mapper.toModel(view);
        assertNull(model.difficulty(), "Null difficulty should map to null");
        assertEquals(WordLength.five, model.wordLength());
    }

    @Test
    void unmatchedWordLengthMapsToNull() {
        var view = new GameLogEntryView("A", "B", DifficultyView.normal, 7, "won");
        var model = mapper.toModel(view);
        assertNull(model.wordLength(), "Unknown word length should map to null");
    }
}
