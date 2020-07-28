package at.campus02.nowa.uno;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.*;

public class App {
    private final Scanner input;
    private final PrintStream output;
    private KartenDeck spielKarten = new KartenDeck();
    private Stack<UnoKarte> stack = new Stack<>();        // Ablagestapel
    private ArrayList<Spieler> playersList = new ArrayList<>();
    private int indexCurrentPlayer;
    private int direction = 1;
    private int counterPlus2 = 1;
    private int roundCounter = 0;
    private int sessionCounter = 0;
    private SqliteClient client;
    private static final String CREATETABLE = "CREATE TABLE Sessions (Player varchar(100) NOT NULL, Session int NOT NULL, Round int NOT NULL, Score int NOT NULL, CONSTRAINT PK_Sessions PRIMARY KEY (Player, Session, Round));";
    private static final String INSERT_TEMPLATE = "INSERT INTO Sessions (Player, Session, Round, Score) VALUES ('%1s', %2d, %3d, %4d);";
    private static final String SELECT_BYPLAYERANDSESSION = "SELECT Player, SUM(Score) AS Score FROM Sessions WHERE Player = '%1s' AND Session = %2d;";
    private static final String SELECT_MAX = "SELECT MAX(Session) as Session FROM Sessions";


    public App(Scanner input, PrintStream output) {
        this.input = input;
        this.output = output;
    }

    public void Run() {
        try {
            initializeGame();
            do {
                initializeRound();

                while (!roundEnded()) {
                    printState();                   // Ausgabe der aktuellen Situation: welcher Spieler ist dran, welche Karte liegt oben
                    makeTurn();                     // Eingabe der Karten, Heben, Weiter
                    updateState();                  // nächster Spieler unter Berücksichtigung der Sonderkarten
                    Thread.sleep(1000);
                }
            } while (!gameEnded());

            printFinalScore();
        } catch (Exception ex) {
            ex.printStackTrace();
            output.println(ex);
        }
    }

    private void initializeGame() throws SQLException { //  Karten erstellen und mischen, Spielernamen eingeben 4x
        client = new SqliteClient("UnoDatabase.sqlite");
        if (!client.tableExists("Sessions")) {
            client.executeStatement(CREATETABLE);
        }
        ArrayList<HashMap<String, String>> results = client.executeQuery(SELECT_MAX);
        try {
            sessionCounter = Integer.parseInt(results.get(0).get("Session"));
        } catch (NumberFormatException e) {
            output.println(e);
            sessionCounter = 0;
        }
        sessionCounter++;
        //System.out.println(sessionCounter);

        spielKarten.makeDeck();
        spielKarten.shuffleDeck();
        initializePlayer();
    }

    // Spieler eingeben
    private void initializePlayer() {
        System.out.println("Bitte gib die Anzahl der Bots ein (0-4): ");
        int botCount = input.nextInt();
        while (botCount < 0 || botCount > 4) {
            System.out.println("Anzahl muss zwischen 0 und 4 sein. Gib die Anzahl erneut ein:");
            botCount = input.nextInt();
        }
        for (int i = 0; i < botCount; i++) {
            System.out.println("Name Bot " + (i + 1) + ": ");
            String name = input.next();
            Bot b = new Bot(name);
            addSpieler(b);
        }
        int playerCount = 4 - botCount;
        for (int i = 0; i < playerCount; i++) {
            System.out.println("Name Spieler " + (i + 1) + ": ");
            String name = input.next();
            Spieler s = new Spieler(name);
            addSpieler(s);
        }
    }

    private void initializeRound() {    // 4x 7 Karten verteilen, 1 Karte aufdecken, Aktionskarte prüfen, Startspieler auslosen
        dealOutCards();
        chooseRandomPlayer();
        determineStartCard();
    }

    // Methode, um die Spieler der Spielerliste hinzuzufügen
    private void addSpieler(Spieler s) {
        playersList.add(s);
    }

    // Methode, um die Handkarten den Spielern "auszuteilen"
    private void dealOutCards() {
        for (Spieler s : playersList) {
            s.setHandCardDeck(spielKarten.makePlayerDeck());
        }
    }

