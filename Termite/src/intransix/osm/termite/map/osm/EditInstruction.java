package intransix.osm.termite.map.osm;

/**
 *
 * @author sutter
 */
public abstract class EditInstruction<T extends OsmObject> {
	
	public EditInstruction() {
	}
	
	public abstract void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception;
	
	public abstract void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception;
	
	//===============================
	// Private Methods
	//===============================
	
	/** This method executes the instruction, returning the inferred initial data. */
	public EditData<T> executeUpdate(OsmData osmData, T osmObject, EditData<T> targetData, int editNumber) 
			throws UnchangedException, Exception {
		
		EditData<T> newInitialData = targetData.readInitialData(osmObject);
		targetData.writeData(osmObject,editNumber);
		
		return newInitialData;
	}
	
	
	/** This method executes the instruction, returning the inferred initial data. */
	public void executeCreate(OsmData osmData, OsmSrcData<T> srcData, int editNumber) 
			throws UnchangedException, Exception {
		
		//lookup object
		long id = srcData.getId();
		String objectType = srcData.getObjectType();

		//make sure object doesn't exist
		T osmObject = (T)osmData.getOsmObject(id,objectType,true);
		if(osmObject.getIsLoaded()) {
			throw new UnchangedException("Object already exists: " + objectType + " " + id);
		}

		srcData.copyInto(osmObject,osmData);
		osmObject.setDataVersion(editNumber);
		
		//initialize object
		osmObject.objectCreated(osmData);
	}
	
	
	/** This method executes the instruction, returning the inferred initial data. */
	public void executeDelete(OsmData osmData, OsmSrcData<T> srcData, int editNumber) 
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
