package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmData;
import intransix.osm.termite.map.osm.OsmObject;

/**
 *
 * @author sutter
 */
public class UpdateInstruction<T extends OsmObject> extends EditInstruction {
	
	private T osmObject;
	private EditData<T> targetData;
	private EditData<T> initialData;
	
	public UpdateInstruction(T objectToUpdate, EditData<T> targetData) {
		this.osmObject = objectToUpdate;
		this.targetData = targetData;
	}
	
	public void doInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		initialData = executeUpdate(termiteData,osmObject,targetData);
	}
	
	public void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		targetData = executeUpdate(termiteData,osmObject,initialData);
	}
}
