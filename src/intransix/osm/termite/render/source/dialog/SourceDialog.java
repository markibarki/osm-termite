package intransix.osm.termite.render.source.dialog;

import intransix.osm.termite.app.geocode.GeocodeManager;
import intransix.osm.termite.app.maplayer.MapLayerManager;
import intransix.osm.termite.app.viewregion.ViewRegionManager;
import intransix.osm.termite.gui.TermiteFXGui;
import intransix.osm.termite.gui.dialog.DialogCallback;
import intransix.osm.termite.gui.dialog.TermiteDialog;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

/**
 *
 * @author sutter
 */
public class SourceDialog extends TermiteDialog {
	
	public SourceDialog() {
		super(TermiteFXGui.getStage());
	}
	
	public void init(final MapLayerManager mapLayerManager, final ViewRegionManager viewRegionManager, final GeocodeManager geocodeManager) {
		
		DialogCallback closeCallback = new DialogCallback(){
			@Override
			public boolean handle(TermiteDialog dialog) {
				return true;
			}
		};
		final SourceDialogContent content = new SourceDialogContent(this, mapLayerManager, viewRegionManager, geocodeManager);
		
		super.init(content, null, closeCallback, null, "Close");
	}
	
	
}
