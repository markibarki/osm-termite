package intransix.osm.termite.gui;

import intransix.osm.termite.map.data.FilterRule;
import intransix.osm.termite.map.data.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class StructureFilterRule implements FilterRule {
	
	private List<OsmObject> objects = new ArrayList<OsmObject>();
	
	public StructureFilterRule(OsmWay structure) {
		populateMultipolyList(structure);
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
		if(osmObject instanceof OsmWay) {
			//see if this way is in the structure (there are multiple if it is a multipoly)
			enabled = objects.contains(osmObject);
		}
		else if(osmObject instanceof OsmNode) {
			//check if this node is on a way in the structure
			for(OsmWay way:((OsmNode)osmObject).getWays()) {
				if(objects.contains(way)) {
					enabled = true;
					break;
				}
			}
		}
		
		if(enabled) {
			osmObject.bitwiseOrFilterState(FilterRule.ALL_ENABLED);
		}
	}

	private void populateMultipolyList(OsmWay way) {
		//handle the case this is in a relation
		for(OsmRelation relation:way.getRelations()) {
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relation.getRelationType())) {
				for(OsmMember member:relation.getMembers()) {
					this.objects.add(member.osmObject);
				}
			}
		}
		//if this is not in a relation, just add this object
		if(objects.isEmpty()) {
			objects.add(way);
		}
	}
}
