package model.enums;

public enum WordLength {
    three(3), four(4), five(5), six(6);
    WordLength(int length) {
        this.length = length;
    }
    public int length() {
        return length;
    }
    final int length;
}
