package intransix.osm.termite.gui.contenttree;

import javax.swing.ImageIcon;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.Component;


/**
 *
 * @author sutter
 */
public class ContentTreeRenderer extends DefaultTreeCellRenderer {
	
	private final static String RESOURCE_PATH = "/intransix/osm/termite/resources/contenttree/";
	
	ImageIcon outdoorsIcon;
	ImageIcon structureIcon;
	ImageIcon levelIcon;
	
	public ContentTreeRenderer() {
		outdoorsIcon = createImageIcon(RESOURCE_PATH + "globe18.png");
		structureIcon = createImageIcon(RESOURCE_PATH + "building.png");
		levelIcon = createImageIcon(RESOURCE_PATH + "level.png");
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
			if(data instanceof ContentTree.StructureWrapper) {
				setIcon(structureIcon);
			}
			else if(data instanceof ContentTree.LevelWrapper) {
				setIcon(levelIcon);
			}
			else {
				setIcon(outdoorsIcon);
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
