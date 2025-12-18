package util;

public enum SoundEffect {
    win("win.wav"),
    lose("lose.wav"),
    tie("tie.wav");

    private final String fileName;

    SoundEffect(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
