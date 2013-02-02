package intransix.osm.termite.app.edit.impl;

import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.app.mapdata.instruction.EditInstruction;
import intransix.osm.termite.app.mapdata.instruction.UpdateObjectProperty;
import intransix.osm.termite.app.mapdata.instruction.EditAction;
import intransix.osm.termite.app.mapdata.instruction.UpdateInstruction;

/**
 *
 * @author sutter
 */
public class PropertyEdit extends EditOperation {
	
	public PropertyEdit(MapDataManager mapDataManager) {
		super(mapDataManager);
	}
	
	/** This method edits a property for an object.
	 * 
	 * @param osmObject		The object
	 * @param initialKey	The initial value of the key. Set to null to create a property.
	 * @param targetKey		The key for the property after the edit
	 * @param targetValue	The final value for the property after the edit
	 * @return 
	 */
	public boolean editProperty(OsmObject osmObject, String initialKey, 
			String targetKey, String targetValue) {
		System.out.println("Edit object property");

		EditAction action = new EditAction(getMapDataManager(),"Edit object property");

		try {
			UpdateObjectProperty uop = new UpdateObjectProperty(initialKey,targetKey,targetValue);
			EditInstruction instr = new UpdateInstruction(osmObject,uop);
			action.addInstruction(instr);
			
			boolean success = action.doAction();
			if(success) {
				return true;
			}
			else {
				reportError(action.getDesc());
				return false;
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			reportFatalError(action.getDesc(),ex.getMessage());
			return false;
		}
	}
}
