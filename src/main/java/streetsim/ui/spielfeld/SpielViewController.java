package streetsim.ui.spielfeld;

import com.google.gson.Gson;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.hildan.fxgson.FxGson;
import streetsim.business.*;
import streetsim.business.abschnitte.Gerade;
import streetsim.business.abschnitte.Kreuzung;
import streetsim.business.abschnitte.Kurve;
import streetsim.business.abschnitte.TStueck;
import streetsim.business.exceptions.KeinAbschnittException;
import streetsim.business.exceptions.SchonBelegtException;
import streetsim.ui.AbstractController;
import streetsim.ui.StreetSimApp;
import streetsim.ui.spielfeld.elemente.*;
import streetsim.ui.spielfeld.elemente.strassenabschnitte.GeradeView;
import streetsim.ui.spielfeld.elemente.strassenabschnitte.KreuzungView;
import streetsim.ui.spielfeld.elemente.strassenabschnitte.KurveView;
import streetsim.ui.spielfeld.elemente.strassenabschnitte.TStueckView;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Verwaltung der folgenden drei Controller:
 * Spielfeldcontroller, NavigationsController, MenueController
 */
public class SpielViewController extends AbstractController<StreetSimApp> {
    private BorderPane spielView;
    private Pane menView, navView, spielfeldView, overlayView;
    private HintergrundView hv;
    private NavigationController navCon;
    private MenueController menCon;
    private SpielfeldController spielfeldCon;
    private OverlayController overlayController;
    private Button hamburger;

    public SpielViewController(Strassennetz netz, StreetSimApp app) {
        super(netz, app);

        rootView = new StackPane();
        spielView = new BorderPane();

        hv = new HintergrundView();

        navCon = new NavigationController(netz, app);
        menCon = new MenueController(netz, app);

        spielfeldCon = new SpielfeldController(netz, app);
        overlayController = new OverlayController(netz);

        menView = menCon.getRootView();
        navView = navCon.getRootView();

        spielfeldView = spielfeldCon.getRootView();
        overlayView = overlayController.getRootView();

        hamburger = new Button();
        hamburger.getStyleClass().add("navbtn");
        hamburger.setPickOnBounds(true);
        hamburger.setId("menu-cross");
        hamburger.setAlignment(Pos.TOP_RIGHT);

        StackPane menStack = new StackPane();
        menStack.getChildren().addAll(menView, hamburger);
        menStack.setAlignment(Pos.TOP_RIGHT);

        spielView.setRight(menStack);
        spielView.setLeft(navView);

        rootView.getChildren().addAll(hv, spielfeldView, spielView, overlayView);
        handlerAnmelden();
    }

