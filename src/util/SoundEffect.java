package util;

public enum SoundEffect {
    WIN("win.wav"),
    LOSE("lose.wav"),
    TIE("tie.wav");

    private final String fileName;

    SoundEffect(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
