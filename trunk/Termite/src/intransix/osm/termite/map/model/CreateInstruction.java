package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmData;
import intransix.osm.termite.map.osm.OsmObject;

/**
 *
 * @author sutter
 */
public class CreateInstruction<T extends OsmObject> extends EditInstruction {
	
	private T osmObject;
	
	public CreateInstruction(T copyOfObjectToCreate, TermiteData termiteData) {
		this.osmObject = copyOfObjectToCreate;
		OsmData osmData = termiteData.getWorkingData();
		osmData.applyNextId(osmObject);
	}
	
	public void doInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeCreate(termiteData,osmObject);
	}
	
	public void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeDelete(termiteData,osmObject);
	}

}
