package intransix.osm.termite.render.source.dialog;

import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.gui.TermiteFXGui;
import intransix.osm.termite.gui.dialog.DialogCallback;
import intransix.osm.termite.gui.dialog.TermiteDialog;
import javafx.stage.Modality;

/**
 *
 * @author sutter
 */
public class SourceDialog extends TermiteDialog {
	
	GeocodeManager geocodeManager;
	MapLayerManager mapLayerManager;
	
	public SourceDialog(GeocodeManager geocodeManager, MapLayerManager mapLayerManager) {
		super(TermiteFXGui.getStage());
		this.geocodeManager = geocodeManager;
		this.mapLayerManager = mapLayerManager;
	}
	
	public void init() {
		DialogCallback closeCallback = new DialogCallback(){
			@Override
			public boolean handle(TermiteDialog dialog) {
				return true;
			}
		};
		SourceDialogContent content = new SourceDialogContent();
		content.setGeocodeManager(geocodeManager);
		
		super.init(content, null, closeCallback, null, "Close");
	}
}
