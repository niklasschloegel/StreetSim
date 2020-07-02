package streetsim.business;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import streetsim.business.abschnitte.TStueck;
import streetsim.business.exceptions.DateiParseException;
import streetsim.business.exceptions.FalschRotiertException;
import streetsim.business.exceptions.SchonBelegtException;
import streetsim.business.exceptions.WeltLeerException;
import streetsim.data.DatenService;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableMap;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Verwaltung aller Strassenabschnitte und Autos sowie Großteil
 * der Anwendungslogik und Schnittstelle für obere Schicht (UI)
 */
public class Strassennetz implements Serializable {

    private ObservableMap<Position, Strassenabschnitt> abschnitte;
    private ObservableMap<Position, ArrayList<Auto>> autos;
    private BooleanProperty simuliert;
    private String name;
    private DatenService datenService;
    private transient String test;

    public Strassennetz() {
        abschnitte = FXCollections.observableHashMap();
        autos = FXCollections.observableHashMap();
        simuliert = new SimpleBooleanProperty();
    }

    /**
     * steht das Auto an einem Strassenabschnitt, an dem es in eine andere Richtung abbiegen kann
     * zufällige Bestimmung der möglichen Abbiegerichtung
     *
     * @param a Auto
     * @return Strassenabschnitt
     */
    public Optional<Strassenabschnitt> stehtAnKreuzung(Auto a) {
        // TODO: Lösungsansatz Distanz vom Auto zum Mittelpunkt (ACHTUNG: Referenzpunkt Auto konsistent)
        return null;
    }

    /**
     *
     * @param a Auto
     * @param s Strassenabschnitt
     * @return steht das Ampel an einer Ampel oder nicht
     */
    public boolean stehtAnAmpel(Auto a, Strassenabschnitt s) {
        // TODO: Lösungsansatz stehtAnKruezug() aufrufen und wenn ja Ampel überprüfen (ACHTUNG: Referenzpunkt Auto konsistent)
        return false;
    }

    /**
     * fügt ein Auto zum Strassennetz hinzu (Autos-Map)
     *
     * @param a Auto
     * @throws SchonBelegtException wenn ein Auto auf dem Strassennetz mit selber Position und Richtung existiert
     */
    public void autoAdden(Auto a) throws SchonBelegtException {
        Position p = new Position(a.getPositionX(), a.getPositionY());
        // TODO: kein Strassenabschnitt an der Stelle (? Exception)
        if (abschnitte.containsKey(p)) {
            if (!autos.containsKey(p)) {
                autos.put(p,new ArrayList());
            }
            if (posBelegt(a)) {
                throw new SchonBelegtException();
            } else {
                autos.get(p).add(a);
            }
        }
    }

    /**
     * fügt ein Strassenabschnitt zum Strassennetz hinzu (Abschnitte-Map)
     *
     * @param s Strassenabschnitt
     * @throws SchonBelegtException an der Position ist bereits ein anderer Strassenabschnitt platziert
     * @throws FalschRotiertException kein Strassenfluss möglich
     */
    public void strasseAdden(Strassenabschnitt s) throws SchonBelegtException, FalschRotiertException {
        // TODO: ? FalschRotiertException unnötig
        Position p = new Position(s.getPositionX(), s.getPositionY());
        if (abschnitte.containsKey(p)) {
            throw new SchonBelegtException();
        } else {
            abschnitte.put(p,s);
        }
    }

