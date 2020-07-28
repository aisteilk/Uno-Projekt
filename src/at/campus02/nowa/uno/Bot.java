package at.campus02.nowa.uno;

public class Bot extends Spieler {

    public Bot(String name) {
        super(name);
    }

    public UnoKarte getFirstValidCard(UnoKarte previousCard) {
        for (UnoKarte u : getHandCardDeck()) {
            if (App.validCard(u, previousCard))
                return u;
        }

        return null;
    }

    /*public UnoKarte getFirstPlus2(UnoKarte previousCard) {
        for (UnoKarte u : getHandCardDeck()) {
            if (App.validPlus2(u, previousCard))
                return u;
        }
        return null;
    }*/

    @Override
    public String toString() {
        return "Bot Name: " + getName() + ", Punkte = " + getPoints() +
                ", Handkarten: " + getHandCardDeck() + '\n';
    }
}
