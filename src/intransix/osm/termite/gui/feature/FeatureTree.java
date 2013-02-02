/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.feature;

import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 *
 * @author sutter
 */
public class FeatureTree extends TreeView {
	
	public FeatureTree() {
	}
	
	public void init() {
		TreeItem<String> rootItem = new TreeItem<String> ("Key");
        rootItem.setExpanded(true);
        for (int i = 1; i < 6; i++) {
            TreeItem<String> item = new TreeItem<String> ("Value  " + i);            
            rootItem.getChildren().add(item);
        }  
		setRoot(rootItem);
	}
}
