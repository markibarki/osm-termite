package intransix.osm.termite.app.filter;

import intransix.osm.termite.map.workingdata.OsmObject;

/**
 * This is a filter rule. A FeatureFilter uses a list of filter rules to create
 * a filter. Because filter rules are chained together, there are two distinct types
 * of filter rules, TURN_ON and TURN_OFF.  A TURN_ON filter will only turn an object
 * on and a TURN_OFF filter will only turn an object off.
 * 
 * @author sutter
 */
public interface FilterRule {
	
	public final static int RENDER_ENABLED = 0x01;
	public final static int EDIT_ENABLED = 0x02;
	public final static int ALL_DISABLED = 0x00;
	public final static int ALL_ENABLED = RENDER_ENABLED | EDIT_ENABLED;
	
	/** This method returns the desired initial filter state. The object will 
	 * be initialized to this initial state for the first filter rule in the filter.
	 * Successive filter rules will not have this method called, rather the 
	 * object will retain the value after the previous filter rule ran.
	 * 
	 * @return		The desired initial state of the object for this filter 
	 */
	int getInitialState();
	
	/** This method gets the filter value for a given object, with a initial
	 * filter value that may have been set by previous filter rules.
	 * 
	 * @param osmObject		The object to be filtered
	 * @param initialValue	The filter state of the object before this rule is applied.
	 * @return				The value of this filter rule for this object.
	 */ 
	int getFilterValue(OsmObject osmObject, int initialValue);
}
