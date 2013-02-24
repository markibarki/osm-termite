/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.app.edit.impl.DeleteSelection;
import java.util.List;

/**
 *
 * @author sutter
 */
public class DeleteSelectionAction {
	
	private EditManager editManager;
	
	public DeleteSelectionAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	public void deleteSelection() {
		List<Object> selection = editManager.getSelection();
		MapDataManager mapDataManager = editManager.getOsmData();
	
		//works on a node or way or a collection of nodes and ways
		if(!selection.isEmpty()) {
			DeleteSelection ds = new DeleteSelection(mapDataManager);
			ds.deleteSelection(selection);
			
			//clear the selection
			editManager.clearSelection();
		}
		
//		editManager.getEditLayer().notifyContentChange();
	}
}
