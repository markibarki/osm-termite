package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmWay;
import java.util.List;

/**
 *
 * @author sutter
 */
public class RemoveWayNodeEdit extends EditOperation {
	
	public RemoveWayNodeEdit(OsmData osmData) {
		super(osmData);
	}
	
	public boolean removeNodesFromWay(OsmWay way, List<Integer> wayNodes) {
		return false;
	}
}
