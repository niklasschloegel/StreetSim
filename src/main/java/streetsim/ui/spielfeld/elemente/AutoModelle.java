package streetsim.ui.spielfeld.elemente;

import javafx.scene.image.Image;
import streetsim.business.Auto;
import streetsim.ui.utils.ResourceAssist;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Definiert die möglichen Auto Modelle
 */
public enum AutoModelle {

    BLAU(new Image(ResourceAssist.getInstance().holeRessourceAusOrdnern("assets", "autos", "blauesAuto.png")), Auto.AutoModell.BLAU),
    POLIZEI(new Image(ResourceAssist.getInstance().holeRessourceAusOrdnern("assets", "autos", "polizeiAuto.png")), Auto.AutoModell.POLIZEI),
    ROT(new Image(ResourceAssist.getInstance().holeRessourceAusOrdnern("assets", "autos", "rotesAuto.png")), Auto.AutoModell.ROT);

    private final AutoView view;

    AutoModelle(Image img, Auto.AutoModell autoModell) {
        view = new AutoView(img, autoModell);
    }


    public AutoView getView() {
        return view;
    }

    public static List<AutoView> getAllViews() {
        return Arrays.stream(AutoModelle.values()).map(AutoModelle::getView).collect(Collectors.toList());
    }

}
