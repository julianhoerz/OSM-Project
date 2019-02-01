# OSM-Project

Getestet mit Ubuntu 18 VM 15,7GB RAM
Im Folgenden sind alle Befehle eingerückt. 
Der letzte Commit der verwendeten Repos war vor der Deadline. 

Installation:

    sudo apt-get update
    sudo apt-get upgrade

    sudo apt-get install git
    sudo apt-get install nodejs 
    sudo apt-get install npm 
    sudo apt-get install default-jdk
    sudo apt-get install maven

    mkdir osm





Neues Terminal öffnen (Terminal 1):
    cd osm
    git clone https://github.com/julianhoerz/OSM-Project.git
    cd OSM-Project
    mvn compile
    set MAVEN_OPTS=-Xmx14G

Kopiere ein PBF File in den Ordner (z.B.: germany.pbf).
Das PBF File von Bremen ist im Ordner schon vorhanden.

    mvn exec:java -Dexec.mainClass="julianhoerz.App" -Dexec.args="bremen.pbf"
[oder: mvn exec:java -Dexec.mainClass="julianhoerz.App" -Dexec.args="germany.pbf"]

Wenn alles klappt startet das Programm und gibt "Starting Program..." aus.
Die PBF Datei ist vollständig eingelesen, wenn die Konsole "Pbf File Reader finished." ausgibt. 






Neues Terminal öffnen (Terminal 2):
    cd osm
    git clone https://github.com/julianhoerz/angulartest.git
    cd angulartest
    npm install
Alle Warnungen ignorieren, die Installation braucht ein paar Minuten ;-)
    npm start





Tabelle:
Die Anzahl der Knoten und Kantentypen kann unter folgender Adresse angesehen werden:
http://localhost:3000/api





Visualisierung (Pfadplanung):
Öffne Firefox: http://localhost:4200
Klick auf "Navigation"
Klick auf "Start" und dann einen Punkt auf der Karte anklicken. (Der Punkt sollte natürlich vom PBF-File abgedeckt werden)
Klick auf "Ziel" und dann einen Punkt auf der Karte anklicken.
Wenn Start und Ziel ausgewählt wurden auf "Berechne Route" klicken.
Beim Deutschlandgraph kann die Berechnung bis zu 15 Sekunden dauern (wenn der Pfad quer durch Deutschland geht).



Bei Fragen: 
julianhoerz@gmail.com











