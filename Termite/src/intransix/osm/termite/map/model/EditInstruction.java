package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public abstract class EditInstruction<T extends OsmObject> {
	
	//used only to undo a delete or redo a create, to maintain the data versioning
	private int recreateVersion = 0;

	
	public EditInstruction() {
	}
	
	public abstract void doInstruction(TermiteData termiteData) throws UnchangedException, Exception;
	
	public abstract void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception;
	
	//===============================
	// Private Methods
	//===============================
	
	/** This method executes the instruction, returning the inferred initial data. */
	public EditData<T> executeUpdate(TermiteData termiteData, T osmObject, EditData<T> targetData) 
			throws UnchangedException, Exception {
		
		EditData<T> newInitialData = targetData.readInitialData(osmObject);
		targetData.writeData(osmObject);
		
		return newInitialData;
	}
	
	
	/** This method executes the instruction, returning the inferred initial data. */
	public void executeCreate(TermiteData termiteData, T objectToCopy) 
			throws UnchangedException, Exception {
		
		//lookup object
		long id = objectToCopy.getId();
		String objectType = objectToCopy.getObjectType();

		//make sure object doesn't exist
		OsmData osmData = termiteData.getWorkingData();
		T osmObject = (T)osmData.getOsmObject(id, objectType);
		if(osmObject != null) {
			throw new UnchangedException("Object already exists: " + objectType + " " + id);
		}

		osmObject = (T)osmData.createOsmObject(id,objectType);
		objectToCopy.copyInto(osmObject);
		
		//process the update - for specific type
		TermiteObject termiteObject = null;
		if(objectType.equalsIgnoreCase("node")) {
			termiteObject = termiteData.getNode(id, true);
		}
		else if(objectType.equalsIgnoreCase("way")) {
			termiteObject = termiteData.getWay(id, true);
		}
		else if(objectType.equalsIgnoreCase("relation")) {
			termiteObject = termiteData.getRelation(id,true);
		}
		
		if(termiteObject != null) {
			termiteObject.init(termiteData,osmObject);
			termiteObject.setLocalDataVersion(recreateVersion);
		}
		
	}
	
	
	/** This method executes the instruction, returning the inferred initial data. */
	public void executeDelete(TermiteData termiteData, T osmObject) 
			throws UnchangedException, Exception {
		
		OsmData osmData = termiteData.getWorkingData();
		
		//process the update
		OsmObject liveOsmObject = osmData.getOsmObject(osmObject.getId(), osmObject.getObjectType());
		TermiteObject termiteObject;
		if(liveOsmObject != null) {
			termiteObject = liveOsmObject.getTermiteObject();
		}
		else {
			//this shouldn't happen
			termiteObject = null;
		}
		
		//verify a delete is OK - this will throw an unchanged exception if this delete
		//is not OK
		termiteObject.verifyDelete();
			
		//if this is a delete, remove the object
		osmData.deleteOsmObject(osmObject.getId(), osmObject.getObjectType());

		if(termiteObject != null) {
			//save this for recreating the object
			this.recreateVersion = termiteObject.getDataVersion();
			//delete data
			termiteData.deleteTermiteObject(termiteObject);
		}

	}
}
