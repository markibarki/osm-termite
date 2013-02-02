package intransix.osm.termite.app.feature;

import intransix.osm.termite.app.filter.FilterRule;
import intransix.osm.termite.map.workingdata.PiggybackData;
import intransix.osm.termite.map.feature.FeatureInfo;

/**
 *
 * @author sutter
 */
public class FeatureData extends PiggybackData {
	
	private FeatureInfo featureInfo = null;
	
	public void setFeatureInfo(FeatureInfo featureInfo) {
		this.featureInfo = featureInfo;
	}
	
	/** This method gets the feature info associated with this object. */
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}
	
}
