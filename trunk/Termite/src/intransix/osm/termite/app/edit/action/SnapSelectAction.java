/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.edit.action;

import intransix.osm.termite.app.edit.EditManager;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SnapSelectAction {
	
	private EditManager editManager;
	
	public SnapSelectAction(EditManager editManager) {
		this.editManager = editManager;
	}
	
	public void nextSnapOject() {
		List<SnapObject> snapObjects = editManager.getSnapObjects();

		if(!snapObjects.isEmpty()) {
			int activeSnapObject = editManager.getActiveSnapObject();
			activeSnapObject--;
			if(activeSnapObject < -1) activeSnapObject = snapObjects.size() - 1;
			editManager.setActiveSnapObject(activeSnapObject);
		}
	}
	
	public void previousSnapObject() {
		List<SnapObject> snapObjects = editManager.getSnapObjects();
		
		if(!snapObjects.isEmpty()) {
			int activeSnapObject = editManager.getActiveSnapObject();
			activeSnapObject++;
			if(activeSnapObject >= snapObjects.size()) activeSnapObject = -1;
			editManager.setActiveSnapObject(activeSnapObject);
		}
	}
}
