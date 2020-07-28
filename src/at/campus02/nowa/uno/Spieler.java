package at.campus02.nowa.uno;

import java.util.LinkedList;

public class Spieler {
    private String name;
    private int points;
    private LinkedList<UnoKarte> handCardDeck;

    public Spieler(String name) {
        this.name = name;

        this.points = 0;    //anfänglich immer 0
        handCardDeck = new LinkedList<>();
    }

    // Vergleich der eingegebenen Karte mit dem Handkartenset. Ist die Karte im Handkartenset enthalten?
    public UnoKarte getKarte(Farbe f, Kartenwert kw) {
        for (UnoKarte handKarte : handCardDeck) {
            if (handKarte.getFARBE() == f && handKarte.getKARTENWERT() == kw) {
                return handKarte;
            }
        }
        return null;
    }

    public UnoKarte validPlus4Turn(Farbe f, Kartenwert kw) {
        for (UnoKarte handKarte : handCardDeck) {
            if (handKarte.getFARBE() == f || handKarte.getKARTENWERT() == kw) {
                return handKarte;
            }
        }
        return null;
    }

    public UnoKarte getFirstPlus2(UnoKarte previousCard) {
        for (UnoKarte u : getHandCardDeck()) {
            if (App.validPlus2(u, previousCard))
                return u;
        }
        return null;
    }

    public LinkedList<UnoKarte> getHandCardDeck() {
        return handCardDeck;
    }

    public void setHandCardDeck(LinkedList<UnoKarte> handCardDeck) {
        this.handCardDeck = handCardDeck;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public boolean isBot() {
        return this instanceof Bot;
    }

    @Override
    public String toString() {
        return "Spieler Name: " + name + ", Punkte = " + points +
                ", Handkarten: " + handCardDeck + '\n';
    }
}