package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.feature.FeatureTypeManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.edit.TypeChangeEdit;
import java.util.List;
import javax.swing.JOptionPane;

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
		OsmData osmData = editManager.getOsmData();
		
		if(!selection.isEmpty()) {
			TypeChangeEdit tce = new TypeChangeEdit(osmData);
			tce.modifyType(selection,featureInfo);
		}
		else {
			JOptionPane.showMessageDialog(null,"An object must be selected.");
		}
	}
}
