package intransix.osm.termite.app.filter;

import intransix.osm.termite.map.data.*;
import intransix.osm.termite.app.mapdata.MapDataListener;
import java.util.*;

/**
 *
 * @author sutter
 */
public class FilterManager implements MapDataListener, OsmDataChangedListener {
	
	//this is for filtering the features
	private OsmData osmData = null;
	private FeatureFilter filter = null;
	
	public void setFilter(FeatureFilter filter) {
		this.filter = filter;
		
		//update the filtered value for all features. */
		filterData();
	}
	
	/** This method is called when the map data is set of cleared. It will be called 
	 * with the value null when the data is cleared. 
	 * 
	 * @param mapData	The map data object
	 */
	@Override
	public void onMapData(OsmData osmData) {
		if(this.osmData != null) {
			osmData.removeDataChangedListener(this);
		}
		
		this.osmData = osmData;
		filterData();
		
		if(this.osmData != null) {
			osmData.addDataChangedListener(this);
		}
	}
	
	/** This method is called when the data has changed. It runs the filter on the
	 * new data.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		//update filter
		filterData();
	}
	
	/** This method returns the type of user this listener is. The type of listener
	 * determines the order in which the listener is called when data has changed. 
	 * 
	 * @return 
	 */
	@Override
	public int getListenerType() {
		return OsmDataChangedListener.LISTENER_PREPROCESSOR;
	}
		
	/** this runs all object through the filter. */
	private void filterData() {
		if(osmData != null) {
			for(OsmNode node:osmData.getOsmNodes()) {
				filterObject(node);
			}
			for(OsmWay way:osmData.getOsmWays()) {
				filterObject(way);
			}

			//filter segments according to node state
			for(OsmSegment segment:osmData.getOsmSegments()) {
				int state1 = segment.getNode1().getFilterState();
				int state2 = segment.getNode2().getFilterState();
				segment.setFilterState(state1 & state2);
			}
		}
	}
	
	/** This method runs the given object through the filter. */
	void filterObject(OsmObject osmObject) {
		int filterValue;
		if(filter != null) {
			filterValue = filter.getFitlerValue(osmObject);
		}
		else {
			filterValue = FilterRule.ALL_ENABLED;
		}
		osmObject.setFilterState(filterValue);
	}
}
