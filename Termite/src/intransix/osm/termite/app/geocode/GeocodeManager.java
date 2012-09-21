package intransix.osm.termite.app.geocode;

import intransix.osm.termite.gui.TermiteGui;
import intransix.osm.termite.render.source.GeocodeLayer;

/**
 *
 * @author sutter
 */
public class GeocodeManager {
	
	private GeocodeLayer geocodeLayer;
	private GeocodeEditorMode geocodeEditorMode;
	
	public void init() {
		geocodeLayer = new GeocodeLayer();
		geocodeEditorMode = new GeocodeEditorMode();
	}
	
	public GeocodeLayer getGeocodeLayer() {
		return geocodeLayer;
	}
	
	public GeocodeEditorMode getGeocodeEditorMode() {
		return geocodeEditorMode;
	}
	
}
