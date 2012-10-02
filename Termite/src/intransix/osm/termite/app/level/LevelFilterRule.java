package intransix.osm.termite.app.level;

import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.filter.FilterRule;

/**
 *
 * @author sutter
 */
public class LevelFilterRule implements FilterRule {
	
	private OsmRelation level;
	
	public LevelFilterRule(OsmRelation level) {
		this.level = level;
	}

	/** This method returns the desired initial filter state. The object will 
	 * be initialized to this initial state for the first filter rule in the filter.
	 * Successive filter rules will not have this method called, rather the 
	 * object will retain the value after the previous filter rule ran.
	 * 
	 * @return		The desired initial state of the object for this filter 
	 */
	@Override
	public int getInitialState() {
		return FilterRule.ALL_DISABLED;
	}
	
	/** This method gets the filter value for a given object, with a initial
	 * filter value that may have been set by previous filter rules.
	 * 
	 * @param osmObject		The object to be filtered
	 * @param initialValue	The filter state of the object before this rule is applied.
	 * @return				The value of this filter rule for this object.
	 */ 
	@Override
	public int getFilterValue(OsmObject osmObject, int initialValue) {
		boolean enabled = false;
		if(osmObject instanceof OsmNode) {
			enabled = nodeOnLevel((OsmNode)osmObject);
		}
		else if(osmObject instanceof OsmWay) {
			enabled = wayOnLevel((OsmWay)osmObject);
		}
		
		if(enabled) {
			return FilterRule.ALL_ENABLED;
		}
		else {
			return initialValue;
		}
	}
	
	private boolean nodeOnLevel(OsmNode node) {
		for(OsmRelation relation:node.getRelations()) {
			if(relation == level) {
				return true;
			}
		}
		return false;
	}
	
	private boolean wayOnLevel(OsmWay way) {
		for(OsmNode node:way.getNodes()) {
			if(nodeOnLevel(node)) return true;
		}
		return false;
	}
}
