package intransix.osm.termite.app.geocode;

/**
 * This interface is used for notifications of geocode state changes.
 * 
 * @author sutter
 */
public interface GeocodeStateListener {
	void geocodeTypeChanged(GeocodeEditorMode.GeocodeType geocodeType);
	
	void geocodeModeChanged(GeocodeEditorMode.LayerState layerState);
}