    // Methode, um Start-Spieler zu bestimmen
    private void chooseRandomPlayer() {
        Random random = new Random();
        int max = playersList.size() - 1;
        int min = 0;
        indexCurrentPlayer = random.nextInt((max - min) + 1) + min;
        Spieler s = playersList.get(indexCurrentPlayer);
        System.out.println("[iNFO] Startspieler ist: " + s.getName());
        System.out.println("Los geht's!");
    }

    // Methode, um die erste Karte "aufzudecken"
    private void determineStartCard() {
        UnoKarte startCard = spielKarten.drawPile.remove();     // oberste Karte vom Nachziehstapel wird der Startkarte zugewiesen
        stack.push(startCard);  // und dem Stack zugefügt
        System.out.println("[iNFO] Startkarte: " + startCard.toString());

        if (startCard.getKARTENWERT().equals(Kartenwert.OUT)) {
            skip();
            System.out.println("[iNFO] Der Start-Spieler wird übersprungen. Der nächste Spieler ist dran.");
        } else if (startCard.getKARTENWERT().equals(Kartenwert.RW)) {
            changeDirection();
            System.out.println("[iNFO] Spielrichtung wird geändert");
        } else if (startCard.getKARTENWERT().equals(Kartenwert.plus2))
            plus2();
        else if (startCard.getKARTENWERT().equals(Kartenwert.plus4)) {
            System.out.println("[iNFO] Die Startkarte hat die Farbe " + colorGenerator() + ".");
            System.out.println("[iNFO] Der Start-Spieler " + playersList.get(indexCurrentPlayer).getName() + " hebt vier Karten. Der nächste Spieler ist dran.");
            drawCardsPlus4();
        } else if (startCard.getKARTENWERT().equals(Kartenwert.WILD))
            System.out.println("[iNFO] Die Startkarte hat die Farbe " + colorGenerator() + ".");
    }

    private String colorGenerator() {
        ArrayList<String> colors = new ArrayList<>(Arrays.asList(
                String.valueOf(Farbe.ROT),
                String.valueOf(Farbe.BLAU),
                String.valueOf(Farbe.GRÜN),
                String.valueOf(Farbe.YELLOW)));

        Random rand = new Random();

        String selectedColor = colors.get(rand.nextInt(3) + 1);
        stack.lastElement().setFARBE(Farbe.valueOf(selectedColor));

        return selectedColor;
    }

    private void addCardToHandcardDeck() { //Karten vom Nachziehstapel hinzufügen
        playersList.get(indexCurrentPlayer).getHandCardDeck().add(spielKarten.drawPile.remove());

    }

    // Methode: Bot macht seinen Zug
    private void makeBotTurn() {
        Bot b = (Bot) playersList.get(indexCurrentPlayer);

        UnoKarte u = b.getFirstValidCard(stack.lastElement());
        if (u != null) {
            System.out.println(b.getName() + " legt " + u.toString());
            b.getHandCardDeck().remove(u);
            stack.push(u);

        } else {
            System.out.println(b.getName() + " hat keine passende Karte und hebt eine neue vom Stapel...");
            if (spielKarten.drawPile.size() <= 1) {  // falls der Nachziehstapel weniger/gleich 1 Karten beinhaltet
                makeNewDeckWhenPileIsEmpty();
            }
            b.getHandCardDeck().add(spielKarten.drawPile.remove()); // bot bekommt 1 Karte vom Pile in sein Hand-deck
            stack.lastElement().setPlayedAlready();                     // Funktion playedAlready wird aktiviert

            // Bot prüft ob er jetzt eine Karte spielen darf
            UnoKarte un = b.getFirstValidCard(stack.lastElement());
            if (un != null) {
                System.out.println(b.getName() + " legt " + un.toString());
                b.getHandCardDeck().remove(un);
                stack.push(un);
            }
        }
        // Bot soll UNO sagen
        if (b.getHandCardDeck().size() == 1) {
            System.out.println("Bot ruft UNOoooo!");
        }

    }

