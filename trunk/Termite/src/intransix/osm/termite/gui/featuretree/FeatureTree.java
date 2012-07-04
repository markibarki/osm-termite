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
 *
 * @author sutter
 */
public class FeatureTree extends javax.swing.JTree 
		implements FeatureLayerListener, TreeSelectionListener {

	private TermiteGui gui;
	private FeatureInfoMap featureInfoMap;
	private FeatureInfo selectedFeatureInfo;
	
	
	/**
	 * Creates new form FeatureTree
	 */
	public FeatureTree(TermiteGui gui) {
		this.gui = gui;
		this.setRootVisible(true);
		this.addTreeSelectionListener(this);
		this.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}
	
	public void setFeatureInfoMap(FeatureInfoMap featureInfoMap) {
		this.featureInfoMap = featureInfoMap;
		createTree();
	}
	
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
	// Private 
	//==========================
	
	private void createTree() {
		PropertyNode<Object,FeatureInfo> root = featureInfoMap.getRoot();
		DefaultMutableTreeNode rootTreeNode = new DefaultMutableTreeNode(root);
		addChildKeys(rootTreeNode,root);
		TreeModel model = new DefaultTreeModel(rootTreeNode);
		this.setModel(model);
	}
	
	private void addChildKeys(DefaultMutableTreeNode treeNode, PropertyNode<Object,FeatureInfo> propertyNode) {
		for(KeyNode<Object,FeatureInfo> key:propertyNode.getKeys()) {
			DefaultMutableTreeNode keyTreeNode = new DefaultMutableTreeNode(key);
			addChildProperties(keyTreeNode,key);
			treeNode.add(keyTreeNode);
		}
	}
	
	private void addChildProperties(DefaultMutableTreeNode treeNode, KeyNode<Object,FeatureInfo> keyNode) {
		for(PropertyNode<Object,FeatureInfo> prop:keyNode.getValues()) {
			DefaultMutableTreeNode propTreeNode = new DefaultMutableTreeNode(prop);
			addChildKeys(propTreeNode,prop);
			treeNode.add(propTreeNode);
		}
	}

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
