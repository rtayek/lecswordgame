package view;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

class InstructionsPanel extends JPanel {

    InstructionsPanel(Navigation navigation) {
        setLayout(new BorderLayout(8, 8));
        setBackground(new java.awt.Color(0xFFF3E0)); // soft peach
        add(new JLabel("Instructions"), BorderLayout.NORTH);

        var text = new JTextArea("""
            Welcome to the Word Guessing Game!

            **Objective:** Guess the secret word chosen by your opponent or the computer.

            **Game Modes:**
            *   **Multiplayer:** One player picks a word for the opponent to guess.
            *   **Solo:** Guess words against the computer.

            **Word Length:** Choose from 3, 4, 5, or 6 letters.

            **Difficulty Modes:**
            *   **Normal:**
                *   Green: Letter is correct and in the right location.
                *   Orange: Letter is present in the word but in the wrong location.
                *   Red: Letter is not in the word.
                *   Keyboard: Letters turn green if in word, red if not in word.
            *   **Hard:**
                *   Green: Letter is present in the word (location not specified).
                *   Red: Letter is not in the word.
                *   Keyboard: Letters turn green if in word, red if not in word.
            *   **Expert:**
                *   Only a number indicates the count of correct letters (no color clues).
                *   Keyboard: Letters just gray out after use (no color coding).

            **Timer (Optional):** Choose 1, 3, 4, or 5 minutes per player. Timer becomes red when time is low.

            **Word Selection:** You can manually pick a word or "Roll the Dice" to have the computer choose.

            **Winning/Losing:**
            *   First to guess correctly wins.
            *   If one player guesses, the opponent gets one final chance.
            *   "Did you know this word?" prompt: If you didn't know the word, you win automatically. If you did and your opponent also guesses correctly, it's a tie.
            *   Losses and wins are signaled with sounds and graphics.

            **Other Features:**
            *   **Profile Setup:** Set your username and avatar.
            *   **Friends:** Add friends and connect.
            *   **Game Log:** View past game details.
            *   **Hardest Words:** See a list of challenging words.
            """);
        text.setEditable(false);
        add(new JScrollPane(text), BorderLayout.CENTER);

        var back = new JButton("Back");
        back.addActionListener(e -> navigation.showLanding());
        add(back, BorderLayout.SOUTH);
    }

    private static final long serialVersionUID = 1L;
}
