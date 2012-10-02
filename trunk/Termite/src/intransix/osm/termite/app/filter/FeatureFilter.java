package intransix.osm.termite.app.filter;

import intransix.osm.termite.map.workingdata.OsmObject;
import java.util.*;

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
		filterRules = filterRules;
		if(filterRules.size() > 0) {
			FilterRule initialRule = filterRules.get(0);
			initialState = initialRule.getInitialState();
		}
	}
	
	/** Constructor */
	public FeatureFilter(FilterRule filterRule) {
		filterRules = new ArrayList<FilterRule>();
		filterRules.add(filterRule);
		initialState = filterRule.getInitialState();
	}
	
	/** This method filters the given feature. */
	public int getFitlerValue(OsmObject osmObject) {
		int filterValue = initialState;
		for(FilterRule rule:filterRules) {
			filterValue = rule.getFilterValue(osmObject,filterValue);
		}
		return filterValue;
	}
}
