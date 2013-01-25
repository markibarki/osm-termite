package intransix.osm.termite.app.geocode;

/**
 * This interface is used for notifications of geocode state changes.
 * 
 * @author sutter
 */
public interface GeocodeStateListener {
	
	/** This method is called when the geocode type changes. */
	void geocodeTypeChanged(GeocodeEditorMode.GeocodeType geocodeType);
	
	/** This method is called when the geocode moide changes. */
	void geocodeModeChanged(GeocodeEditorMode.LayerState layerState);
}
