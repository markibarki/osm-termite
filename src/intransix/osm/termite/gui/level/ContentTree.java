/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.gui.level;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 *
 * @author sutter
 */
public class ContentTree extends TreeView {
	
	private TreeItem rootItem;
	private LevelComparator levelComparator;
	
	public ContentTree() {
		rootItem = new TreeItem<String> ("Map");
        rootItem.setExpanded(true); 
		setRoot(rootItem);
		levelComparator = new LevelComparator();
	}
	
	void setLevels(ArrayList<Level> levels) {
		Collections.sort(levels,levelComparator);
		
		//create an object holding the current level index + level array, structure index + structure array, level index + level array
		
//FIX THIS!! - use method above instead
//		//walk the tree, seeing if we need to add or remove
//		int currentIndex = 0;
//		Level currentLevel = levels.get(currentIndex);
//		
//		ObservableList<TreeItem> structureItems = rootItem.getChildren();
//		for(TreeItem sti:structureItems) {
//			//see if this structure matches the level
//			Structure s = (Structure)sti.getValue();
//			int structureMatch = getStructureMatch(currentLevel.structure,s);
//			if(structureMatch == 0) {
//				ObservableList<TreeItem> levelItems = sti.getChildren();
//				for(TreeItem lti:levelItems) {
//					Level l = (Level)lti.getValue();
//					int levelMatch = getLevelMatch(currentLevel,l);
//					if(levelMatch == 0) {
//						//next level and tree item
//						currentIndex = 0;
//						currentLevel = levels.get(currentIndex);
//					}
//					else if(levelMatch > 0) {
//						//insert new level tree item anead of the current location
//						
//						//next level
//						currentIndex = 0;
//						currentLevel = levels.get(currentIndex);
//					}
//					else {
//						//delete level tree item
//						
//						//next tree item
//					}
//				}
//			}
//			else if(structureMatch > 0) {
//				//insert new structure tree item and level item ahead of our current location
//				
//				//next level
//				currentIndex = 0;
//				currentLevel = levels.get(currentIndex);
//			}
//			else {
//				//delete structure tree item
//				
//				//next structure tree item
//			}
//			
//		}
	}
	
	//these will be defined elsewhere
	static class Structure {
		public String name;
	}
	static class Level {
		public Structure structure;
		public String name;
	}
	
	class LevelComparator implements Comparator<Level> {
		public int compare(Level l1, Level l2) {
			int result;
			
			
			if(l1.structure == l2.structure) {
				//same structure - order based on level name
				return compareNames(l1.name,l2.name);
			}
			else {
				if(l1.structure != null) {
					if(l2.structure != null) {
						//order based on structure name
						return compareNames(l1.structure.name,l2.structure.name);
					}
					else {
						//l1 first
						return 1;
					}
				}
				else if(l2.structure != null) {
					//l2 first
					return -1;
				}
				else {
					//no info
					return 0;
				}
			}
		}
		
		private int compareNames(String name1, String name2) {
			if(name1 != null) {
				if(name2 != null) {
					//compare names
					return name1.compareTo(name2);
				}
				else {
					//name 1
					return 1;
				}
			}
			else if(name2 != null) {
				//name 2
				return -1;
			}
			else {
				//no info
				return 0;
			}
		}
	}
}
