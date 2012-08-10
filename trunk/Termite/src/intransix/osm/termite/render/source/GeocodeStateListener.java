package intransix.osm.termite.render.source;

/**
 * This interface is used for notifications of geocode state changes.
 * 
 * @author sutter
 */
public interface GeocodeStateListener {
	void geocodeTypeChanged(GeocodeLayer.GeocodeType geocodeType);
	
	void geocodeModeChanged(GeocodeLayer.LayerState layerState);
}
