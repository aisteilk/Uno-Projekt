package at.campus02.nowa.uno;


public enum Farbe {
    ROT, GRÜN, BLAU, YELLOW, SCHWARZ;

    private static final Farbe[] FARBEN = Farbe.values();   // .values() --> Returns an array containing the constants
                                                            // of this enum type, in the order they're declared.
    public static Farbe getFarbe(int i){

        return FARBEN[i];
    }

}
