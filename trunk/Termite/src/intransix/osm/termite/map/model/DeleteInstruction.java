package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmData;
import intransix.osm.termite.map.osm.OsmObject;

/**
 *
 * @author sutter
 */
public class DeleteInstruction<T extends OsmObject> extends EditInstruction {
	private T osmObject;
	
	public DeleteInstruction(T objectToDelete) {
		this.osmObject = objectToDelete;
	}
	
	public void doInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeDelete(termiteData,osmObject);
	}
	
	public void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeCreate(termiteData,osmObject);
	}

}
