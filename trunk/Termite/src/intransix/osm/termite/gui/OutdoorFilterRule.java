package intransix.osm.termite.gui;

import intransix.osm.termite.map.data.FilterRule;
import intransix.osm.termite.map.data.*;

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
	
	/** This method sets the filter value for the object, based on the object
	 * parameters. Multiple filter rules may run sequentially. Thus it is recommended that a 
	 * filter follows the policy of doing an AND operation of an OR operation with 
	 * the existing object filter state, rather than explicitly setting an
	 * enable/disable value which would nullify any previous filter rules.
	 * 
	 * @param osmObject		The object to be filtered
	 * @return				The value of this filter rule for this object.
	 */ 
	@Override
	public void filterObject(OsmObject osmObject) {
		boolean enabled = false;
		if(osmObject instanceof OsmNode) {
			enabled = nodeOutdoors((OsmNode)osmObject);
		}
		else if(osmObject instanceof OsmWay) {
			enabled = wayOutdoors((OsmWay)osmObject);
		}
		
		if(!enabled) {
			osmObject.bitwiseAndFilterState(FilterRule.ALL_DISABLED);
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
