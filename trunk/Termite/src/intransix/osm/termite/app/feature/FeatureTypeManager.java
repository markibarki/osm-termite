package intransix.osm.termite.app.feature;

import intransix.osm.termite.map.data.OsmModel;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.feature.FeatureInfoMap;
import intransix.osm.termite.util.JsonIO;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author sutter
 */
public class FeatureTypeManager {
	
	private FeatureInfoMap featureInfoMap;
	
	private FeatureInfo activeFeatureType;
	private List<FeatureTypeListener> featureTypeListeners = new ArrayList<FeatureTypeListener>();
	
	public void init(String configFileName) throws Exception {
		JSONObject featureInfoJson = JsonIO.readJsonFile(configFileName);
		featureInfoMap = FeatureInfoMap.parse(featureInfoJson);
		
		//load feature info into model
		OsmModel.featureInfoMap = featureInfoMap;
	}
	
	/** This method gets the feature info map. */
	public FeatureInfoMap getFeatureInfoMap() {
		return featureInfoMap;
	}
		
	/** This method returns the active feature type. */
	public FeatureInfo getActiveFeatureLayer() {
		return activeFeatureType;
	}
	
	/** This method will dispatch a feature layer selected event. It should be called
	 * when a feature layer is selected to notify all interested objects. */
	public void setActiveFeatureType(FeatureInfo featureInfo) {
		activeFeatureType = featureInfo;
		
		for(FeatureTypeListener listener:featureTypeListeners) {
			listener.onFeatureTypeSelected(featureInfo);
		}
	}
	
	/** This adds a feature type listener. */
	public void addFeatureLayerListener(FeatureTypeListener listener) {
		featureTypeListeners.add(listener);
	}
	
	public void removeFeatureLayerListener(FeatureTypeListener listener) {
		featureTypeListeners.remove(listener);
	}
}
