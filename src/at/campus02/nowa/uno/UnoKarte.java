package at.campus02.nowa.uno;

public class UnoKarte {

    private Farbe FARBE;    // darf ich nicht final machen, weil ich sonst keine farbwünsche vergeben kann
    private Kartenwert KARTENWERT;  // darf ich nicht final machen, weil ich sonst bei farbwünschen den Wert nicht setzen kann
    private boolean playedAlready;

    public UnoKarte(Farbe FARBE, Kartenwert KARTENWERT) {
        this.FARBE = FARBE;
        this.KARTENWERT = KARTENWERT;
        playedAlready = false;
    }

    public void setPlayedAlready() {
        playedAlready = true;
    }

    public boolean isPlayedAlready() {
        return playedAlready;
    }

    public void setPlayedAlreadyToFalse() {
        playedAlready = false;
    }

    public Farbe getFARBE() {
        return FARBE;
    }

    public Kartenwert getKARTENWERT() {
        return KARTENWERT;
    }

    public void setFARBE(Farbe FARBE) {                 // brauchen wir, wenn wir bei einem Farbwunsch die Farbe
        this.FARBE = FARBE;                             // setzen
    }

    public void setKARTENWERT(Kartenwert KARTENWERT) {  // brauchen wir, wenn wir bei einem Farbwunsch den
        this.KARTENWERT = KARTENWERT;                   // "Standardwert" auf "0" setzen
    }

    @Override
    public String toString() {
        return FARBE + " " + "\"" + KARTENWERT + "\"";
    }


}