    /**
     * Überprüfung der Position des Autos
     *
     * @param a Auto
     * @return schon belegt oder nicht
     */
    public boolean posBelegt(Auto a) {
        Position p = new Position(a.getPositionX(), a.getPositionY());
        for (Auto brum: autos.get(p)) {
            if (a.getRectangle().intersects(brum.getRectangle().getLayoutBounds())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Überprüfung der Position des Strassenabschnitts
     *
     * @param s Strassenabschnitt
     * @return schon belegt oder nicht
     */
    public boolean posBelegt(Strassenabschnitt s) {
        Position p = new Position(s.getPositionX(), s.getPositionY());
        return abschnitte.containsKey(p);
    }

    /**
     * Überprüfung einer freien Position
     *
     * @param x X-Koordinate
     * @param y y-Koordinate
     * @return schon belegt oder nicht
     */
    public boolean posBelegt(int x, int y) {
        // TODO: macht keinen Sinn (nicht aussagekräftiger Rückgabewert)
        return false;
    }

    /**
     * entfernt beliebig viele Autos vom Strassennetz
     *
     * @param a Autos
     */
    public void entfAuto(Auto ... a) {
        for (Auto brum: a) {
            Position p = new Position(brum.getPositionX(), brum.getPositionY());
            autos.get(p).remove(brum);
        }
    }

    /**
     * aktiviert Ampeln an gegebenen Strassenabschnitt
     * automatisches Schalten von Ampeln wird aktiviert
     *
     * @param s Strassenabschnitt
     */
    public void ampelnAktivieren(Strassenabschnitt s) {
        s.ampelnAktivieren();
    }

    /**
     * deaktiviert Ampeln an gegeben Strassenabschnitt
     *
     * @param s Strassenabschnitt
     */
    public void ampelnDeaktivieren(Strassenabschnitt s) {
        s.setAmpelAktiv(false);
    }

    /**
     * speichert aktuelles Strassennetz im Dateisystem
     *
     * @throws WeltLeerException keine Attribute auf Strassennetz gesetzt
     */
    public void speicherNetz() throws WeltLeerException {
        // TODO: speichern
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("StreetSim - Strassennetz speichern");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rueckgabewert = chooser.showSaveDialog(null);
        File file = chooser.getSelectedFile();
        if (rueckgabewert == JFileChooser.APPROVE_OPTION) {
            name = file.getName();
            ObjectMapper mapper = new ObjectMapper();
            try {
                // TODO: Rekursion unterbinden
                String jsonResult = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
                System.out.println(jsonResult);
                FileWriter f = new FileWriter(file.getPath() + ".txt");
                f.write(jsonResult);
                f.flush();
                f.close();
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * versucht ein Strassennetz aus einer Datei zu laden
     *
     * @throws DateiParseException Datei konnte nicht gelesen werden
     */
    public static Strassennetz ladeNetz() throws DateiParseException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("StreetSim - Strassennetz auswählen");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int rueckgabewert = chooser.showOpenDialog(null);
        File file = chooser.getSelectedFile();
        if (rueckgabewert == JFileChooser.APPROVE_OPTION) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                // TODO: dezerialise Position
                return mapper.readValue(Files.readString(Path.of(file.getPath())), Strassennetz.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new DateiParseException();
        }
        return null;
    }

    /**
     * versucht den Strassenabschnitt um 90 Grad im Uhrzeigersinn zu rotieren
     *
     * @param s Strassenabschnitt
     * @throws FalschRotiertException keine Verknüpfung zu einem anderen Strassenabschnitt
     */
    public void rotiereStrasse(Strassenabschnitt s) throws FalschRotiertException {
        // TODO: FlaschRotiertException sinvoll? (wie überprüfen, beschränkt Benutzer, lose Enden = Sackgasse)
        s.rotiere();
    }

    /**
     * entfernt beliebig viele Strassenabschnitte
     * entfernt ebenfalls sich darauf befindlichen Autos
     *
     * @param s Strassenabschnitte
     */
    public void entfStrasse(Strassenabschnitt ... s) {
        for (Strassenabschnitt stra: s) {
            Position p = new Position(stra.getPositionX(), stra.getPositionY());
            abschnitte.remove(p);
        }
    }

    /**
     * versucht Strassenabschnitt zu verschieben
     * eventuell darauf befindliche Autos werden mit verschoben
     *
     * @param s Strassenabschnitt
     * @param x X-Koordinate
     * @param y Y-Koordinate
     */
    public void bewegeStrasse(Strassenabschnitt s, int x, int y) {
        Position oldP = new Position(s.getPositionX(), s.getPositionY());
        Position newP = new Position(x,y);
        int xOff = newP.getPositionX() - oldP.getPositionX();
        int yOff = newP.getPositionY() - oldP.getPositionY();
        for (Auto a: autos.get(oldP)) {
            a.setPositionX(a.getPositionX() + xOff);
            a.setPositionY(a.getPositionY() + yOff);
        }
        autos.put(newP,autos.remove(oldP));
        entfStrasse(s);
        abschnitte.put(newP,s);
    }

    /**
     * passt die Geschwindigkeit eines Autos an
     *
     * @param a Auto
     * @param geschwindigkeit Geschwindigkeit (Intervall zwischen 0 und 1)
     */
    public void geschwindigkeitAnpassen(Auto a, float geschwindigkeit) {
        a.setGeschwindigkeit(geschwindigkeit);
    }

    /**
     * entfernt alle Autos vom Strassennetz
     */
    public void entfAlleAutos() {
        autos.clear();
    }

    /**
     * entfernt alle Strassen vom Strassennetz
     */
    public void entfAlleStrassen() {
        entfAlleAutos();
        abschnitte.clear();
    }

    // TODO Ändernung von entfAlleAmpeln zu alleAmpelnDeaktivieren
    /**
     * deaktiviert alle Ampeln vom Strassennetz
     */
    public void alleAmpelnDeaktivieren() {
        for (Map.Entry<Position,Strassenabschnitt> entry: abschnitte.entrySet()) {
            ampelnDeaktivieren(entry.getValue());
        }
    }

    /**
     * setzt die geladene Welt in den Ausgangszustand
     */
    public void reset() {
        // TODO unnötig? (gleich mit entfAlleStrassen)
    }

    /**
     * Starten der Simulation
     *
     * @throws WeltLeerException keine Attribute auf Strassennetz gesetzt
     */
    public void starteSimulation() throws WeltLeerException {
        if (abschnitte.isEmpty()) {
            throw new WeltLeerException();
        }
        simuliert.setValue(true);
        // TODO: Zeit-Intervall festlegen
        // Ampelschaltung
        int millisek = 10000;
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (simuliert.get()) {
                    for (Strassenabschnitt s : abschnitte.values()) {
                        if (s.isAmpelAktiv()) {
                            s.schalte();
                        }
                    }
                    try {
                        Thread.sleep(millisek);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Thread.currentThread().interrupt();
                    alleAmpelnDeaktivieren();
                }

            }
        }).start();
        // Autos fahren
        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (simuliert.get()) {
                    for (Map.Entry<Position, ArrayList<Auto>> entry : autos.entrySet()) {
                        for (Auto a : entry.getValue()) {
                            a.fahre();
                            // TODO: wann wird auto in andere Liste verschoben?
                        }
                    }
                } else {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    /**
     * Pausieren der Simulation
     */
    public void pausiereSimulation() {
        simuliert.setValue(false);
    }

    public boolean isSimuliert() {
        return simuliert.get();
    }

    public BooleanProperty simuliertProperty() {
        return simuliert;
    }

    public void setSimuliert(boolean simuliert) {
        this.simuliert.set(simuliert);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ObservableMap<Position, Strassenabschnitt> getAbschnitte() {
        return abschnitte;
    }

    public ObservableMap<Position, ArrayList<Auto>> getAutos() {
        return autos;
    }

    public static void main(String[] args) {
        Strassennetz s = new Strassennetz();
        Strassenabschnitt str = new TStueck(100,100,800);
        s.strasseAdden(str);
        //Auto brumbrum = new Auto(0.7f, Himmelsrichtung.NORDEN,100,100,20,30,"blau",s);
        Auto brum = new Auto(0.9f, Himmelsrichtung.WESTEN,100,100,10,20,"geln",s);
        //s.autoAdden(brumbrum);
        //s.autoAdden(brum);
        s.speicherNetz();
        Strassennetz.ladeNetz();
    }

}


