package intransix.osm.termite.map.model;

import intransix.osm.termite.map.model.UnchangedException;
import intransix.osm.termite.map.osm.OsmData;
import intransix.osm.termite.map.osm.OsmObject;

/**
 *
 * @author sutter
 */
public class EditInstruction<T extends OsmObject> {
	
	public enum InstrType {
		CREATE, UPDATE, DELETE
	};
	
	private InstrType instrType;
	private String objectType;
	private long id;
	private EditData<T> initialData;
	private EditData<T> finalData;
	
	public EditInstruction(T osmObject, InstrType instrType) {
		this.id = osmObject.getId();
		this.objectType = osmObject.getObjectType();
		this.instrType = instrType;
	}
	
	public EditInstruction(long objectId, String objectType, InstrType instrType) {
		this.id = objectId;
		this.objectType = objectType;
		this.instrType = instrType;
	}
	
	public void setData(EditData<T> finalData) {
		this.initialData = null;
		this.finalData = finalData;
	}
	
	public void doInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		initialData = execute(termiteData,instrType,initialData,finalData);
	}
	
	public void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		InstrType effectiveType;
		if(instrType == InstrType.CREATE) effectiveType = InstrType.DELETE;
		else if(instrType == InstrType.UPDATE) effectiveType = InstrType.UPDATE;
		else if(instrType == InstrType.DELETE) effectiveType = InstrType.CREATE;
		else throw new UnchangedException("Invalid instruction type");
		
		finalData = execute(termiteData,effectiveType,finalData,initialData);
	}
	
	//===============================
	// Private Methods
	//===============================
	
	/** This method executes the instruction, returning the inferred initial data. */
	public EditData<T> execute(TermiteData termiteData, 
			InstrType t, 
			EditData<T> startData, 
			EditData<T> targetData) throws UnchangedException, Exception {
		
		OsmData osmData = termiteData.getWorkingData();
		
		EditData<T> newInitialData;
				
		//lookup object
		T osmObject;
		if(t == InstrType.CREATE) {
			//make sure object doesn't exist
			osmObject = (T)osmData.getOsmObject(id, objectType);
			if(osmObject != null) {
				throw new UnchangedException("Object already exists: " + objectType + " " + id);
			}
			
			osmObject = (T)osmData.createOsmObject(id,objectType);
			
			//no initial state for create
			newInitialData = null;
		}
		else {
			osmObject = (T)osmData.getOsmObject(id,objectType);
			
			if(osmObject == null) {
				throw new UnchangedException("Object does not exist: " + objectType + " " + id);
			}
			
			if(t == InstrType.DELETE) {
				//on delete, the object becomes the initial data
				newInitialData = (EditData<T>)osmObject;
			}
			else {
				//on update, generate the initial data
				newInitialData = targetData.readInitialData(osmObject);
			}
		}
	
//OPTIONAL - We may want to validate existing initial data
//and throw an exception if it does not agree
//in this case, we may also want to allow the user to set initial data to validate against
//during the first execution
		
		//process the update
		TermiteObject termiteObject = osmObject.getTermiteObject();
			
		if(t == InstrType.DELETE) {
			//if this is a delete, remove the object
			osmData.removeOsmObject(this.id, this.objectType);
			
			termiteData.deleteTermiteObject(termiteObject);
		}
		else {
			//update the object
			targetData.writeData(osmObject);
			osmObject.incrementLocalVersion();
		}
		
		
		
		return newInitialData;
	}
}
