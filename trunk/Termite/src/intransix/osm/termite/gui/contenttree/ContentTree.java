package intransix.osm.termite.gui.contenttree;

import intransix.osm.termite.gui.*;
import intransix.osm.termite.map.data.*;
import java.util.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 * This control is used to filter the data by level. It displays all the levels
 * defined in the current data set and it has an icon to display data not on 
 * a level, in other words "normal" outdoor data.
 * 
 * @author sutter
 */
public class ContentTree extends javax.swing.JTree 
		implements LevelSelectedListener, MapDataListener, TreeSelectionListener {
	
	//====================
	// Private Properties
	//====================
	
	private final static String ROOT_NAME = "Content";
	private final static String OUTDOORS_NAME = "Outdoors";
	
	private TermiteGui gui;
	private OsmData mapData;
	private TreeMap<OsmWay,List<OsmRelation>> activeTreeMap = null;

	//====================
	// Public Methods
	//====================
	
	/**
	 * Creates new form ContentTree
	 */
	public ContentTree(TermiteGui gui) {
		this.gui = gui;
		this.setRootVisible(false);
		this.addTreeSelectionListener(this);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setCellRenderer(new ContentTreeRenderer());
		clearTree();
	}
	
	/** This method is called when the map data is set or cleared. It should be called 
	 * with the value null when the data is cleared. It should be called from the
	 * UI thread.
	 * 
	 * @param feature	The selected map feature
	 */
	@Override
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
	@Override
	public void onLevelSelected(OsmWay structure, OsmRelation level) {
//implement this
	}
	
	/** This method should be called when the map data is updated, from the UI thread. */
	public void mapDataUpdated() {
		if(mapData != null) {
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
			//sort the levels for each structure
			LevelComparator lc = new LevelComparator();
			for(List<OsmRelation> rs:newTreeMap.values()) {
				Collections.sort(rs, lc);
			}
			//update the list (if needed)
			if(doTreeMapUpdate(newTreeMap)) {
				updateTree(newTreeMap);
			}
		}
		else {
			//clear the tree if there is no data
			clearTree();
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent event) {
		TreePath tp = event.getNewLeadSelectionPath();
		if(tp != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			Object data = node.getUserObject();
			if(data instanceof StructureWrapper) {
				gui.setSelectedLevel(((StructureWrapper)data).structure,null);
			}
			else if(data instanceof LevelWrapper) {
				gui.setSelectedLevel(((LevelWrapper)data).structure,((LevelWrapper)data).level);
			}
			else {
				gui.setSelectedLevel(null,null);
			}
		}
		else {
//I'm not sure what to do here
			gui.setSelectedLevel(null,null);
		}
	}
	
	//====================
	// Private Methods
	//====================
	
	/** This method clears the tree, taking away all the levels and the outdoor
	 * icon too. */
	private void clearTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(ROOT_NAME);
		this.setModel(new DefaultTreeModel(rootNode));
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
	private boolean doTreeMapUpdate(TreeMap<OsmWay,List<OsmRelation>> newTreeMap) {
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
	
	/** This method updates the UI tree to match the passed data. 
	 * 
	 * @param newTreeMap		The new tree map data 
	 */
	private void updateTree(TreeMap<OsmWay,List<OsmRelation>> newTreeMap) {
		//create the root node
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(ROOT_NAME);
		//create the outdoor node
		DefaultMutableTreeNode outdoorNode = new DefaultMutableTreeNode(OUTDOORS_NAME);
		rootTreeNode.add(outdoorNode);
		//add the nodes for the structures
		populateStructures(rootTreeNode,newTreeMap);
		//update the tree
		activeTreeMap = newTreeMap;
		TreeModel model = new DefaultTreeModel(rootTreeNode);
		setModel(model);
	}
	
	/** This method populates the structure tree nodes under the root element.
	 * 
	 * @param rootNode	The root node of the tree.
	 * @param treeMap	The data for the tree
	 */
	private void populateStructures(DefaultMutableTreeNode rootNode, TreeMap<OsmWay,List<OsmRelation>> treeMap) {
		for(OsmWay structure:treeMap.keySet()) {
			DefaultMutableTreeNode structureNode = new DefaultMutableTreeNode(new StructureWrapper(structure));
			populateLevels(structureNode,structure, treeMap.get(structure));
			rootNode.add(structureNode);
		}
	}
	
	/** This method populates the level nodes for a given structure. 
	 * 
	 * @param structureNode		The structure tree node to populate
	 * @param structure			The structure object
	 * @param levels			The list of levels for this structure.
	 */
	private void populateLevels(DefaultMutableTreeNode structureNode, OsmWay structure, List<OsmRelation> levels) {
		for(OsmRelation level:levels) {
			DefaultMutableTreeNode levelNode = new DefaultMutableTreeNode(new LevelWrapper(level,structure));
			structureNode.add(levelNode);
		}
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
	
	/** This is a wrapper class to hold structure objects as payload in a tree node. */
	public class StructureWrapper {
		public String label;
		public OsmWay structure; 
		
		public StructureWrapper(OsmWay structure) {
			this.structure = structure;
			createLabel();
		}
		
		public String toString() {
			return label;
		}
		
		private void createLabel() {
			String name = structure.getProperty(OsmModel.KEY_NAME);
			if(name != null) {
				label = name;
			}
			else {
				label = "Way " + structure.getId();
			}
		}
	}
	
	/** This is a wrapper class to hold level objects as payload in a tree node. */
	public class LevelWrapper {
		public String label;
		public OsmRelation level; 
		public OsmWay structure;
		
		public LevelWrapper(OsmRelation level, OsmWay structure) {
			this.level = level;
			this.structure = structure;
			createLabel();
		}
		
		public String toString() {
			return label;
		}
		
		private void createLabel() {
			String name = level.getProperty(OsmModel.KEY_NAME);
			String zlevel = level.getProperty(OsmModel.KEY_ZLEVEL);
			label = "";
			if(name != null) {
				label = "Level " + name;
			}
			if(zlevel != null) {
				if(label.length() > 0) {
					label += "; ";
				}
				label += "Zlevel = " + zlevel;
			}
			if(label.length() == 0) {
				label = "Level ID " + level.getId();
			}
		}
	}
}
