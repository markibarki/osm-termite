package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmData;
import intransix.osm.termite.map.osm.OsmObject;

/**
 *
 * @author sutter
 */
public class CreateInstruction<T extends OsmObject> extends EditInstruction {
	
	private T copyOfObjectToCreate;
	
	public CreateInstruction(T copyOfObjectToCreate, TermiteData termiteData) {
		this.copyOfObjectToCreate = copyOfObjectToCreate;
		OsmData osmData = termiteData.getWorkingData();
		osmData.applyNextId(copyOfObjectToCreate);
	}
	
	public void doInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeCreate(termiteData,copyOfObjectToCreate);
	}
	
	public void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeDelete(termiteData,copyOfObjectToCreate);
	}

}
