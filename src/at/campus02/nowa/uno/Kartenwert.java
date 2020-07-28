package at.campus02.nowa.uno;

public enum Kartenwert {
    zero(0), eins(1), zwei(2), drei(3), vier(4), fünf(5), sechs(6),
    sieben(7), acht(8), neun(9), plus2(20), RW(20), OUT(20),
    WILD(50), plus4(50);
    int cardPoints;

    private static final Kartenwert[] WERTE = Kartenwert.values();// Returns an array containing the constants
    // of this enum type, in the order they're declared.

    Kartenwert(int cardPoints) {
        this.cardPoints = cardPoints;
    }

    public static Kartenwert getWert(int i) {
        return WERTE[i];
    }
}
