package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.app.edit.impl.TypeChangeEdit;
import java.util.List;
//import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class ChangeFeatureTypeAction {
	
	private EditManager editManager;
	
	public ChangeFeatureTypeAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	public void changeSelectionFeatureType() {
		List<Object> selection = editManager.getSelection();
		FeatureTypeManager featureTypeManager = editManager.getFeatureTypeManager();
		FeatureInfo featureInfo = featureTypeManager.getActiveFeatureType();
		MapDataManager mapDataManager = editManager.getOsmData();
		
		if(!selection.isEmpty()) {
			TypeChangeEdit tce = new TypeChangeEdit(mapDataManager);
			tce.modifyType(selection,featureInfo);
		}
		else {
//@TODO add a mesage dialog here
//			JOptionPane.showMessageDialog(null,"An object must be selected.");
		}
	}
}
