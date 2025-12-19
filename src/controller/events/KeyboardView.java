package controller.events;

import java.util.Map;

/**
 * Immutable keyboard status map: letter -> status string.
 */
public record KeyboardView(Map<Character, String> keyStates) { }
