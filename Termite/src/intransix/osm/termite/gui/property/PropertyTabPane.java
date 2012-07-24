package intransix.osm.termite.gui.property;

import intransix.osm.termite.map.data.OsmObject;
import intransix.osm.termite.map.data.OsmRelation;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.gui.FeatureSelectedListener;
import intransix.osm.termite.gui.LevelSelectedListener;
import java.util.List;
import javax.swing.JTabbedPane;


/**
 *
 * @author sutter
 */
public class PropertyTabPane extends JTabbedPane implements LevelSelectedListener, FeatureSelectedListener {
	
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
	
	/** This method is called when a map feature is selected. The arguments selectionType
	 * and wayNodeType indicate the type of selection made. The list objects may be null
	 * if there is no selection for the list. 
	 * 
	 * @param selection			A list of the selected objects
	 * @param selectionType		The type objects objects in the selection
	 * @param wayNodeSelection	If the selection is a single way, this is a possible list
	 *							of selected nodes within the way.
	 * @param wayNodeType		This is the type of way nodes selected for the way, if applicable.
	 */
	public void onFeatureSelected(List<Object> selection, 
			FeatureSelectedListener.SelectionType selectionType,
			List<Integer> wayNodeSelection,
			FeatureSelectedListener.WayNodeType wayNodeType) {
//implement this		
	}
	
}
