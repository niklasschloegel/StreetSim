package streetsim.business;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.Rectangle;

import java.util.*;

/**
 * In eine Himmelsrichtung sich fortbewegendes Objekt
 * welches auf das Strassennetz, inbesondere Ampeln, reagiert
 */
public class Auto {

    public enum AutoModell {ROT, POLIZEI, BLAU}

    private int geschwindigkeit;
    private static final int MAXGESCHWINDIGKEIT = 8;
    private static final int OFFSET = 2;
    private SimpleObjectProperty<Himmelsrichtung> richtung;

    private Queue<Wendepunkt> wendepunkte;

    private SimpleIntegerProperty positionX;
    private SimpleIntegerProperty positionY;
    private int breite;
    private int laenge;
    private AutoModell autoModell;
    private final transient Strassennetz strassennetz;
    private transient Rectangle rectangle;

    public Auto(int positionX, int positionY, AutoModell autoModell) {
        setGeschwindigkeit(0.5f);
        this.positionX = new SimpleIntegerProperty(positionX);
        this.positionY = new SimpleIntegerProperty(positionY);
        this.breite = 32;
        this.laenge = 32;
        this.strassennetz = Strassennetz.getInstance();
        this.autoModell = autoModell;
        this.wendepunkte = new LinkedList<>();
        richtung = new SimpleObjectProperty<>();
        initRectangle();
        positionierung();
    }

    /**
     * Positionierung und Ausrichtung des Autos
     * an nächstgelegener Stelle des Strassenabschnittes
     */
    public void positionierung() {
        // Distanz der oberen linken Ecke zum Mittelpunkt
        int offX = (this.positionX.get() % Strassenabschnitt.GROESSE) - Strassenabschnitt.GROESSE / 2;
        int offY = (this.positionY.get() % Strassenabschnitt.GROESSE) - Strassenabschnitt.GROESSE / 2;
        // Wert welcher näher zur Mitte liegt wird angepasst
        if (Math.abs(offX) < Math.abs(offY)) {
            // Trennung nach Richtung aus welcher der Punkt angepasst wird (rechts/links)
            if (offX < 0) {
                this.richtung.set(Himmelsrichtung.SUEDEN);
                this.positionX.set(this.positionX.get() - (this.positionX.get() % Strassenabschnitt.GROESSE) + ((Strassenabschnitt.GROESSE - (this.breite + OFFSET)) / 2));
            } else {
                this.richtung.set(Himmelsrichtung.NORDEN);
                this.positionX.set(this.positionX.get() - (this.positionX.get() % Strassenabschnitt.GROESSE) + ((Strassenabschnitt.GROESSE + (this.breite + OFFSET)) / 2));
            }
            if (Math.abs(offY) < Strassenabschnitt.HALTELINIENABSTAND) {
                this.positionY.set(Integer.signum(offY) * (Strassenabschnitt.HALTELINIENABSTAND + OFFSET) + this.positionY.get() - (this.positionY.get() % Strassenabschnitt.GROESSE) + (Strassenabschnitt.GROESSE / 2));
            }
        } else {
            // Trennung nach Richtung aus welcher der Punkt angepasst wird (oben/unten)
            if (offY < 0) {
                this.richtung.set(Himmelsrichtung.WESTEN);
                this.positionY.set(this.positionY.get() - (this.positionY.get() % Strassenabschnitt.GROESSE) + ((Strassenabschnitt.GROESSE - (this.breite + OFFSET)) / 2));
            } else {
                this.richtung.set(Himmelsrichtung.OSTEN);
                this.positionY.set(this.positionY.get() - (this.positionY.get() % Strassenabschnitt.GROESSE) + ((Strassenabschnitt.GROESSE + (this.breite + OFFSET)) / 2));
            }
            if (Math.abs(offX) < Strassenabschnitt.HALTELINIENABSTAND) {
                this.positionX.set(Integer.signum(offX) * (Strassenabschnitt.HALTELINIENABSTAND + OFFSET) + this.positionX.get() - (this.positionX.get() % Strassenabschnitt.GROESSE) + (Strassenabschnitt.GROESSE / 2));
            }
        }
        if (!positionAufStrasse()) {
            this.rotiere();
            positionierung();
        }
    }

