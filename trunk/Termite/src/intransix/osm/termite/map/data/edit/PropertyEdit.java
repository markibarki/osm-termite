package intransix.osm.termite.map.data.edit;

import intransix.osm.termite.map.data.*;

/**
 *
 * @author sutter
 */
public class PropertyEdit extends EditOperation {
	
	public PropertyEdit(OsmData osmData) {
		super(osmData);
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

		EditAction action = new EditAction(getOsmData(),"Edit object property");

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
