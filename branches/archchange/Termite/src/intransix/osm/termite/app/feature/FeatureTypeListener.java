package intransix.osm.termite.app.feature;

import intransix.osm.termite.map.feature.*;

/**
 * This interface is used to receive notification when a feature layer information
 * is changed. 
 * 
 * @author sutter
 */
public interface FeatureTypeListener {
	
	/** This method is called when a feature layer is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param featureInfo	The selected feature type
	 */
	void onFeatureTypeSelected(FeatureInfo featureInfo);
	
}
