package intransix.osm.termite.app.level;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmMember;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.app.filter.FeatureFilter;
import intransix.osm.termite.app.filter.FilterRule;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.filter.FilterManager;
import java.util.*;

/**
 *
 * @author sutter
 */
public class LevelManager implements MapDataListener {
	
	private MapDataManager mapDataManager;
	private FilterManager filterManager;
	
	private OsmWay selectedStructure;
	private OsmRelation selectedLevel;
	private List<LevelSelectedListener> levelSelectedListeners = new ArrayList<LevelSelectedListener>();
	private List<LevelStructureListener> levelStructureListeners = new ArrayList<LevelStructureListener>();
	
	private TreeMap<OsmWay,List<OsmRelation>> activeTreeMap = null;
	
	public LevelManager(MapDataManager mapDataManager, FilterManager filterManager) {
		this.mapDataManager = mapDataManager;
		this.filterManager = filterManager;
	}
	
	/** This method is called when the map data is set or cleared. It should be called 
	 * with the value null when the data is cleared. It should be called from the
	 * UI thread.
	 */
	@Override
	public void onMapData(boolean dataPresent) {
		
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		mapDataUpdated();
	}
	
	/** This returns the priority for this object as a map data listener. */
	@Override
	public int getMapDataListenerPriority() {
		return 2;
	}
	
	/** This method should be called when the map data is updated, from the UI thread. */
	public void mapDataUpdated() {
		OsmData osmData = mapDataManager.getOsmData();
		if(osmData != null) {
			TreeMap<OsmWay,List<OsmRelation>> newTreeMap = new TreeMap<OsmWay,List<OsmRelation>>(new WayComparator());
			//create a new data sturcture for levels,outdoor
			boolean added = false;
			for(OsmRelation relation:osmData.getOsmRelations()) {
				if(OsmModel.TYPE_LEVEL.equalsIgnoreCase(relation.getRelationType())) {
					for(OsmMember member:relation.getMembers()) {
						if((OsmModel.ROLE_PARENT.equalsIgnoreCase(member.role))&&
								(member.osmObject instanceof OsmWay)) {
							OsmWay structure = (OsmWay)member.osmObject;
							addToMap(newTreeMap,structure,relation);
							added = true;
							break;
						}
					}
					if(!added) {
						//handle levels that have no parent here!!!
System.out.println("Warning! There is a level with no parent object.");
					}
				}
			}
			//sort the levels for each structure
			LevelComparator lc = new LevelComparator();
			for(List<OsmRelation> rs:newTreeMap.values()) {
				Collections.sort(rs, lc);
			}
			//update the list (if needed)
			if(treeUpdated(newTreeMap)) {
				activeTreeMap = newTreeMap;
				notifyTreeChange(newTreeMap);
			}
		}
		else {
			//clear the tree if there is no data
			activeTreeMap = null;
			notifyTreeChange(null);
		}
	}
	
	//---------------------
	// Level Selection
	//---------------------
	
	public OsmWay getSelectedStructure() {
		return selectedStructure;
	}
	
	public OsmRelation getSelectedLevel() {
		return this.selectedLevel;
	}

	/** This adds a level selected listener. */
	public void addLevelSelectedListener(LevelSelectedListener listener) {
		levelSelectedListeners.add(listener);
	}
	
	/** This removes a level selected listener. */
	public void removeLevelSelectedListener(LevelSelectedListener listener) {
		levelSelectedListeners.remove(listener);
	}
	
	/** This method will dispatch a level selected event. It should be called
	 * when a level is selected to notify all interested objects. */
	public void setSelectedLevel(OsmWay structure, OsmRelation level) {
		
		this.selectedStructure = structure;
		this.selectedLevel = level;
		
		//update filter
		if(filterManager != null) {
			FilterRule filterRule = null;
			if(level != null) {
				filterRule = new LevelFilterRule(level);

			}
			else if(structure != null) {
				filterRule = new StructureFilterRule(structure);
			}
			else {
				filterRule = new OutdoorFilterRule();
			}

			FeatureFilter filter = new FeatureFilter(filterRule);
			filterManager.setFilter(filter);
			
			//notify listeners
			for(LevelSelectedListener listener:levelSelectedListeners) {
				listener.onLevelSelected(structure,level);
			}
		}
	}
	
	//----------------------
	// Tree Update
	//----------------------
	
	/** This adds a level selected listener. */
	public void addLevelStructureListener(LevelStructureListener listener) {
		levelStructureListeners.add(listener);
	}
	
	/** This removes a level selected listener. */
	public void removeLevelStructureListener(LevelStructureListener listener) {
		levelStructureListeners.remove(listener);
	}
	
	/** This method retrieves the current tree map. */
	public TreeMap<OsmWay,List<OsmRelation>> getTreeMap() {
		return activeTreeMap;
	}
	
	//=========================
	// Private Methods
	//=========================
	
	/** This method notifies listeners of a tree change. */
	private void notifyTreeChange(TreeMap<OsmWay,List<OsmRelation>> newTreeMap) {
		for(LevelStructureListener listener:levelStructureListeners) {
			listener.levelStructureChanged(newTreeMap);
		}
	}
	
	/** This method adds a structure and level to a tree map collection. */
	private void addToMap(TreeMap<OsmWay,List<OsmRelation>> treeMap, 
			OsmWay structure, OsmRelation level) {
		List<OsmRelation> levels = treeMap.get(structure);
		if(levels == null) {
			levels = new ArrayList<OsmRelation>();
			treeMap.put(structure,levels);
		}
		levels.add(level);
	}
	
	/** This method takes the formatted new tree map and compares it to the 
	 * current tree map. It returns true if they are different, meaning the
	 * tree should be updated. 
	 * 
	 * @param newTreeMap		The tree map for the new data
	 * @return					True if the UI tree element should be updated.
	 */
	private boolean treeUpdated(TreeMap<OsmWay,List<OsmRelation>> newTreeMap) {
		if(activeTreeMap == null) return true;
		
		//check the way count matches
		Set<OsmWay> activeWays = activeTreeMap.keySet();
		Set<OsmWay> newWays = newTreeMap.keySet();
		if(activeWays.size() != newWays.size()) return true;
		
		//check all way levels in new list match way levels in active list
		List<OsmRelation> activeList;
		List<OsmRelation> newList;
		for(OsmWay way:newTreeMap.keySet()) {
			activeList = activeTreeMap.get(way);
			if(activeList == null) return true;
			newList = newTreeMap.get(way);
			if(newList.size() != activeList.size()) return true;
			
			for(int i = 0; i < newList.size(); i++) {
				if(activeList.get(i) != newList.get(i)) return true;
			}
		}
		
		//lists match
		return false;
	}
	

	/** This is a comparator to order ways by id, with smaller positive first
	 * and negative last
	 */
	public class WayComparator implements Comparator<OsmWay> {
		public int compare(OsmWay way1, OsmWay way2) {
			//remove sign bit, making negative nubmers larger than positives (with small enough negatives)
			Long modId1 = way1.getId() & Long.MAX_VALUE;
			Long modId2 = way2.getId() & Long.MAX_VALUE;
			return modId1.compareTo(modId2);
		}
	}
	
	/** This is a comparator to order levels by zlevel. */
	public class LevelComparator implements Comparator<OsmRelation> {
		public int compare(OsmRelation levelA, OsmRelation levelB) {
			int zlevel1 = levelA.getIntProperty(OsmModel.KEY_ZLEVEL,0);
			int zlevel2 = levelB.getIntProperty(OsmModel.KEY_ZLEVEL,0);
			return zlevel1 - zlevel2;
		}
	}
}
