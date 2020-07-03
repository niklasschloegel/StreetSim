package streetsim.ui.spielfeld.elemente;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import streetsim.business.Ampel;
import streetsim.business.Auto;
import streetsim.business.Strassennetz;
import streetsim.ui.AbstractController;
import streetsim.ui.StreetSimApp;
import streetsim.ui.spielfeld.elemente.straßenabschnitte.GeradeView;
import streetsim.ui.spielfeld.elemente.straßenabschnitte.KreuzungView;
import streetsim.ui.spielfeld.elemente.straßenabschnitte.KurveView;
import streetsim.ui.spielfeld.elemente.straßenabschnitte.TStueckView;
import streetsim.ui.utils.ResourceAssist;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Verwaltung von Aktionen im Menü
 */
public class MenueController extends AbstractController<StreetSimApp> {

    enum AutoModelle {

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

    GeradeView gerade;
    KreuzungView kreuzung;
    KurveView kurve;
    TStueckView tstueck;
    List<AutoView> autoViews;
    List<ImageView> alleViews;
    AmpelView ampelView;

    public MenueController(Strassennetz netz, StreetSimApp app) {
        super(netz, app);

        rootView = new MenueView(AutoModelle.getAllViews());

        gerade = ((MenueView) rootView).gerade;
        kreuzung = ((MenueView) rootView).kreuzung;
        kurve = ((MenueView) rootView).kurve;
        tstueck = ((MenueView) rootView).tstueck;
        autoViews = ((MenueView) rootView).autoViews;
        ampelView = ((MenueView) rootView).ampelView;

        List<ImageView> viewList = List.of(gerade, kreuzung, kurve, tstueck, ampelView);
        alleViews = new ArrayList<>();
        alleViews.addAll(viewList);
        alleViews.addAll(autoViews);

        handlerAnmelden();
    }

    @Override
    public void handlerAnmelden() {
        alleViews.forEach(e -> {
            e.setOnDragDetected(event -> {

                Dragboard dragboard = e.startDragAndDrop(TransferMode.COPY);

                ClipboardContent content = new ClipboardContent();

                String df;
                if (e instanceof GeradeView) df = ViewDataFormats.GERADE_FORMAT;
                else if (e instanceof KreuzungView) df = ViewDataFormats.KREUZUNG_FORMAT;
                else if (e instanceof KurveView) df = ViewDataFormats.KURVE_FORMAT;
                else if (e instanceof TStueckView) df = ViewDataFormats.TSTUECK_FORMAT;
                else if (e instanceof AmpelView) df = ViewDataFormats.AMPEL_FORMAT;
                else df = ((AutoView) e).getAutoModell().name();

                dragboard.setDragView(e.getImage());
                content.putString(df);
                dragboard.setContent(content);

                event.consume();
            });
        });
    }
}
