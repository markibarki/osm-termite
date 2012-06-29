package intransix.osm.termite.map.data;

/**
 * <p>This is the base class for an edit instruction. It is used to edit the data. 
 * The instruction should be placed into an action where it can be executed.</p>
 * 
 * <p>Instructions can throw two types of Exceptions, a UnchangedException and a
 * other Exception. If the instruction throws an UnchangedException, then the instruction
 * did not change the state of the data. The containing action will catch this exception
 * and undo any other instructions that have already been done. The action will then
 * report a failure. This is a recoverable failure. If a other exception is thrown, then
 * the state of the data is indeterminate. In this case, the program should not
 * continue because the data may be corrupted and the behavior wil be unknown for
 * the application and for any data submitted.
 *
 * @author sutter
 */
public abstract class EditInstruction<T extends OsmObject> {
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor */
	public EditInstruction() {
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method executes the instruction. */
	abstract void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception;
	
	/** This method undoes the intruction. */
	abstract void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception;
	
	/** This method executes am update instruction. */
	EditData<T> executeUpdate(OsmData osmData, T osmObject, EditData<T> targetData, int editNumber) 
			throws UnchangedException, Exception {
		
		EditData<T> newInitialData = targetData.readInitialData(osmObject);
		targetData.writeData(osmObject,editNumber);
		
		return newInitialData;
	}
	
	
	/** This method executes a create instruction. */
	void executeCreate(OsmData osmData, OsmSrcData<T> srcData, int editNumber) 
			throws UnchangedException, Exception {
		
		//lookup object
		long id = srcData.getId();
		String objectType = srcData.getObjectType();

		//make sure object doesn't exist
		T osmObject = (T)osmData.getOsmObject(id,objectType,true);
		if(osmObject == null) {
			throw new UnchangedException("The object could not be created: " + objectType + " " + id);
		}
		if(osmObject.getIsLoaded()) {
			throw new UnchangedException("Object already exists: " + objectType + " " + id);
		}

		srcData.copyInto(osmObject,osmData);
		osmObject.setDataVersion(editNumber);
		
		//initialize object
		osmObject.objectCreated(osmData);
	}
	
	
	/** This method executes a delete instruction. */
	void executeDelete(OsmData osmData, OsmSrcData<T> srcData, int editNumber) 
			throws UnchangedException, Exception {
		
		//process the update
		OsmObject osmObject = osmData.getOsmObject(srcData.getId(), srcData.getObjectType(),false);
		
		if(osmObject != null) {
			//verify a delete is OK - this will throw an unchanged exception if this delete
			//is not OK
			osmObject.verifyDelete();

			//if this is a delete, remove the object
			osmData.removeOsmObject(srcData.getId(), srcData.getObjectType());
			
					
			osmObject.objectDeleted(osmData);
		}

	}
}
