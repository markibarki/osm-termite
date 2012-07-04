package intransix.osm.termite.gui.contenttree;

import intransix.osm.termite.gui.*;
import intransix.osm.termite.map.data.*;
import java.util.*;

/**
 *
 * @author sutter
 */
public class ContentTree extends javax.swing.JTree implements LevelSelectedListener, MapDataListener {
	
	private OsmData mapData;
	private TreeMap<OsmWay,List<OsmRelation>> activeTreeMap = null;

	/**
	 * Creates new form ContentTree
	 */
	public ContentTree() {
	}
	
	/** This method is called when the map data is set or cleared. It should be called 
	 * with the value null when the data is cleared. It should be called from the
	 * UI thread.
	 * 
	 * @param feature	The selected map feature
	 */
	public void onMapData(OsmData mapData) {
		this.mapData = mapData;
		mapDataUpdated();
	}
	
	/** This method is called when a map level is selected. It may be called 
	 * with the value null for the level or the level and the structure. 
	 * 
	 * @param structure		The footprint in the outdoor map for the selected level
	 * @param level			The selected level
	 */
	public void onLevelSelected(OsmWay structure, OsmRelation level) {
//implement this
	}
	
	/** This method should be called when the map data is updated, from the UI thread. */
	public void mapDataUpdated() {
		TreeMap<OsmWay,List<OsmRelation>> newTreeMap = new TreeMap<OsmWay,List<OsmRelation>>(new WayComparator());
		//create a new data sturcture for levels,outdoor
		for(OsmRelation relation:mapData.getOsmRelations()) {
			if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relation.getRelationType())) {
				for(OsmMember member:relation.getMembers()) {
					if((OsmModel.ROLE_PARENT.equalsIgnoreCase(member.role))&&
							(member.osmObject instanceof OsmWay)) {
						OsmWay structure = (OsmWay)member.osmObject;
						addToMap(newTreeMap,structure,relation);
					}
				}
			}
		}
//sort the tree map!!
		if(doTreeMapUpdate(newTreeMap)) {
			updateTree(newTreeMap);
		}
	}
	
	/** This method adds a structure and level to a tree map collection. */
	private void addToMap(TreeMap<OsmWay,List<OsmRelation>> treeMap, OsmWay structure, OsmRelation level) {
		List<OsmRelation> levels = treeMap.get(structure);
		if(levels == null) {
			levels = new ArrayList<OsmRelation>();
			treeMap.put(structure,levels);
		}
		levels.add(level);
	}
	
	private boolean doTreeMapUpdate(TreeMap<OsmWay,List<OsmRelation>> newTreeMap) {
//implement this
return true;
	}
	
	private void updateTree(TreeMap<OsmWay,List<OsmRelation>> newTreeMap) {
//implement this
	}
	
	public class WayComparator implements Comparator<OsmWay> {
		public int compare(OsmWay way1, OsmWay way2) {
			//remove sign bit, making negative nubmers larger than positives (with small enough negatives)
			Long modId1 = way1.getId() & Long.MAX_VALUE;
			Long modId2 = way2.getId() & Long.MAX_VALUE;
			return modId1.compareTo(modId2);
		}
	}
	
	public class LevelComparator implements Comparator<OsmRelation> {
		public int compare(OsmRelation levelA, OsmRelation levelB) {
			int zlevel1 = levelA.getIntProperty(OsmModel.KEY_ZLEVEL,0);
			int zlevel2 = levelB.getIntProperty(OsmModel.KEY_ZLEVEL,0);
			return zlevel1 - zlevel2;
		}
	}
}
