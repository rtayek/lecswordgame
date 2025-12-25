package controller;

import controller.events.DifficultyView;
import controller.events.GameLogEntryView;
import controller.events.TimerDurationView;
import controller.events.WordChoiceView;
import controller.events.WordLengthView;
import controller.events.WordSourceView;
import model.GameLogEntry;
import model.WordChoice;
import model.enums.Difficulty;
import model.enums.TimerDuration;
import model.enums.WordLength;
import model.enums.WordSource;

/**
 * Centralized conversion from view DTOs to domain/model types.
 */
class ViewToModelMapper {

    WordChoice toModel(WordChoiceView view) {
        if (view == null) return null;
        WordSource source = view.source() == WordSourceView.rollTheDice ? WordSource.rollTheDice : WordSource.manual;
        return new WordChoice(view.word(), source);
    }

    Difficulty toModel(DifficultyView view) {
        if (view == null) return Difficulty.normal;
        return switch (view) {
            case normal -> Difficulty.normal;
            case hard -> Difficulty.hard;
            case expert -> Difficulty.expert;
        };
    }

    Difficulty toModelNullable(DifficultyView view) {
        if (view == null) return null;
        return toModel(view);
    }

    WordLength toModel(WordLengthView view) {
        if (view == null) return WordLength.five;
        return switch (view) {
            case three -> WordLength.three;
            case four -> WordLength.four;
            case five -> WordLength.five;
            case six -> WordLength.six;
        };
    }

    TimerDuration toModel(TimerDurationView view) {
        if (view == null) return TimerDuration.none;
        return switch (view) {
            case none -> TimerDuration.none;
            case oneMinute -> TimerDuration.oneMinute;
            case threeMinutes -> TimerDuration.threeMinutes;
            case fourMinutes -> TimerDuration.fourMinutes;
            case fiveMinutes -> TimerDuration.fiveMinutes;
        };
    }

    GameLogEntry toModel(GameLogEntryView view) {
        if (view == null) return null;
        Difficulty difficulty = toModelNullable(view.difficulty());
        WordLength length = null;
        for (WordLength wl : WordLength.values()) {
            if (wl.length() == view.wordLength()) {
                length = wl;
                break;
            }
        }
        return new GameLogEntry(
                null,
                view.playerOneName(),
                view.playerTwoName(),
                difficulty,
                length,
                view.resultSummary());
    }
}