    private void getPlayersPoints() {

        for (Spieler s : playersList) {
            try {
                ArrayList<HashMap<String, String>> results = client.executeQuery(String.format(SELECT_BYPLAYERANDSESSION, s.getName(), sessionCounter));
                for (HashMap<String, String> map : results) {
                    System.out.println(map.get("Player") + " hat derzeit:  " + map.get("Score") + " Punkte");
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }

    }

    // Methode, um die Karteneingabe des menschlichen Spielers zu verarbeiten
    private void readUserInput() {

        Farbe f = null;
        Kartenwert kW = null;
        boolean correctInput = false;

        int drawCardCounter = 0;

        while (!correctInput) {
            System.out.println("Wähle eine Karte aus deiner Hand oder hebe eine Karte vom Nachziehstapel...");
            System.out.println("Wenn du dir nicht sicher bist, gib \"Hilfe\" ein.");
            System.out.println("Wenn du den aktuellen Punktestand wissen willst, gib \"Punkte\" ein.");
            String consoleInput = input.next();
            String userInput = consoleInput.toUpperCase();

            if (userInput.equals("PUNKTE")) {
                getPlayersPoints();
            } else if (userInput.equals("HILFE")) {
                callHelp();
            } else if (userInput.equals("HEBEN")) {
                drawCardCounter = drawCard(drawCardCounter);
            } else if (userInput.equals("WEITER")) {  // nur wenn bereits 1x gehoben wurde, darf man "weiter sagen"
                if (drawCardCounter >= 1) {
                    correctInput = true;
                    stack.lastElement().setPlayedAlready();
                    drawCardCounter = 0;
                } else
                    System.out.println("Du musst erst eine Karte vom Nachziehstapel nehmen, bevor du \"weiter\" sagen kannst.");
            } else {

                String[] values = userInput.split("-");

                try {                           // IndexOutOfBoundsException wird geworfen, wenn die Eingabe nicht richtig erfolgt
                    // z.B. nur R anstelle von R-9, dann ist das values[] nicht vollständig

                    switch (values[0]) {
                        case "R":
                            f = Farbe.ROT;
                            break;
                        case "B":
                            f = Farbe.BLAU;
                            break;
                        case "G":
                            f = Farbe.GRÜN;
                            break;
                        case "Y":
                            f = Farbe.YELLOW;
                            break;
                        case "S":
                            f = Farbe.SCHWARZ;
                            break;
                        default:
                            values[0] = null;
                            break;
                    }

                    switch (values[1]) {
                        case "0":
                            kW = Kartenwert.zero;
                            break;
                        case "1":
                            kW = Kartenwert.eins;
                            break;
                        case "2":
                            kW = Kartenwert.zwei;
                            break;
                        case "3":
                            kW = Kartenwert.drei;
                            break;
                        case "4":
                            kW = Kartenwert.vier;
                            break;
                        case "5":
                            kW = Kartenwert.fünf;
                            break;
                        case "6":
                            kW = Kartenwert.sechs;
                            break;
                        case "7":
                            kW = Kartenwert.sieben;
                            break;
                        case "8":
                            kW = Kartenwert.acht;
                            break;
                        case "9":
                            kW = Kartenwert.neun;
                            break;
                        case "RW":
                            kW = Kartenwert.RW;
                            break;
                        case "OUT":
                            kW = Kartenwert.OUT;
                            break;
                        case "+2":
                            kW = Kartenwert.plus2;
                            break;
                        case "+4":
                            kW = Kartenwert.plus4;
                            break;
                        case "WILD":
                            kW = Kartenwert.WILD;
                            break;
                        default:
                            values[1] = null;
                            break;
                    }
                    switch (values[2]) {
                        case "U":
                        case "UNO":
                            System.out.println("UNOoooo!");
                            break;
                        default:
                            values[2] = null;
                            break;
                    }

                } catch (IndexOutOfBoundsException e) {
                    //System.out.println("Du hast die Karte falsch eingegeben. Richtige Eingabe z.B. \"R-7\". ");
                }
                correctInput = isInputCorrectWithUNO(f, kW, correctInput, values);
            }
        }
    }

    private boolean isInputCorrectWithUNO(Farbe f, Kartenwert kW, boolean korrekteEingabe, String[] values) {
        if (f != null && kW != null) {      // Prüfung, ob die eingegebene Karte im Handkartenset vorhanden ist

            UnoKarte u = playersList.get(indexCurrentPlayer).getKarte(f, kW);

            if (u != null) {                // wenn es die Karte im Handkartenset gibt

                if (validTurn(u)) {         // prüfen, ob Karte gespielt werden darf
                    playersList.get(indexCurrentPlayer).getHandCardDeck().remove(u);   // gültig: entferne sie aus handkarten
                    stack.push(u);                                         // füge sie zum stack hinzu

                    System.out.println("Korrekte Karteneingabe.");
                    korrekteEingabe = true;         // while Schleife verlassen, methode readuserinput verlassen --> weiter zu updateState

                    try {
                        if ((playersList.get(indexCurrentPlayer).getHandCardDeck().size() == 1) && values[2].isEmpty()) {

                        }

                    } catch (ArrayIndexOutOfBoundsException e) {    // wenn kein uno eingegeben wurde
                        System.out.println("Du hast vergessen, UNO zu rufen und erhälst eine Strafkarte!");
                        addCardToHandcardDeck();
                    } catch (NullPointerException nullPointerException) { // wenn was anderes als uno eingegeben wurde
                        System.out.println("Du hast vergessen, UNO zu rufen und erhälst eine Strafkarte!");
                        addCardToHandcardDeck();
                    }

                } else {
                    System.out.println("Diese Karte darf nicht gespielt werden. Du bekommst eine Strafkarte. ");
                    addCardToHandcardDeck();
                    korrekteEingabe = true;

                }
            } else {
                System.out.println("Diese Karte ist nicht in den Handkarten vorhanden. ");
            }
        } else
            System.out.println("FALSCHE EINGABE!");
        return korrekteEingabe;
    }

    // Spielzug abhängig ob Mensch oder BOT
    private void makeTurn() {
        Spieler currentPlayer = playersList.get(indexCurrentPlayer);

        if (currentPlayer.isBot())
            makeBotTurn();
        else
            readUserInput();
    }

    // Methode "HILFE"
    private void callHelp() {
        BufferedReader helpReader = null;
        try {
            helpReader = new BufferedReader(new FileReader("help.txt"));

            String line;
            while ((line = helpReader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (helpReader != null)
                    helpReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Methode "HEBEN"
    private int drawCard(int drawCardCounter) {
        // wenn bereits 1x gehoben wurde
        if (drawCardCounter == 1) {
            System.out.println("Es kann nur 1x eine Karte gehoben werden. Gib bei der nächsten Eingabe \"weiter\" ein.\n");
        } else {
            drawCardCounter++;
            if (spielKarten.drawPile.size() <= 1) {  // falls der Nachziehstapel weniger/gleich 1 Karten beinhaltet
                makeNewDeckWhenPileIsEmpty();
            }
            // die oberste Karte vom Nachziehstapel wird zu den Handkarten hinzugefügt
            addCardToHandcardDeck();
            // die aktuellen Karten werden angezeigt
            System.out.println("Deine " + playersList.get(indexCurrentPlayer).getHandCardDeck().size() + " aktuellen Karten: " + playersList.get(indexCurrentPlayer).getHandCardDeck());

        }
        return drawCardCounter;
    }

    // Methode, um zu prüfen, ob die gewünschte Karte gespielt werden darf
    private boolean validTurn(UnoKarte currentCard) {

        return validCard(currentCard, stack.lastElement());
    }

    // Prüfung, ob die Karte gültig ist
    public static boolean validCard(UnoKarte currentCard, UnoKarte previousCard) {
        if (currentCard.getFARBE() == Farbe.SCHWARZ)
            return true;
        if (currentCard.getFARBE() == previousCard.getFARBE() || currentCard.getKARTENWERT() == previousCard.getKARTENWERT())
            return true;

        return false;
    }

    // Plus2 prüfen
    public static boolean validPlus2(UnoKarte currentCard, UnoKarte previousCard) {
        if (currentCard.getKARTENWERT() == previousCard.getKARTENWERT())
            return true;
        return false;
    }

    // hier werden die Sonderkarten implementiert
    private void updateState() {

        if (playersList.get(indexCurrentPlayer).getHandCardDeck().size() == 0) {
            return;

        } else {

            if (stack.lastElement().isPlayedAlready() == false) {  // Funktion der Karte wurde noch nicht gespielt

                if (stack.lastElement().getKARTENWERT() == Kartenwert.OUT) {
                    System.out.println("[iNFO] Der nächste Spieler wird übersprungen.");
                    nextPlayer();
                    skip();
                } else if (stack.lastElement().getKARTENWERT() == Kartenwert.RW) {
                    System.out.println("[iNFO] Spielrichtung wird geändert");
                    changeDirection();
                    nextPlayer();
                } else if (stack.lastElement().getKARTENWERT() == Kartenwert.plus2) {
                    nextPlayer();
                    plus2();
                } else if (stack.lastElement().getKARTENWERT() == Kartenwert.WILD) {
                    chooseColor();
                    nextPlayer();
                } else if (stack.lastElement().getKARTENWERT() == Kartenwert.plus4) {
                    chooseColor();
                    if (playersList.get(indexCurrentPlayer).isBot()) {
                        nextPlayer();
                        System.out.println("[iNFO] Der nächste Spieler " + playersList.get(indexCurrentPlayer).getName() + " hebt vier Karten.");
                        drawCardsPlus4();

                    } else {
                        plus4();
                    }
                }
                // falls "normale" Karte gespielt wurde
                else
                    nextPlayer();
            }
            // Funktion der Karte wurde bereits gespielt, dann kommt einfach der nächste Spieler dran
            else
                nextPlayer();
        }
    }

    private void plus4() {
        UnoKarte lastCard = stack.pop();
        UnoKarte secondToLast = stack.pop();

        Spieler currentMinus1Player = playersList.get(indexCurrentPlayer);
        nextPlayer();   // zuerst einen Spieler weiterspringen

        System.out.println("[ACHTUNG] Nächster Spieler " + playersList.get(indexCurrentPlayer).getName() + ": willst du deinen Vorgänger herausfordern?");
        System.out.println("Gib ja oder nein ein:");
        String consoleInput = input.next();
        String userInput = consoleInput.toUpperCase();
        while (!userInput.equals("NEIN") && !userInput.equals("JA")) {
            System.out.println("Falsche Eingabe! Es ist nur \"ja\" oder \"nein\" erlaubt!");
            consoleInput = input.next();
            userInput = consoleInput.toUpperCase();
        }
        if (userInput.equals("NEIN")) {
            System.out.println("[iNFO] Spieler " + playersList.get(indexCurrentPlayer).getName() + " hebt vier Karten.");
            drawCardsPlus4();
            stack.push(secondToLast);
            stack.push(lastCard);
        } else if (userInput.equals("JA")) {
            UnoKarte u = currentMinus1Player.validPlus4Turn(secondToLast.getFARBE(), secondToLast.getKARTENWERT());
            if (u != null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("[BöSE] " + currentMinus1Player.getName() + ", du hast geschummelt und hättest eine Karte legen können!");
                System.out.println("[iNFO] Der Spieler " + currentMinus1Player.getName() + " hebt vier Karten.");
                stack.push(lastCard);
                stack.push(secondToLast);
                if (spielKarten.drawPile.size() <= 4) {  // falls der Nachziehstapel zu wenig Karten beinhaltet
                    makeNewDeckWhenPileIsEmpty();
                }
                for (int i = 0; i < 4; i++) {
                    currentMinus1Player.getHandCardDeck().add(spielKarten.drawPile.remove());  //  4 Karten vom Nachziehstapel hinzufügen
                }
                System.out.println("[iNFO] Spieler " + playersList.get(indexCurrentPlayer).getName() + ": du bist an der Reihe.");
                printState();
                readUserInput();
            } else {
                System.out.println("[iNFO]" + currentMinus1Player.getName() + " hat nicht geschummelt!");
                System.out.println("[iNFO]" + playersList.get(indexCurrentPlayer).getName() + " muss 6 Karten aufnehmen!");
                if (spielKarten.drawPile.size() <= 6) {
                    makeNewDeckWhenPileIsEmpty();
                }
                for (int i = 0; i < 6; i++) {
                    addCardToHandcardDeck();
                }
                stack.push(secondToLast);
                stack.push(lastCard);
                nextPlayer();
            }
        }
    }

    private void drawCardsPlus4() {
        if (spielKarten.drawPile.size() <= 4) {  // falls der Nachziehstapel weniger/gleich 4 Karten beinhaltet
            makeNewDeckWhenPileIsEmpty();
        }
        for (int i = 0; i < 4; i++) {
            addCardToHandcardDeck();
        }
        nextPlayer();
    }

    private void plus2() { //bei Plus2 Karte
        printState();
        Spieler currentPlayer = playersList.get(indexCurrentPlayer);

        if (currentPlayer.isBot()) {
            playAnotherPlus2(currentPlayer);
        } else {
            System.out.println("Hast du eine Plus2 oder hebst du 2 Karten? Dann gib entweder die Karte ein oder \"heben\":");

            String consoleInput = input.next();
            String userInput2 = consoleInput.toUpperCase();

            if (userInput2.equals("HEBEN")) {
                drawCardsPlus2();
            } else
                playAnotherPlus2(currentPlayer);
        }
        nextPlayer();
    }

    private void playAnotherPlus2(Spieler currentPlayer) {
        UnoKarte u = currentPlayer.getFirstPlus2(stack.lastElement());
        if (u != null) {
            System.out.println(currentPlayer.getName() + " legt " + u.toString());
            currentPlayer.getHandCardDeck().remove(u);
            stack.push(u);
            counterPlus2++;
            nextPlayer();
            plus2();
        } else {
            drawCardsPlus2();
        }
    }

    private void drawCardsPlus2() {
        if (spielKarten.drawPile.size() <= (counterPlus2 * 2)) {  // falls der Nachziehstapel weniger/gleich 1 Karten beinhaltet
            makeNewDeckWhenPileIsEmpty();
        }
        for (int i = 0; i < (counterPlus2 * 2); i++) {
            addCardToHandcardDeck();
        }
        System.out.println("[iNFO] Spieler " + playersList.get(indexCurrentPlayer).getName() + " muss " + (counterPlus2 * 2) + " Karten heben.");
        //nextPlayer(); rausgenommen wegen startkarte
        counterPlus2 = 1;
    }

    private void chooseColor() {
        Spieler currentPlayer = playersList.get(indexCurrentPlayer);

        if (currentPlayer.isBot()) {
            System.out.println(currentPlayer.getName() + " hat die Farbe " + colorGenerator() + " ausgewählt.");
            return;
        }

        boolean correctInput = false;

        while (!correctInput) {
            System.out.println("gib deine gewünschte Farbe an: ");
            String farbEingabe = input.next();
            String farbwunsch = farbEingabe.toUpperCase();

            switch (farbwunsch) {
                case "R":
                    farbwunsch = String.valueOf(Farbe.ROT);
                    break;
                case "B":
                    farbwunsch = String.valueOf(Farbe.BLAU);
                    break;
                case "G":
                    farbwunsch = String.valueOf(Farbe.GRÜN);
                    break;
                case "Y":
                    farbwunsch = String.valueOf(Farbe.YELLOW);
                    break;
                default:
                    farbwunsch = null;
                    System.out.println("Falsche Eingabe! Nur R (rot), G (grün), B (blau) und Y (gelb) sind erlaubt.");
                    break;
            }

            if (farbwunsch != null) {
                correctInput = true;

                stack.lastElement().setFARBE(Farbe.valueOf(farbwunsch));
            }
        }
    }

    private void skip() {
        //nextPlayer();
        nextPlayer();
    }

    private void changeDirection() {
        direction *= -1;
    }


    private Spieler nextPlayer() {
        indexCurrentPlayer = indexCurrentPlayer + direction;

        if (direction == 1) {
            if (indexCurrentPlayer == playersList.size()) {
                indexCurrentPlayer = 0;
            }
        } else if (direction == -1) {
            if (indexCurrentPlayer == -1) {
                indexCurrentPlayer = playersList.size() - 1;
            }
        }
        return playersList.get(indexCurrentPlayer);
    }

    // Nachziehstapel ist fast leer --> Ablagestapel neu mischen und zum Nachziehstapel machen
    private void makeNewDeckWhenPileIsEmpty() {

        System.out.println("Anzahl der Karten im Ablagestapel: " + stack.size()); // nur für Testzwecke

        //UnoKarte oK = spielKarten.stack.removeLast();       // oberste Karte wird "auf die Seite gelegt" (nicht mitgemischt)
        UnoKarte oK = stack.pop();       // oberste Karte wird "auf die Seite gelegt" (nicht mitgemischt)

        System.out.println("oberste Karte " + oK.toString());   // nur für Testzwecke
        System.out.println("Anzahl der Karten im Ablagestapel nach Entfernen der obersten Karte: " + stack.size()); // nur für Testzwecke

        for (UnoKarte u : stack) {           // die Funktion playedAlready wieder auf FALSE setzen
            u.setPlayedAlreadyToFalse();
        }
        Collections.shuffle(stack);                     // Ablagestapel wird gemischt

        spielKarten.drawPile.addAll(stack);             // die gemischten Karten werden dem Nachziehstapel zugefügt
        stack.clear();          // Stack leeren
        stack.push(oK);                   // oberste Karte wird wieder aufgelegt
        System.out.println("Oberste Karte, nachdem die letzte Karte wieder aufgelegt wurde: " + stack.lastElement());   // nur für Testzwecke

    }

    private void makeNewDeckWhenRoundEnded() {

        for (Spieler p : playersList) {              // alle übrigen Handkarten werden geleert
            p.getHandCardDeck().clear();
        }
        spielKarten.drawPile.clear();               // Nachziehstapel "leeren"
        stack.clear();                 // Ablagestapel "leeren
        spielKarten.shuffleDeck();
        for (UnoKarte u : spielKarten.drawPile) {     // "Rücksetzen" der farbigen Wilden oder farbigen +4 aus voriger Runde
            if (u.getKARTENWERT() == Kartenwert.plus4 || u.getKARTENWERT() == Kartenwert.WILD) {
                u.setFARBE(Farbe.SCHWARZ);
            }
            u.setPlayedAlreadyToFalse();            // "Rücksetzen" der playedAlready Funktion
        }
    }

    // aktueller Status wird ausgegeben: welcher Spieler ist dran, welche Karte liegt oben, verfügbare Handkarten
    private void printState() {

        System.out.println("........");
        System.out.println("Anzahl Nachziehstapel: " + spielKarten.drawPile.size()); // für testzwecke
        System.out.println("Anzahl Ablagestapel/Stack: " + stack.size());
        System.out.println("oberste Karte am Ablagestapel: " + stack.lastElement().toString());
        Spieler s = playersList.get(indexCurrentPlayer);
        System.out.println("aktueller Spieler: " + s.getName());
        System.out.println("Deine " + playersList.get(indexCurrentPlayer).getHandCardDeck().size() + " aktuellen Karten: " + s.getHandCardDeck());
        System.out.println("........");
    }

    // abbruch der runde, wenn keine karten mehr im handkartenset vorhanden sind
    private boolean roundEnded() throws SQLException {

        if (playersList.get(indexCurrentPlayer).getHandCardDeck().size() != 0) {
            return false;
        } else {
            Spieler winnerRound = playersList.get(indexCurrentPlayer);
            roundCounter++;
            System.out.println("*********************************************************");
            System.out.println(winnerRound.getName() + " hat die " + roundCounter + ". Runde gewonnen und " +
                    getCardPoints() + " Punkte erreicht!!!");
            winnerRound.setPoints(winnerRound.getPoints() + getCardPoints());
            client.executeStatement(String.format(INSERT_TEMPLATE, winnerRound.getName(), sessionCounter,
                    roundCounter, getCardPoints()));
            System.out.println("Eine neue Runde beginnt.");
            for (Spieler s : playersList) {                      // todo: um die punkte zu prüfen
                System.out.println(s.getName() + ": " + s.getPoints() + " Punkte");
            }
            System.out.println("*********************************************************");
            makeNewDeckWhenRoundEnded();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    private int getCardPoints() {
        int playersPoints = 0;
        int winnersPoints = 0;
        for (Spieler s : playersList) {
            for (UnoKarte k : s.getHandCardDeck()) {
                playersPoints += k.getKARTENWERT().cardPoints;
            }
            winnersPoints += playersPoints;
            playersPoints = 0;
        }
        return winnersPoints;
    }

    private boolean gameEnded() {

        if (playersList.get(indexCurrentPlayer).getPoints() < 500)
            return false;
        else {
            System.out.println("*********************************************************");
            System.out.println("Das Spiel ist beendet!");
            System.out.println("Der Gewinner ist: " + playersList.get(indexCurrentPlayer).getName() + "!!!!!!!");
            System.out.println("GRATULATION");

            System.out.println("*********************************************************");
            return true;
        }
    }

    private void printFinalScore() {

        System.out.println("Endpunktestand:");
        System.out.println(":::::::::::::::::::::::::::::::::::::::::::::::::::");
        getPlayersPoints();

        System.out.println();
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhyso+++osyhdhhhhhhhhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhyo:          -/shhhhhhhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhy+..    .ooo.    .:shhhhhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhy:    -/osysso+:.   .+hhhhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhyso/-.shhhhh:    /yddmddhhys+-    /hhhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhys:    -yhhds-   -ydhhhhddmhyho-    shhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhyyhhhhhhhdhs+-    /hddo.   -hhhhhhhhhmmdh/    :hhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhhhhhhhhys+:-.-+shhhhdmys+.    +dds-   .shhhhhhhhhmmd/    -hhh");
        System.out.println("hhhhhhhhhhhhhhhhhhhyso/:shs/-      -+shhdmhy/.   .hhy/   .-yhhhhhhhhdmh-    /hhh");
        System.out.println("hhhhhhhhhhhhhhhhhhy/-   -hso:         -+sdmhh/    -hs+:.    oyhhhhhhhy:     yhhh");
        System.out.println("hhhhhhhhyyhhhhhhhdso:    :hs+:    -.`   `-oyhy:    /so+/    -:/oooo/:     /shhhh");
        System.out.println("hhhhys+:--yhhhhhhmhs+-    oys+-    :/:.    .-+/:    os+++/              /oyhhhhh");
        System.out.println("hhhhs:   ./hhhhhhdmys/.   .yys/.   .+so+/           -yhssso++         /+shhhhhhh");
        System.out.println("hhdhy+-   .+hhhhhhdmys:    -hyy/    -yyysso/          dddhyyyyyssssyhdddhhhhhhhh");
        System.out.println("hhdmhy/-   -shhhhhhddys:    :dys:    :dddhhyyo+/      /hddddddmdmmmmddhhhhhhhhhh");
        System.out.println("hhhdmys:.   -yhhhhhhmdhs:    .odho    -+hdmmdddhyo+    /shhhhdddddhhhhhhhhhhhhhh");
        System.out.println("hhhhddyo:.   :hhhhhhdmdd+     .mhh+    -ohhddmmdddhysyyhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhmhy+:    .+hhhhhhdmd+    .hmdh/    -yhhhhddmmdddddhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhdmhy+:     +yhhhhhds-     hdmdh/:/++ohhhhhhhddhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhdms++:     /+o+/:-     shhmddyyhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhhddoo+/              /+yhhhdmdmmddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhhhddysso//..      //+shhhhhhhddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhhhhdmdddhhyyyyyhhdmdhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhhhhhhdhhhhhhhhhhddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");
        System.out.println("hhhhhhhhhhhhhhhdddhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhh");

    }
}