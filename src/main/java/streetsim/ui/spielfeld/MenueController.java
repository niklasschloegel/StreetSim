package streetsim.ui.spielfeld;

import javafx.scene.layout.Pane;
import streetsim.business.Strassennetz;
import streetsim.ui.AbstractController;
import streetsim.ui.StreetSimApp;

/**
 * Verwaltung von Aktionen im Menü
 */
public class MenueController extends AbstractController<StreetSimApp> {


    public MenueController(Strassennetz netz, StreetSimApp app) {
        super(netz, app);
    }

    @Override
    public void handlerAnmelden() {

    }
}