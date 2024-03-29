package intransix.osm.termite.app.mapdata.instruction;

import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;

/**
 * This is an update instruction.
 * 
 * @author sutter
 */
public class UpdateInstruction<T extends OsmObject> extends EditInstruction {
	
	//========================
	// Properties
	//========================
	
	private long objectId;
	private String objectType;
	private EditData<T> targetData;
	private EditData<T> initialData;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor. */
	public UpdateInstruction(T objectToUpdate, EditData<T> targetData) {
		this.objectId = objectToUpdate.getId();
		this.objectType = objectToUpdate.getObjectType();
		this.targetData = targetData;
	}
	
	public UpdateInstruction(long objectId, String objectType, EditData<T> targetData) {
		this.objectId = objectId;
		this.objectType = objectType;
		this.targetData = targetData;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method executes the instruction. */
	@Override
	void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		OsmObject osmObject = osmData.getOsmObject(objectId, objectType, false);
		if(osmObject == null) throw new UnchangedException("Object not found");
		
		initialData = executeUpdate(osmData,osmObject,targetData,editNumber);
	}
	
	/** This method undoes the instruction. */
	@Override
	void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		OsmObject osmObject = osmData.getOsmObject(objectId, objectType, false);
		if(osmObject == null) throw new UnchangedException("Object not found");
		
		targetData = executeUpdate(osmData,osmObject,initialData,editNumber);
	}
}
