package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.map.data.*;
import intransix.osm.termite.map.data.edit.RemoveWayNodeEdit;
import java.util.List;

/**
 *
 * @author sutter
 */
public class RemoveWayNodeAction {
	
	private EditManager editManager;
	
	public RemoveWayNodeAction(EditManager editManager) {
		this.editManager = editManager;
	}
	public void removeNodeFromWay() {
		
		List<Object> selection = editManager.getSelection();
		List<Integer> selectedWayNodes = editManager.getSelectedWayNodes();
		OsmData osmData = editManager.getOsmData();
		
		//works on a node selected within a way
		if(osmData != null) {
			if((!selection.isEmpty())&&(!selectedWayNodes.isEmpty())) {
				Object obj = selection.get(0);
				if(obj instanceof OsmWay) {
					RemoveWayNodeEdit rwne = new RemoveWayNodeEdit(osmData);
					rwne.removeNodesFromWay((OsmWay)obj,selectedWayNodes);
				}
				
				editManager.clearWayNodesSelection();
			}
		}
	}
	
}
