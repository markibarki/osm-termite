package intransix.osm.termite.gui.featuretree;

import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.Component;
import intransix.osm.termite.map.proptree.*;


/**
 * This is a cell renderer for the feature tree.
 * 
 * @author sutter
 */
public class FeatureTreeRenderer extends DefaultTreeCellRenderer {
	
	private final static String RESOURCE_PATH = "/intransix/osm/termite/resources/featuretree/";
	
	ImageIcon keyIcon;
	ImageIcon valueIcon;
	
	public FeatureTreeRenderer() {
		keyIcon = createImageIcon(RESOURCE_PATH + "key.png");
		valueIcon = createImageIcon(RESOURCE_PATH + "value.png");
	}
	
	public Component getTreeCellRendererComponent(
                        JTree tree,
                        Object value,
                        boolean sel,
                        boolean expanded,
                        boolean leaf,
                        int row,
                        boolean hasFocus) {

        super.getTreeCellRendererComponent(
                        tree, value, sel,
                        expanded, leaf, row,
                        hasFocus);
        if(value instanceof DefaultMutableTreeNode) {
			Object data = ((DefaultMutableTreeNode)value).getUserObject();
			if(data instanceof KeyNode) {
				setIcon(keyIcon);
			}
			else if(data instanceof PropertyNode) {
				setIcon(valueIcon);
			}
        }

        return this;
    }
	
	
	private ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = this.getClass().getResource(path);
		if(imgURL != null) {
			return new ImageIcon(imgURL);
		} 
		else {
			return null;
		}
	}
	
}
