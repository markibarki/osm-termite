package intransix.osm.termite.gui.featuretree;

import intransix.osm.termite.map.feature.*;
import intransix.osm.termite.map.proptree.*;
import intransix.osm.termite.gui.FeatureLayerListener;
import intransix.osm.termite.gui.TermiteGui;
import javax.swing.tree.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.util.ArrayList;

/**
 * This is a UI control to display the recognized feature types. It allows the
 * user to select a feature type which can be applied to the newly drawn objects.
 * The feature types are defined from the feature info map, which associates
 * properties with object types, such as highway:primary.
 * 
 * @author sutter
 */
public class FeatureTree extends javax.swing.JTree 
		implements FeatureLayerListener, TreeSelectionListener {

	//=======================
	// Properties
	//=======================
	
	private TermiteGui gui;
	private FeatureInfoMap featureInfoMap;
	private FeatureInfo selectedFeatureInfo;
	
	//=======================
	// Public Methods
	//=======================
	
	/**
	 * Creates new form FeatureTree
	 */
	public FeatureTree(TermiteGui gui) {
		this.gui = gui;
		this.setRootVisible(true);
		this.addTreeSelectionListener(this);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.setCellRenderer(new FeatureTreeRenderer());
	}
	
	/** This method sets the feature info map, which defines the tree content. */
	public void setFeatureInfoMap(FeatureInfoMap featureInfoMap) {
		this.featureInfoMap = featureInfoMap;
		createTree();
	}
	
	//------------------
	//FeatureLayerListener interface
	//------------------
	
	/** This method is called when a map feature is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param feature	The selected map feature
	 */
	@Override
	public void onFeatureLayerSelected(FeatureInfo featureInfo) {
		if(featureInfo != selectedFeatureInfo) {
			this.selectNode(featureInfo);
		}
	}
	
	//------------------
	//TreeSelectionListener interface
	//------------------
	
	/** This is called when the tree selection changes. It updates the active 
	 * feature info layer for the application.  */
	@Override
	public void valueChanged(TreeSelectionEvent event) {
		TreePath tp = event.getNewLeadSelectionPath();
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tp.getLastPathComponent();
		Object data = node.getUserObject();
		if(data instanceof PropertyNode) {
			Object fi = ((PropertyNode)data).getData();
			if(fi instanceof FeatureInfo) {
				selectedFeatureInfo = (FeatureInfo)fi;
				gui.setSelectedFeatureLayer((FeatureInfo)fi);
			}
		}
		else {
			selectedFeatureInfo = null;
			if(data instanceof KeyNode) {
				TreeNode parent = node.getParent();
				TreePath parentPath = getPath(parent);
				this.setSelectionPath(parentPath);
			}
		}
	}
	
	/** This method creates a label for each node in the tree. */
	@Override
	public String convertValueToText(Object value,
                        boolean selected,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {
		if(value instanceof DefaultMutableTreeNode) {
			Object data = ((DefaultMutableTreeNode)value).getUserObject();
			if(data instanceof PropertyNode) {
				return ((PropertyNode)data).getName();
			}
			else if(data instanceof KeyNode) {
				return ((KeyNode)data).getName();
			}
			else {
				return data.toString();
			}
		}
		else {
			return value.toString();
		}
	}
	
	//==========================
	// Private Methods
	//==========================
	
	/** This method creates the tree. */
	private void createTree() {
		PropertyNode<Object,FeatureInfo> root = featureInfoMap.getRoot();
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(root);
		addChildKeys(rootTreeNode,root);
		TreeModel model = new DefaultTreeModel(rootTreeNode);
		this.setModel(model);
	}
	
	/** This method adds key tree nodes to a property tree node. */
	private void addChildKeys(DefaultMutableTreeNode treeNode, PropertyNode<Object,FeatureInfo> propertyNode) {
		for(KeyNode<Object,FeatureInfo> key:propertyNode.getKeys()) {
			DefaultMutableTreeNode keyTreeNode = new DefaultMutableTreeNode(key);
			addChildProperties(keyTreeNode,key);
			treeNode.add(keyTreeNode);
		}
	}
	
	/** This method adds property tree nodes to a key tree node. */
	private void addChildProperties(DefaultMutableTreeNode treeNode, KeyNode<Object,FeatureInfo> keyNode) {
		for(PropertyNode<Object,FeatureInfo> prop:keyNode.getValues()) {
			DefaultMutableTreeNode propTreeNode = new DefaultMutableTreeNode(prop);
			addChildKeys(propTreeNode,prop);
			treeNode.add(propTreeNode);
		}
	}

	/** This method creates a tree path for the given tree node. */
	private TreePath getPath(TreeNode node) {
		ArrayList<TreeNode> path = new ArrayList<TreeNode>();
		while(node != null) {
			path.add(0,node);
			node = node.getParent();
		}
		Object[] pathArray = path.toArray();
		TreePath treePath = new TreePath(pathArray);
		return treePath;
	}
	
	/** This method selects a tree node given a feature info object. */
	private boolean selectNode(FeatureInfo featureInfo) {
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)getModel().getRoot();
		DefaultMutableTreeNode featureNode = getFeatureNode(rootNode,featureInfo);
		if(featureNode != null) {
			TreePath treePath = getPath(featureNode);
			this.setSelectionPath(treePath);
			return true;
		}
		else {
			return false;
		}
	}
	
	/** This method gets a tree node for the associated feature inof that is a
	 * child to the passed node. It is used for a recursive search.
	 * 
	 * @param node				A parent node that will be searched
	 * @param featureInfo		The feature info object whose node is being searched
	 * @return					The node with this feature info, if it is found under 
	 *							this node. Otherwise null.
	 */
	private DefaultMutableTreeNode getFeatureNode(DefaultMutableTreeNode node, FeatureInfo featureInfo) {
		if(node.getUserObject() == featureInfo) return node;
		
		int cnt = node.getChildCount();
		for(int i = 0; i < cnt; i++) {
			DefaultMutableTreeNode childNode = (DefaultMutableTreeNode)node.getChildAt(i);
			DefaultMutableTreeNode featureNode = getFeatureNode(childNode,featureInfo);
			if(featureNode != null) return featureNode;
		}
		return null;
	}

}
