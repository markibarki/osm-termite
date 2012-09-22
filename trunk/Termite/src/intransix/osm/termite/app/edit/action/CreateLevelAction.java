package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.gui.dialog.CreateLevelDialog;
import intransix.osm.termite.map.data.OsmData;
import intransix.osm.termite.map.data.OsmObject;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author sutter
 */
public class CreateLevelAction {
	
	private EditManager editManager;
	
	public CreateLevelAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	public void createLevel() {
		List<Object> selection = editManager.getSelection();
		OsmData osmData = editManager.getOsmData();
		
		if(selection.size() == 1) {
			Object parent = selection.get(0);
			if(parent instanceof OsmObject) {
				CreateLevelDialog cld = new CreateLevelDialog(null,osmData,(OsmObject)parent);
				cld.setVisible(true);
			}
			else {
				JOptionPane.showMessageDialog(null,"Invalid object type for creating a level.");
			}
		}
		else {
			JOptionPane.showMessageDialog(null,"A single object must be selected to create a level.");
		}
	}
}