    /**
     * Überpürft ob das Auto sich nach der Positionierung
     * auf der Straße befindet
     *
     * @return
     */
    private boolean positionAufStrasse() {
        Position p = new Position(this.getPositionX(), this.getPositionY());
        Strassenabschnitt s = Strassennetz.getInstance().getAbschnitte().get(p);
        int kleinerAls = Strassenabschnitt.GROESSE / 2 - Strassenabschnitt.HALTELINIENABSTAND;
        int groesserAls = Strassenabschnitt.GROESSE / 2 + Strassenabschnitt.HALTELINIENABSTAND;
        if (this.positionY.get() % Strassenabschnitt.GROESSE <= kleinerAls && !s.getRichtungen().contains(Himmelsrichtung.NORDEN)) {
            // im Norden stehen, aber keine Strasse
            return false;
        } else if (this.positionY.get() % Strassenabschnitt.GROESSE >= groesserAls && !s.getRichtungen().contains(Himmelsrichtung.SUEDEN)) {
            // im Süden stehen, aber keine Strasse
            return false;
        } else if (this.positionX.get() % Strassenabschnitt.GROESSE <= kleinerAls && !s.getRichtungen().contains(Himmelsrichtung.WESTEN)) {
            // im Westen stehen, aber keine Strasse
            return false;
        } else if (this.positionX.get() % Strassenabschnitt.GROESSE >= groesserAls && !s.getRichtungen().contains(Himmelsrichtung.OSTEN)) {
            // im Osten stehen, aber keine Strasse
            return false;
        }
        return true;
    }

    /**
     * Abbilden der Koordinaten des Autos auf ein Rechteck
     * (für die Kollisionserkennung)
     */
    private void initRectangle(){
        rectangle = new Rectangle(positionX.doubleValue() - (breite / 2),positionY.doubleValue() - (laenge / 2), breite, laenge);
        // automatische Anpassung an die Position
        rectangle.xProperty().bind(Bindings.subtract(this.positionX, breite / 2));
        rectangle.yProperty().bind(Bindings.subtract(this.positionY, laenge / 2));
    }

