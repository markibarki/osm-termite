package intransix.osm.termite.app.level;

import intransix.osm.termite.map.data.OsmRelation;
import intransix.osm.termite.map.data.OsmWay;
import java.util.List;
import java.util.TreeMap;

/**
 *
 * @author sutter
 */
public interface LevelStructureListener {
	
	/** This method is called when the level structure for the data changes. 
	 * 
	 * @param treeMap	A mapping of level relations to structure footprints. 
	 */
	void levelStructureChanged(TreeMap<OsmWay,List<OsmRelation>> treeMap);
}
