/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.app.edit;

import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.app.edit.snapobject.SnapObject;
import java.util.List;

/**
 *
 * @author sutter
 */
public interface EditObjectChangedListener {
	
	void activeSnapObjectChanged(SnapObject activeSnapObject);
	
	void pendingListChanged(List<EditObject> editObjects);
}
