package intransix.osm.termite.gui.contenttree;

import intransix.osm.termite.gui.*;
import intransix.osm.termite.map.data.*;
import java.util.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

/**
 *
 * @author sutter
 */
public class ContentTree extends javax.swing.JTree 
		implements LevelSelectedListener, MapDataListener, TreeSelectionListener {
	
	private final static String ROOT_NAME = "Content";
	private final static String OUTDOORS_NAME = "Outdoors";
	
	private TermiteGui gui;
	private OsmData mapData;
	private TreeMap<OsmWay,List<OsmRelation>> activeTreeMap = null;

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
	
	private void clearTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(ROOT_NAME);
		this.setModel(new DefaultTreeModel(rootNode));
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
	
	private void populateStructures(DefaultMutableTreeNode rootNode, TreeMap<OsmWay,List<OsmRelation>> treeMap) {
		for(OsmWay structure:treeMap.keySet()) {
			DefaultMutableTreeNode structureNode = new DefaultMutableTreeNode(new StructureWrapper(structure));
			populateLevels(structureNode,structure, treeMap.get(structure));
			rootNode.add(structureNode);
		}
	}
	
	private void populateLevels(DefaultMutableTreeNode structureNode, OsmWay structure, List<OsmRelation> levels) {
		for(OsmRelation level:levels) {
			DefaultMutableTreeNode levelNode = new DefaultMutableTreeNode(new LevelWrapper(level,structure));
			structureNode.add(levelNode);
		}
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
