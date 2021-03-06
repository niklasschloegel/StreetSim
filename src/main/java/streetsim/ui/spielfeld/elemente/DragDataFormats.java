package streetsim.ui.spielfeld.elemente;

import javafx.scene.input.DataFormat;

/**
 * Konstanten, die für Drag and Drop Aktionen den Inhalt des
 * {@link javafx.scene.input.Dragboard}s festlegt.
 */
public class DragDataFormats {

    public static final String GERADE_FORMAT = "GERADE";
    public static final String KREUZUNG_FORMAT = "KREUZUNG";
    public static final String KURVE_FORMAT = "KURVE";
    public static final String TSTUECK_FORMAT = "TSTUECK";
    public static final String AMPEL_FORMAT = "AMPEL";

    // Straßenabschnitt aus dem Spielfeld
    public static final String STRASSENABSCHNITT = "STRASSENABSCHNITT";
    public static final DataFormat ABSCHNITTFORMAT = new DataFormat(STRASSENABSCHNITT);

}
