package intransix.osm.termite.app.level;

import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.filter.FilterRule;

/**
 *
 * @author sutter
 */
public class OutdoorFilterRule implements FilterRule {
	
	private OsmRelation level;
	
	public OutdoorFilterRule() {
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
		return FilterRule.ALL_ENABLED;
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
			enabled = nodeOutdoors((OsmNode)osmObject);
		}
		else if(osmObject instanceof OsmWay) {
			enabled = wayOutdoors((OsmWay)osmObject);
		}
		
		if(!enabled) {
			return FilterRule.ALL_DISABLED;
		}
		else {
			return initialValue;
		}
	}
	
	/** This returns false if the node is on a level. */
	private boolean nodeOutdoors(OsmNode node) {
		for(OsmRelation relation:node.getRelations()) {
			if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relation.getRelationType())) {
				return false;
			}
		}
		return true;
	}
	
	/** This returns true if any node on the way is outdoors. */
	private boolean wayOutdoors(OsmWay way) {
		for(OsmNode node:way.getNodes()) {
			if(nodeOutdoors(node)) return true;
		}
		return false;
	}
}
