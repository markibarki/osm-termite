package intransix.osm.termite.gui.property;

import intransix.osm.termite.map.osm.OsmObject;
import intransix.osm.termite.map.osm.OsmRelation;
import intransix.osm.termite.map.osm.OsmWay;
import intransix.osm.termite.gui.FeatureSelectedListener;
import intransix.osm.termite.gui.LevelSelectedListener;
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
	
	/** This method is called when a map feature is selected. It may be called 
	 * with the value null if a selection is cleared an no new selection is made. 
	 * 
	 * @param feature	The selected map feature
	 */
	@Override
	public void onFeatureSelected(OsmObject feature) {
//implement this		
	}
	
}
