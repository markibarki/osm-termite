package intransix.osm.termite.app.filter;

import intransix.osm.termite.map.workingdata.OsmObject;
import java.util.*;

/**
 *
 * @author sutter
 */
public class FilterManager {
	
	//this is for filtering the features

	private FeatureFilter filter = null;
	private List<FilterListener> filterListeners = new ArrayList<FilterListener>();
	
	public void setFilter(FeatureFilter filter) {
		this.filter = filter;
		
		//update the filtered value for all features. */
		for(FilterListener fl:filterListeners) {
			fl.onFilterChanged();
		}
	}
	
	public FeatureFilter getFilter() {
		return filter;
	}
	
	public void addFilterListener(FilterListener filterListener) {
		if(!filterListeners.contains(filterListener)) {
			filterListeners.add(filterListener);
		}
	}
	
	public void removeFilterListener(FilterListener filterListener) {
		filterListeners.remove(filterListener);
	}
	
	/** This method runs the given object through the filter. */
	public int getFilterValue(OsmObject osmObject) {
		if(filter != null) {
			return filter.getFitlerValue(osmObject);
		}
		else {
			return FilterRule.ALL_ENABLED;
		}
	}
		
}
