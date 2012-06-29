package intransix.osm.termite.map.data;

/**
 * This is an update instruction.
 * 
 * @author sutter
 */
public class UpdateInstruction<T extends OsmObject> extends EditInstruction {
	
	//========================
	// Properties
	//========================
	
	private T osmObject;
	private EditData<T> targetData;
	private EditData<T> initialData;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor. */
	public UpdateInstruction(T objectToUpdate, EditData<T> targetData) {
		this.osmObject = objectToUpdate;
		this.targetData = targetData;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method executes the instruction. */
	@Override
	void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		initialData = executeUpdate(osmData,osmObject,targetData,editNumber);
	}
	
	/** This method undoes the instruction. */
	@Override
	void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		targetData = executeUpdate(osmData,osmObject,initialData,editNumber);
	}
}
