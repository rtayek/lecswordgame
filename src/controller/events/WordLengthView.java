package controller.events;

/**
 * View-friendly word length enum to avoid leaking domain types into the UI layer.
 */
public enum WordLengthView {
    three(3), four(4), five(5), six(6);

    private final int length;

    WordLengthView(int length) {
        this.length = length;
    }

    public int length() {
        return length;
    }

    public static WordLengthView fromLength(int length) {
        for (WordLengthView v : values()) {
            if (v.length == length) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unsupported word length: " + length);
    }
}
