package controller.events;

import java.util.Map;

/**
 * Immutable keyboard status map: letter -> feedback state.
 */
public record KeyboardView(Map<Character, LetterFeedbackView> keyStates) { }
