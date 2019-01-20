# OSM-Project

Getestet mit Ubuntu 18 VM 15,7GB RAM

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
    mvn exec:java -Dexec.mainClass="julianhoerz.App" -Dexec.args="stuttgart.pbf"


Neues Terminal öffnen (Terminal 2):
    cd osm
    git clone https://github.com/julianhoerz/angulartest.git
    cd angulartest
    npm install
    npm start


Visualisierung:
Öffne Firefox: http://localhost:4200
Klick auf "Navigation"
Klick auf "











