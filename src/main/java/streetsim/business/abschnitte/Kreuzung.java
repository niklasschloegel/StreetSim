package streetsim.business.abschnitte;

import streetsim.business.Ampel;
import streetsim.business.Himmelsrichtung;
import streetsim.business.Strassenabschnitt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Realisierung eines Strassenabschnitts
 * maximal vie Ampel-Instanzen
 * vier mögliche Strassenrichtungen
 */
public class Kreuzung extends Strassenabschnitt {

    public Kreuzung(int positionX, int positionY, int groesse) {
        // TODO groesse bleibt doch immer gleich?
        super(positionX, positionY, definiereRichtungen(), groesse);
    }

    public static List<Himmelsrichtung> definiereRichtungen() {
        List<Himmelsrichtung> richtungen = Arrays.asList(Himmelsrichtung.values());
        return richtungen;
    }

}