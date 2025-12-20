package controller;

import controller.NextAction;
import util.SoundEffect;

public record OutcomeViewModel(
        String title,
        String message,
        String graphicFile,
        SoundEffect soundEffect,
        NextAction nextAction
) { }