    /**
     * eigenständiges Fahren des Autos
     * <p>
     * Überprüfung auf eventuell eintretende Kollision
     * Überprüfung und Berücksichtigung der aktuellen Ampelphase
     * Berücksichtigung der Verkehrsregeln (rechts vor links)
     * Überprüfung auf Fortführung des Strassenabschnittes (Einleitung eines U-Turns)
     * Fahren unter Berücksichtigung möglicher zuvor ermittelter Wendepuntke (Abbiegen, U-Turn)
     * mögliches Versetzen des Autos in neuen Strassenabschnitt (Handover)
     */
    public void fahre() {
        Position p = new Position(positionX.get(), positionY.get());
        Strassenabschnitt aktuellerAbschnitt = strassennetz.getAbschnitte().get(p);
        int mittelpunktX = aktuellerAbschnitt.getPositionX() + aktuellerAbschnitt.getGroesse() / 2;
        int mittelpunktY = aktuellerAbschnitt.getPositionY() + aktuellerAbschnitt.getGroesse() / 2;

        Wendepunkt neuePosition = positionAktualisieren();

        Rectangle newR = new Rectangle(neuePosition.x - breite/2, neuePosition.y - laenge/2, breite, laenge);
        // Kollisions-Überprüfung (wenn fahren Kollision hervorruft stoppt das Auto)
        if (autoKollision(p, newR)) {
            return;
        }

        // Kreuzungs- und Ampel-Überprüfung
        if (strassennetz.stehtAnKreuzung(this)) {
            // Neue Richtung bestimmen
            List<Himmelsrichtung> r = new ArrayList<>(aktuellerAbschnitt.getRichtungen());
            r.remove(this.getRichtung().gegenueber());
            Himmelsrichtung abbiegerichtung = r.get(new Random().nextInt(r.size()));
            // nur Wendepunkt bestimmen wenn es noch keinen gibt
            if (this.wendepunkte.size() == 0) {
                if (abbiegerichtung.equals(this.richtung.get().naechstes())) {
                    // Rechtsabbieger
                    int x = mittelpunktX + ((this.richtung.get().gegenueber().getX() + this.richtung.get().naechstes().getX()) * ((this.breite / 2) + OFFSET));
                    int y = mittelpunktY + ((this.richtung.get().gegenueber().getY() + this.richtung.get().naechstes().getY()) * ((this.laenge / 2) + OFFSET));
                    Wendepunkt w = new Wendepunkt(x, y, abbiegerichtung);
                    wendepunkte.add(w);
                } else if (abbiegerichtung.naechstes().equals(this.richtung.get())) {
                    // Linksabbieger
                    int x = mittelpunktX + ((this.richtung.get().getX() + this.richtung.get().naechstes().getX()) * ((this.breite / 2) + OFFSET));
                    int y = mittelpunktY + ((this.richtung.get().getY() + this.richtung.get().naechstes().getY()) * ((this.laenge / 2) + OFFSET));
                    Wendepunkt w = new Wendepunkt(x, y, abbiegerichtung);
                    wendepunkte.add(w);
                }
            }
            if (aktuellerAbschnitt.isAmpelAktiv()) {
                if (Strassennetz.getInstance().stehtAnAmpel(this, aktuellerAbschnitt)) {
                    return;
                } else if (abbiegerichtung.equals(richtung.get().naechstes())) {
                    // links abbiegen
                    if (kreuzungBlockiert(p,mittelpunktX,mittelpunktY,List.of(richtung.get().gegenueber()))) {
                        return;
                    }
                }
            } else {
                // Abbiegerichtung beachten
                // Rechtsabbieger dürfen immer fahren
                if (!abbiegerichtung.equals(richtung.get().naechstes())) {
                    if (abbiegerichtung.equals(richtung.get())) {
                        // gerade fahren
                        Himmelsrichtung rechtsVonUns = richtung.get().vorheriges();
                        if (kreuzungBlockiert(p, mittelpunktX, mittelpunktY, List.of(rechtsVonUns))) {
                            return;
                        }
                    } else {
                        // links abbiegen
                        Himmelsrichtung rechtsVonUns = richtung.get().vorheriges();
                        Himmelsrichtung gegenueberVonuns = richtung.get().gegenueber();
                        if (kreuzungBlockiert(p, mittelpunktX, mittelpunktY, List.of(rechtsVonUns, gegenueberVonuns))) {
                            return;
                        }
                    }
                }
            }
        }
        // U-Turn
        uTurn(mittelpunktX, mittelpunktY, p);

        // fahren und Wendepunkte dabei beachten
        //positionAktualisieren();
        if(this.getRichtung() != neuePosition.richtung && !wendepunkte.isEmpty() && neuePosition.richtung == wendepunkte.peek().richtung){
            wendepunkte.remove();
        }
        this.richtung.set(neuePosition.richtung);
        this.positionX.set(neuePosition.x);
        this.positionY.set(neuePosition.y);

        // Auto in neuen Strassenabschnitt verlegen
        pruefeHandover(p);
    }

    /**
     * Prüft ob ein U-Turn notwendig ist (Keine Weiterfahrt möglich) und setzt entsprechend Wendepunkte
     * @param mittelpunktX x-Mittelpunkt Koordinate des aktuellen Straßenabschnitts
     * @param mittelpunktY y-Mittelpunkt Koordinate des aktuellen Straßenabschnitts
     * @param p Positionsobjekt des aktuellen Straßenabschnitts
     */
    private void uTurn(int mittelpunktX, int mittelpunktY, Position p){
        int distanzBisMitte = this.distanzBisMitte(mittelpunktX, mittelpunktY);
        if (distanzBisMitte < -(Strassenabschnitt.HALTELINIENABSTAND - MAXGESCHWINDIGKEIT) && this.wendepunkte.size() == 0) {
            Position naechsterAbschnitt = new Position(p.getPositionX() + this.richtung.get().getX() * Strassenabschnitt.GROESSE, p.getPositionY() + this.richtung.get().getY() * Strassenabschnitt.GROESSE);
            if (!(Strassennetz.getInstance().getAbschnitte().containsKey(naechsterAbschnitt) && Strassennetz.getInstance().getAbschnitte().get(naechsterAbschnitt).getRichtungen().contains(this.getRichtung().gegenueber()))) {
                // Distanz des Wendepunkts vom Mittelpunkt
                int wendepunktDistanz = Strassenabschnitt.HALTELINIENABSTAND + MAXGESCHWINDIGKEIT;
                int basisX = mittelpunktX + richtung.get().getX() * wendepunktDistanz;
                int basisY = mittelpunktY + richtung.get().getY() * wendepunktDistanz;
                int w1x = basisX + (richtung.get().naechstes().getX() * ((breite / 2) + OFFSET));
                int w1y = basisY + (richtung.get().naechstes().getY() * ((breite / 2) + OFFSET));
                int w2x = basisX + (richtung.get().vorheriges().getX() * ((breite / 2) + OFFSET));
                int w2y = basisY + (richtung.get().vorheriges().getY() * ((breite / 2) + OFFSET));
                wendepunkte.add(new Wendepunkt(w1x, w1y, richtung.get().vorheriges()));
                wendepunkte.add(new Wendepunkt(w2x, w2y, richtung.get().gegenueber()));
            }
        }
    }

