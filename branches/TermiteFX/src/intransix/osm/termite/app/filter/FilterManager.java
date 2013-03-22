package intransix.osm.termite.app.filter;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.map.workingdata.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class FilterManager implements MapDataListener {
	
	private static int piggybackIndex;
	static {
		piggybackIndex = OsmObject.registerPiggybackUser();
	}
	
	//this is for filtering the features
	private MapDataManager mapDataManager;
	private FeatureFilter filter = null;
	private List<FilterListener> filterListeners = new ArrayList<FilterListener>();
	
	public void setFilter(FeatureFilter filter) {
		this.filter = filter;
		this.filterChanged();
		
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
	
	/** This method is called when the map data is set or cleared. It will be called 
	 * with the value true when the data is set and false when the data is cleared. The
	 * method osmDataChanged is also called when the data is set.
	 * 
	 * @param dataPresent	Set to true if data is present, false if data is cleared.
	 */
	public void onMapData(MapDataManager mapDataManager, boolean dataPresent) {
		this.mapDataManager = mapDataManager;
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	public void osmDataChanged(MapDataManager mapDataManager, int editNumber) {
		OsmData osmData = mapDataManager.getOsmData();
		
		//update feature info
		for(OsmNode node:osmData.getOsmNodes()) {
			processObjectFilter(node);
		}
		for(OsmWay way:osmData.getOsmWays()) {
			processObjectFilter(way);
		}
	}
	
	/** This method returns the priority for the listener. It is used to determine
	 * the order the listeners get called.
	 * 
	 * @return				The priority of the map listener
	 */
	public int getMapDataListenerPriority() {
		return PRIORITY_DATA_MODIFY_1;
	}
	
	
	/** This method updates the feature info for the given object. */
	private void processObjectFilter(OsmObject osmObject) {

		FilterData filterData = (FilterData)osmObject.getPiggybackData(piggybackIndex);
		if(filterData == null) {
			filterData = new FilterData();
			osmObject.setPiggybackData(piggybackIndex, filterData);
		}
		
		if(!filterData.isUpToDate(osmObject)) {
			//filter state
			int filterState = this.getFilterValue(osmObject);
			filterData.setFilterState(filterState);
			
			filterData.markAsUpToDate(osmObject);
		}
	}
	
	/** This method updates the feature info for the given object. */
	private void updateFilter(OsmObject osmObject) {
		FilterData filterData = (FilterData)osmObject.getPiggybackData(piggybackIndex);
		if(filterData == null) {
			//the object shoudl have a fully processed feature data entry
			//do not do a plain filter update
			processObjectFilter(osmObject);
			return;
		}
		
		int filterState = this.getFilterValue(osmObject);
		filterData.setFilterState(filterState);
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

	public static boolean getObjectEditEnabled(OsmObject osmObject) {
		FilterData fd = (FilterData)osmObject.getPiggybackData(piggybackIndex);
		if(fd != null) {
			return fd.editEnabled();
		}
		else {
			return true;
		}
	}
	
	public static boolean getObjectRenderEnabled(OsmObject osmObject) {
		FilterData fd = (FilterData)osmObject.getPiggybackData(piggybackIndex);
		if(fd != null) {
			return fd.renderEnabled();
		}
		else {
			return true;
		}
	}
	
	public static boolean getSegmentEditEnabled(OsmSegment segment) {
		return (getObjectEditEnabled(segment.getNode1()) && 
				getObjectEditEnabled(segment.getNode2()) );
	}
	
	public static boolean getSegmentRenderEnabled(OsmSegment segment) {
		return (getObjectRenderEnabled(segment.getNode1()) && 
				getObjectRenderEnabled(segment.getNode2()) );
	}
	
		/** This method is called when the filter changes. */
	public void filterChanged() {
		OsmData osmData = mapDataManager.getOsmData();
		if(osmData != null) {
			for(OsmNode node:osmData.getOsmNodes()) {
				updateFilter(node);
			}
			for(OsmWay way:osmData.getOsmWays()) {
				updateFilter(way);
			}
		}
	}
		
}