    @Override
    public void handlerAnmelden() {

        rootView.setOnDragDetected(e -> {
            hideMenu();
            Strassenabschnitt s = netz.strasseAnPos((int) Math.round(e.getX()), (int) Math.round(e.getY()));


            if (s != null) {

                Dragboard dragboard = rootView.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                Gson gson = FxGson.coreBuilder().registerTypeAdapter(Strassenabschnitt.class, StrassenAdapter.getInstance())
                        .create();
                String serializedStrasse = gson.toJson(s, Strassenabschnitt.class);

                content.put(DragDataFormats.ABSCHNITTFORMAT, serializedStrasse);

                ImageView imageView;

                if (s instanceof Gerade) imageView = new GeradeView();
                else if (s instanceof Kreuzung) imageView = new KreuzungView();
                else if (s instanceof Kurve) imageView = new KurveView();
                else imageView = new TStueckView();

                for (int i = 0; i < s.getRotiertCounter(); i++) {
                    imageView.setRotate(imageView.getRotate() + 90);
                }
                Image img = imageView.snapshot(null, null);

                dragboard.setDragView(img);
                dragboard.setContent(content);
            }
        });

        rootView.setOnDragOver(event -> {
            hideMenu();

            boolean dropSupported = true;
            Dragboard dragboard = event.getDragboard();

            Strassenabschnitt s = netz.strasseAnPos((int) Math.round(event.getX()), (int) Math.round(event.getY()));
            if (event.getTransferMode() == TransferMode.COPY) {
                if (dragboard.hasString()) {
                    String dataString = dragboard.getString();

                    switch (dataString) {
                        case DragDataFormats.AMPEL_FORMAT:
                            if (s == null || s.isAmpelAktiv()) dropSupported = false;
                            break;
                        case DragDataFormats.GERADE_FORMAT:
                        case DragDataFormats.KREUZUNG_FORMAT:
                        case DragDataFormats.KURVE_FORMAT:
                        case DragDataFormats.TSTUECK_FORMAT:
                            if (s != null) dropSupported = false;
                            break;
                        default:
                            dropSupported = Arrays.stream(Auto.AutoModell.values()).map(Enum::name).collect(Collectors.toList()).contains(dataString) && s != null;
                            break;
                    }
                    if (dropSupported) event.acceptTransferModes(TransferMode.COPY);
                }
            } else {
                dropSupported = event.getTransferMode() == TransferMode.MOVE && dragboard.hasContent(DragDataFormats.ABSCHNITTFORMAT) && s == null;
                if (dropSupported) event.acceptTransferModes(TransferMode.MOVE);

            }


            event.consume();
        });

        rootView.setOnDragDropped(event -> {
            showMenu();
            Dragboard dragboard = event.getDragboard();
            if (dragboard.hasString()) {
                String dataString = dragboard.getString();
                Strassenabschnitt s = netz.strasseAnPos((int) Math.round(event.getX()), (int) Math.round(event.getY()));

                switch (dataString) {
                    case DragDataFormats.AMPEL_FORMAT:
                        netz.ampelnAktivieren(s);
                        break;
                    case DragDataFormats.GERADE_FORMAT:
                        Gerade g = new Gerade((int) Math.round(event.getX()), (int) Math.round(event.getY()));
                        netz.strasseAdden(g);
                        break;
                    case DragDataFormats.KREUZUNG_FORMAT:
                        Kreuzung kr = new Kreuzung((int) Math.round(event.getX()), (int) Math.round(event.getY()));
                        netz.strasseAdden(kr);
                        break;
                    case DragDataFormats.KURVE_FORMAT:
                        Kurve ku = new Kurve((int) Math.round(event.getX()), (int) Math.round(event.getY()));
                        netz.strasseAdden(ku);
                        break;
                    case DragDataFormats.TSTUECK_FORMAT:
                        TStueck t = new TStueck((int) Math.round(event.getX()), (int) Math.round(event.getY()));
                        netz.strasseAdden(t);
                        break;
                    default:
                        if (Arrays.stream(Auto.AutoModell.values()).map(Enum::name).collect(Collectors.toList()).contains(dataString)) {
                            if (netz.posBelegt(s)) {
                                Auto.AutoModell am = Auto.AutoModell.valueOf(dataString);
                                Auto a = new Auto((int) Math.round(event.getX()), (int) Math.round(event.getY()), am);
                                try {
                                    netz.autoAdden(a);
                                } catch (KeinAbschnittException | SchonBelegtException e) {
                                    // TODO infotext in ui?
                                    event.setDropCompleted(true);
                                    event.consume();
                                    return;
                                }
                            }
                        }
                        break;
                }
            } else if (dragboard.hasContent(DragDataFormats.ABSCHNITTFORMAT)) {
                String serializedStrasse = (String) dragboard.getContent(DragDataFormats.ABSCHNITTFORMAT);
                Gson gson = FxGson.coreBuilder().registerTypeAdapter(Strassenabschnitt.class, StrassenAdapter.getInstance())
                        .enableComplexMapKeySerialization()
                        .create();
                Strassenabschnitt s = gson.fromJson(serializedStrasse, Strassenabschnitt.class);
                netz.bewegeStrasse(s, (int) Math.round(event.getX()), (int) Math.round(event.getY()));
            }


            event.setDropCompleted(true);
            event.consume();


        });

        rootView.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                double x = e.getX();
                double y = e.getY();
                Strassenabschnitt s = netz.strasseAnPos((int) Math.round(x), (int) Math.round(y));
                if (s != null) {
                    Position p = new Position((int) Math.round(x), (int) Math.round(y));
                    if(spielfeldCon.getAutoMap().containsKey(p)) {
                        for (Auto a : spielfeldCon.getAutoMap().get(p)) {
//                        überprüfen ob rechtsklick innerhalb des Autos geschehen ist um Auto Overlay zu aktivieren
                            if (x <= a.getPositionX() + (double) a.getBreite() / 2 && x >= a.getPositionX() - (double) a.getBreite() / 2
                                    && y >= a.getPositionY() - (double) a.getLaenge() / 2 && y <= a.getPositionY() + (double) a.getLaenge() / 2) {
                                overlayController.setAutoPosition(a.getPositionX(), a.getPositionY());
                                overlayController.enableAuto();
                                overlayController.aktAuto(a);
                                System.out.println("autoOverlay roll out!!!!");
                            }
                            return;
                        }
                    } else {
                        overlayController.setPosition(p.getPositionX(), p.getPositionY());
                        overlayController.enableStrasse(s);
                        return;
                    }
                }
            }
            overlayController.disable();
        });

        hamburger.setOnAction(e -> {
            if (hamburger.getId().equals("menu-stripes")) { //menu eingeklappt -> aufklappen
                showMenu();
//                netz.setSimuliert(false);
//                navCon.pause();
            } else { //menu aufgeklappt -> einklappen
                hideMenu();
            }
        });

        netz.simuliertProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) hideMenu();
            else showMenu();
        });
    }

    private void showMenu(){
        hamburger.setId("menu-cross");
        menView.setVisible(true);
    }

    private void hideMenu(){
        hamburger.setId("menu-stripes");
        menView.setVisible(false);
    }

}
