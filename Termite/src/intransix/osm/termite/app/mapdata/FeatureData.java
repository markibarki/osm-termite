package intransix.osm.termite.app.mapdata;

import intransix.osm.termite.app.filter.FilterRule;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.PiggybackData;
import intransix.osm.termite.map.feature.FeatureInfo;

/**
 *
 * @author sutter
 */
public class FeatureData extends PiggybackData {
	
	private FeatureInfo featureInfo = null;
	private int filterState;
	
	public void setFeatureInfo(FeatureInfo featureInfo) {
		this.featureInfo = featureInfo;
	}
	
	/** This method gets the feature info associated with this object. */
	public FeatureInfo getFeatureInfo() {
		return featureInfo;
	}
		
	public void setFilterState(int state) {
		this.filterState = state;
	}
	
	public boolean editEnabled() {
		return ((filterState & FilterRule.EDIT_ENABLED) != 0);
	}
	
	public boolean renderEnabled() {
		return ((filterState & FilterRule.RENDER_ENABLED) != 0);
	}
	
	public int getFilterState() {
		return filterState;
	}
	
}
