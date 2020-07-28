# **UNO Spiel in Java by Britta, Lena, Leo und Aiste a.k.a. Gruppe 4.**

Das altbekannte UNO Spiel programmiert in Java für
 maximal 4 menschliche Spieler oder 4 Bots mit implementierter
 Datenbank.  

# **_Strukturaufbau_:** 

zuerst haben wir alle _Anforderungen_ abgeklärt, es sind 41 geworden. 

Dann haben wir die Karten implementiert und angefangen _Kartendecks_ zu erstellen:
 Nachziehstapel, Ablagestapel, Spielerhandkarten. Da war es wichtig eine passende Datenstruktur
 zu wählen, wir haben uns für ein ArrayList zum Erstellen für das gesamte Kartendeck entschieden,  
der Nachziehstapel ist eine LinkedList(Queue), da sie gut geeignet ist, um Elemente zu Löschen oder einzufügen.
 Ablagestapel ist ein Stack weil wir nur
 auf die letzte Karte zugreifen müssen (außer beim herausfordern des +4 Legers s.P#5).
 Spielerhandkarten sind in einer LinkedList gespeichert. 
 
 Eine _Spielerliste_ (ArrayList) für 4 Spieler erstellt und per Zufallsgenerator "Random" ein Spieler 
 als Startspieler ausgesucht.
 
 Wir sind mit 9 Klassen ausgekommen und unsere Hauptmethoden haben wir in der 
 _App Klasse_ verpackt. 
 
 In der ReadUserInput() Methode haben wir den eigentlichen Spielablauf für den menschlichen Spieler 
 implementiert: Benutzereingabe wird eingelesen, Hilfestellung angeboten, Korrekte Eingabe
  abgeglichen. 
 
 In der updateState() Methode haben wir die Sonderkarten und Sonderfunktionen implementiert.
 
 Wenn die Karten neu gemischt werden, wird die playedAlready() (s.P#6) Funktion wieder auf false gesetzt. 
 
 makeNewDeck(): die davor gewählten Farben bei WILD und +4 werden wieder auf Schwarz gesetzt. 
 
 Die Runde endet, wenn ein Spieler keine Handkarten mehr hat. 
 
 Das Spiel endet, wenn ein Spieler 500 Punkte gesammelt hat. 
 
 Viel Spaß! 
    
 

# _**Probleme & Bugs:**_

1. Die passenden Datenstrukturen für Listen zu wählen. 
2. User input durch den Separator zu spliten. 
3. Die verschiedenen Fehlermöglichkeiten bei der Karten-Eingabe zu "lokalisieren" --> welche Fehlerausgabe soll bei welcher Eingabe passieren
4. Arbeit aufteilen, Kolleginnen den Code und die Logik zu erklären. 
 Änderungen vornehmen und zusammenfügen. 
5. Bei +4 Herausforderung auf die vorletzte Karte im Stack zugreifen. 
6. Aktionskarte tritt mehrmals in Aktion --> setPlayedAlready() zu vermeiden, dass
einmal gelegte Aktionskarte nicht mehrmals in Aktion tritt. 
7. Richtungswechsel hat Anfangs nur einmal die Richtung gewechselt und ist dann zurückgesprungen.
 Mit Aufruf von changeDirection() haben wir Variable direction mit -1 multipliziert 
 um die Richtung zu verändern.
8. NextPlayer() mit +1 oder -1 implementiert. 
9. Als bei den Bots der Nachziestapel leer war, hat das Spiel nicht mehr ausgeteilt. 
10. Beim neuen Stapel machen mussten wir alle Karten(Aktionskarten) in ihrem Wert zurücksetzen. (Grün "wild") 
11. Plus2 auf Plus2 legen eine Rekursion implementieren. 
12. Beim "UNO" rufen das Array von 2 auf 3 Elemente erweitern müssen und damit es funktioniert, 
haben wir in der Exception "catch" Block ausführbaren Code eingebaut. 
13. GITHUB: Zusammenfügen der verschiedenen Versionen (aktualisierte) und nicht am Master zu arbeiten.

# **Was wir jetzt anders machen würden**
* Der Austausch mit Kolleginnen anderer Gruppen durch Onlinelehre hat massiv gefehlt. 
* Wir würden uns gleich in Person treffen und Anforderungen priorisieren. 
* Das Projekt besser strukturieren, weil wir jetzt wissen, was anfangs wichtig ist und was nicht. 
* Die Methoden aus der App Klasse in andere Klassen auslagern. 


# **Highlights** 
* Daily Scrums
* Tatsächliches Spielen: das erste mal z.B. r-3 eingeben und sehen, dass es funktioniert. 
* Pair- & Groupprogramming Erkenntnisse.
* Gemeinsames Brainstorming.
* Product Owner Unterstützung.  

