package intransix.osm.termite.gui.contenttree;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.app.level.LevelSelectedListener;
import intransix.osm.termite.app.level.LevelStructureListener;
import intransix.osm.termite.gui.*;
import java.util.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import intransix.osm.termite.app.level.LevelManager;

/**
 * This control is used to filter the data by level. It displays all the levels
 * defined in the current data set and it has an icon to display data not on 
 * a level, in other words "normal" outdoor data.
 * 
 * @author sutter
 */
public class ContentTree extends javax.swing.JTree 
		implements LevelSelectedListener, LevelStructureListener, TreeSelectionListener {
	
	//====================
	// Private Properties
	//====================
	
//	private final static String ROOT_NAME = "Map Data";
	private final static String OUTDOORS_NAME = "Map Data";
	
	private TermiteGui gui;
	private OsmData mapData;
	
	private LevelManager levelManager;
	private TreeMap<OsmWay,List<OsmRelation>> activeTreeMap = null;

	//====================
	// Public Methods
	//====================
	
	/**
	 * Creates new form ContentTree
	 */
	public ContentTree(TermiteGui gui) {
		this.gui = gui;
		this.setRootVisible(true);
		this.addTreeSelectionListener(this);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setCellRenderer(new ContentTreeRenderer());
		clearTree();
	}
	
	/** This method connects the UI element to the level manager. */
	public void setLevelManager(LevelManager levelManager) {
		this.levelManager = levelManager;
		
		//load the tree
		TreeMap<OsmWay,List<OsmRelation>> treeMap = levelManager.getTreeMap();
		this.levelStructureChanged(treeMap);
		
		//set the selection
		OsmWay structure = levelManager.getSelectedStructure();
		OsmRelation level = levelManager.getSelectedLevel();
		this.onLevelSelected(structure,level);
		
		levelManager.addLevelStructureListener(this);
		levelManager.addLevelSelectedListener(this);		
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
	
	/** This method is called when the level structure for the data changes. 
	 * 
	 * @param treeMap	A mapping of level relations to structure footprints. 
	 */
	@Override
	public void levelStructureChanged(TreeMap<OsmWay,List<OsmRelation>> treeMap) {
		//create the root node
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(OUTDOORS_NAME);
//		//create the outdoor node
//		DefaultMutableTreeNode outdoorNode = new DefaultMutableTreeNode(OUTDOORS_NAME);
//		rootTreeNode.add(outdoorNode);
		//add the nodes for the structures
		populateStructures(rootTreeNode,treeMap);
		//update the tree
		activeTreeMap = treeMap;
		TreeModel model = new DefaultTreeModel(rootTreeNode);
		setModel(model);
		
		//for now, always set the outside when the structure changes
		this.setSelectionRow(0);
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent event) {
		TreePath tp = event.getNewLeadSelectionPath();
		if(tp != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
			Object data = node.getUserObject();
			if(data instanceof StructureWrapper) {
				levelManager.setSelectedLevel(((StructureWrapper)data).structure,null);
			}
			else if(data instanceof LevelWrapper) {
				levelManager.setSelectedLevel(((LevelWrapper)data).structure,((LevelWrapper)data).level);
			}
			else {
				levelManager.setSelectedLevel(null,null);
			}
		}
		else {
//I'm not sure what to do here
			levelManager.setSelectedLevel(null,null);
		}
	}
	
	//====================
	// Private Methods
	//====================
	
	/** This method clears the tree, taking away all the levels and the outdoor
	 * icon too. */
	private void clearTree() {
		DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(OUTDOORS_NAME);
		this.setModel(new DefaultTreeModel(rootNode));
	}

	
	/** This method populates the structure tree nodes under the root element.
	 * 
	 * @param rootNode	The root node of the tree.
	 * @param treeMap	The data for the tree
	 */
	private void populateStructures(DefaultMutableTreeNode rootNode, TreeMap<OsmWay,List<OsmRelation>> treeMap) {
		if(treeMap != null) {
			for(OsmWay structure:treeMap.keySet()) {
				DefaultMutableTreeNode structureNode = new DefaultMutableTreeNode(new StructureWrapper(structure));
				populateLevels(structureNode,structure, treeMap.get(structure));
				rootNode.add(structureNode);
			}
		}
		else {
			rootNode.removeAllChildren();
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