    /**
     * Aktualisiert Position des Autos unter Berücksichtigung der Wendepunkte und Geschwindigkeit
     */
    private Wendepunkt positionAktualisieren(){
        int distanz = (wendepunkte.size() > 0) ? wendepunkte.peek().distanzBisWendepunkt(this.positionX.get(), this.positionY.get()) : 0;
        Wendepunkt ret = new Wendepunkt(0, 0, this.getRichtung());
        if (wendepunkte.size() > 0 && distanz <= geschwindigkeit) {
            Wendepunkt w = wendepunkte.peek();
            ret.richtung = w.richtung;
            ret.x = w.getX() + w.getRichtung().getX() * (geschwindigkeit - distanz);
            ret.y = w.getY() + w.getRichtung().getY() * (geschwindigkeit - distanz);
        } else {
            ret.x = this.positionX.get() + (this.richtung.get().getX() * geschwindigkeit);
            ret.y = this.positionY.get() + (this.richtung.get().getY() * geschwindigkeit);
        }
        return ret;
    }

    /**
     * Prüft ob sich das Auto nach dem fahren in neuem Abschnitt befindet und verlegt es bei Bedarf in andere Liste
     * @param alt die Alte Position des Autos
     */
    private void pruefeHandover(Position alt){
        Position neu = new Position(positionX.get(), positionY.get());
        if (!neu.equals(alt)) {
            strassennetz.getAutos().get(alt).remove(this);
            strassennetz.getAutos().computeIfAbsent(neu, k -> new ArrayList<>());
            strassennetz.getAutos().get(neu).add(this);
        }
    }

    /**
     * Berechnung der Distanz bis zum Mittelpunkt
     * unter Berücksichtigung der Bewegungsrichtung
     * (Rechtsverkehr Offset wird vernachlässigt)
     *
     * @param mittelpunktX Mittelpunkt-Koordinate-X des aktuellen Abschnitts
     * @param mittelpunktY Mittelpunkt-Koordinate-Y des aktuellen Abschnitts
     * @return Distanz
     */
    public int distanzBisMitte(int mittelpunktX, int mittelpunktY) {
        return this.getRichtung().getX() * (mittelpunktX - this.getPositionX()) + this.getRichtung().getY() * (mittelpunktY - this.getPositionY());
    }

    /**
     * Überprüfung ob Autos eines Abschnittes
     * (fahren in angegebener Himmelsrichtung)
     * sich im relevanten Bereich zum Abbiegen
     * des Abschnittes befinden
     *
     * @param p            Position des Strassenabschnitts
     * @param mittelpunktX Mittelpunkt-Koordinate-X des aktuellen Abschnitts
     * @param mittelpunktY Mittelpunkt-Koordinate-Y des aktuellen Abschnitts
     * @param h            Liste von zu beachtenden Himmelsrichtungen
     * @return Kreuzung (Bereich zum Abbiegen) ist blockiert
     */
    public boolean kreuzungBlockiert(Position p, int mittelpunktX, int mittelpunktY, List<Himmelsrichtung> h) {
        if (Strassennetz.getInstance().getAbschnitte().get(p).getRichtungen().size() <= 2) {
            return false;
        }
        for (Auto a: strassennetz.getAutos().get(p)) {
            //if (h.contains(a.getRichtung())) {
            // Bereichsprüfung
            int distanzBisMitte = a.distanzBisMitte(mittelpunktX, mittelpunktY);
            // TODO -6 anpassen
            if (a.getRichtung() != this.getRichtung() && distanzBisMitte < Strassenabschnitt.HALTELINIENABSTAND - 6 && distanzBisMitte > -(Strassenabschnitt.HALTELINIENABSTAND - 6)) {
                return true;
            }
            //}
        }
        return false;
    }

