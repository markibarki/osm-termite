package intransix.osm.termite.gui.property;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.app.edit.FeatureSelectedListener;
import intransix.osm.termite.app.level.LevelSelectedListener;
import intransix.osm.termite.app.mapdata.MapDataListener;
import intransix.osm.termite.app.mapdata.MapDataManager;
import java.util.List;
import javax.swing.JTabbedPane;

/**
 *
 * @author sutter
 */
public class PropertyTabPane extends JTabbedPane implements 
		LevelSelectedListener, FeatureSelectedListener,
		MapDataListener {
	
	private final static String STRUCTURE_LABEL = "Structure";
	private final static String LEVEL_LABEL = "Level";
	private final static String FEATURE_LABEL = "Feature";
	
	private PropertyPage structurePage;
	private boolean structureActive = false;
	private PropertyPage levelPage;
	private boolean levelActive = false;
	private PropertyPage featurePage;
	private boolean featureActive = false;
	
	public PropertyTabPane() {
		featurePage = new PropertyPage();
		levelPage = new PropertyPage();
		structurePage = new PropertyPage();
	}
	
	public void setMapDataManager(MapDataManager mapDataManager) {
		structurePage.setMapDataManager(mapDataManager);
		levelPage.setMapDataManager(mapDataManager);
		featurePage.setMapDataManager(mapDataManager);
		
		if(mapDataManager != null) {
			mapDataManager.addMapDataListener(this);
		}
	}
	
	/** This method is called when the map data is set of cleared. It will be called 
	 * with the value null when the data is cleared. 
	 * 
	 * @param mapData	The map data object
	 */
	@Override
	public void onMapData(boolean dataPresent) {
		//no action - should be no properties to set yet
	}
	
	/** This method is called when the data has changed.
	 * 
	 * @param editNumber	This is the data version that will be reflected in any data changed 
	 *						by this edit action.
	 */
	@Override
	public void osmDataChanged(int editNumber) {
		if(structureActive) structurePage.updateProperties();
		if(levelActive) levelPage.updateProperties();
		if(featureActive) featurePage.updateProperties();
	}		
	
	/** This method is called when a map level is selected. It may be called 
	 * with the value null for the level or the level and the structure. 
	 * 
	 * @param structure		The footprint in the outdoor map for the selected level
	 * @param level			The selected level
	 */
	@Override
	public void onLevelSelected(OsmWay structure, OsmRelation level) {
		if(structure != null) {
			structurePage.setObject(structure);
			if(!structureActive) {
				addTab(STRUCTURE_LABEL,structurePage);
				structureActive = true;
			}
		}
		else {
			if(structureActive) {
				removeTab(STRUCTURE_LABEL);
				structurePage.setObject(null);
				structureActive = false;
			}
		}
		
		if(level != null) {
			levelPage.setObject(level);
			if(!levelActive) {
				addTab(LEVEL_LABEL,levelPage);
				levelActive = true;
			}
		}
		else {
			if(levelActive) {
				removeTab(LEVEL_LABEL);
				levelPage.setObject(null);
				levelActive = false;
			}
		}
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
	@Override
	public void onFeatureSelected(List<Object> selection,List<Integer> wayNodeSelection) {

		//for now, only support single selection of properties
		OsmObject osmObject = null;
		if(selection.size() == 1) {
			Object object = selection.get(0);
			if(object instanceof OsmObject) {
				osmObject = (OsmObject)object;
			}
		}
		
		if(osmObject != null) {
			featurePage.setObject(osmObject);
			if(!featureActive) {
				this.addTab(FEATURE_LABEL,featurePage);
				featureActive = true;
				selectActiveTab(FEATURE_LABEL);
			}
		}
		else {
			if(featureActive) {
				removeTab(FEATURE_LABEL);
				featurePage.setObject(null);
				featureActive = false;
			}
		}	
	}
	
	private void removeTab(String title) {
		int index = indexOfTab(title);
		if(index >= 0) {
			this.removeTabAt(index);
		}
	}
	
	private void selectActiveTab(String title) {
		int index = indexOfTab(title);
		if(index >= 0) {
			this.setSelectedIndex(index);
		}
	}
	
}
