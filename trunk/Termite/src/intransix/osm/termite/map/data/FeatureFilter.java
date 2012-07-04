package intransix.osm.termite.map.data;

import java.util.List;

/**
 * This is a filter for features. It should be populated with a list of filter rules. 
 * 
 * @author sutter
 */
public class FeatureFilter {
	
	private List<FilterRule> filterRules;
	private int initialState;
	
	/** Constructor */
	public FeatureFilter(List<FilterRule> filterRules) {
		this.filterRules = filterRules;
		if(filterRules.size() > 0) {
			FilterRule initialRule = filterRules.get(0);
			initialState = initialRule.getInitialState();
		}
	}
	
	/** This method filters the given feature. */
	public void filterFeature(OsmObject osmObject) {
		osmObject.setFilterState(initialState);
		for(FilterRule rule:filterRules) {
			rule.filterObject(osmObject);
		}
	}
}