    /**
     * Überprüfung der zu fahrenden Strecke
     * auf Kollision mit anderem Auto
     *
     * @param p    Position des Strassenabschnitts
     * @param newR Rechteck des Autos auf künftiger Position
     * @return ob nächste Position des Autos verfügbar ist (ohne Kollision mit anderem Auto)
     */
    public boolean autoKollision(Position p, Rectangle newR) {
        List<Auto> brums = new ArrayList<>(strassennetz.getAutos().get(p));
        brums.remove(this);
        int vorneX = positionX.get() + richtung.get().getX() * laenge;
        int vorneY = positionY.get() + richtung.get().getY() * laenge;
        Position vorneP = new Position(vorneX, vorneY);
        if(strassennetz.getAutos().containsKey(vorneP)){
            brums.addAll(strassennetz.getAutos().get(vorneP));
        }
        for (Auto a : brums) {
            if (newR.intersects(a.getRectangle().getBoundsInLocal()) && !this.equals(a)) {
                return true;
            }
        }
        return false;
    }

    /**
     * rotiert ein Auto um 90° im Uhrzeigersinn
     */
    public void rotiere() {
        Position p = new Position(this.getPositionX(), this.getPositionY());
        Strassenabschnitt aktuellerAbschnitt = Strassennetz.getInstance().getAbschnitte().get(p);
        int mittelpunkt[] = {aktuellerAbschnitt.getPositionX() + aktuellerAbschnitt.getGroesse() / 2, aktuellerAbschnitt.getPositionY() + aktuellerAbschnitt.getGroesse() / 2};
        int alterPunkt[] = {this.getPositionX(), this.getPositionY()};
        double winkel = Math.toRadians(90.0);
        double zwPunkt[] = {Math.cos(winkel) * (alterPunkt[0] - mittelpunkt[0]) + (-1) * Math.sin(winkel) * (alterPunkt[1] - mittelpunkt[1]),
                Math.sin(winkel) * (alterPunkt[0] - mittelpunkt[0]) + Math.cos(winkel) * (alterPunkt[1] - mittelpunkt[1])};
        double neuerPunkt[] = {zwPunkt[0] + mittelpunkt[0], zwPunkt[1] + mittelpunkt[1]};
        this.setPositionX((int) Math.round(neuerPunkt[0]));
        this.setPositionY((int) Math.round(neuerPunkt[1]));
        this.setRichtung(richtung.get().naechstes());
    }

    /**
     * innere Klasse zur Definition eines Wendepunktes
     * bestehend aus den Koordinaten und der Richtung nach Wendung
     */
    class Wendepunkt {

        private int x;
        private int y;
        private Himmelsrichtung richtung;

        public Wendepunkt(int x, int y, Himmelsrichtung richtung) {
            this.x = x;
            this.y = y;
            this.richtung = richtung;
        }

        /**
         * Berechnet Manhatten-Distanz bis zum übergebenen Koordinaten
         *
         * @param x X-Koordinate
         * @param y Y-Koordinate
         * @return Distanz
         */
        public int distanzBisWendepunkt(int x, int y) {
            return Math.abs(x - this.x) + Math.abs(y - this.y);
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public Himmelsrichtung getRichtung() {
            return richtung;
        }

    }

    public float getGeschwindigkeit() {
        return geschwindigkeit;
    }

    public void setGeschwindigkeit(float geschwindigkeitsfaktor) {
        this.geschwindigkeit = Math.min(Math.round(geschwindigkeitsfaktor * MAXGESCHWINDIGKEIT), MAXGESCHWINDIGKEIT);
    }

    public float getGeschwindigkeitsfaktor() {
        return (this.geschwindigkeit * 1.0f) / MAXGESCHWINDIGKEIT;
    }

    public Himmelsrichtung getRichtung() {
        return richtung.get();
    }

    public void setRichtung(Himmelsrichtung richtung) {
        this.richtung.set(richtung);
    }

    public int getPositionX() {
        return positionX.get();
    }

    public SimpleIntegerProperty positionXProperty() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX.set(positionX);
    }

    public int getPositionY() {
        return positionY.get();
    }

    public SimpleIntegerProperty positionYProperty() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY.set(positionY);
    }

    public int getBreite() {
        return breite;
    }

    public int getLaenge() {
        return laenge;
    }

    public Rectangle getRectangle() {
        if (rectangle == null) initRectangle();
        return rectangle;
    }

    public SimpleObjectProperty<Himmelsrichtung> richtungProperty() {
        return richtung;
    }

    public Auto.AutoModell getAutoModell() {
        return this.autoModell;
    }
}
