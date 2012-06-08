package intransix.osm.termite.map.osm;

/**
 *
 * @author sutter
 */
public class EditInstruction<T extends OsmObject> {
	
	public enum InstrType {
		CREATE, UPDATE, DELETE
	};
	
	private OsmData osmData;
	private InstrType instrType;
	private String objectType;
	private long id;
	private EditData<T> initialData;
	private EditData<T> finalData;
	
	public EditInstruction(OsmData osmData, T osmObject, InstrType instrType) {
		this.osmData = osmData;
		this.id = osmObject.getId();
		this.objectType = osmObject.getObjectType();
		this.instrType = instrType;
	}
	
	public void setData(EditData<T> initialData, EditData<T> finalData) {
		this.initialData = initialData;
		this.finalData = finalData;
	}
	
	public void doInstruction() throws UnchangedException, Exception {
		execute(instrType,initialData,finalData);
	}
	
	public void undoInstruction() throws UnchangedException, Exception {
		InstrType effectiveType;
		if(instrType == InstrType.CREATE) effectiveType = InstrType.DELETE;
		else if(instrType == InstrType.UPDATE) effectiveType = InstrType.UPDATE;
		else if(instrType == InstrType.DELETE) effectiveType = InstrType.CREATE;
		else throw new UnchangedException("Invalid instruction type");
		
		execute(effectiveType,finalData,initialData);
	}
	
	//===============================
	// Private Methods
	//===============================
	
	/** This method executes the instruction, returning the inferred initial data. */
	public EditData<T> execute(InstrType t, EditData<T> startData, EditData<T> targetData) 
			throws UnchangedException, Exception {
		
		//lookup object
		T osmObject;
		if(t == InstrType.CREATE) {
			//make sure object doesn't exist
			osmObject = (T)osmData.getOsmObject(id, objectType);
			if(osmObject != null) {
				throw new UnchangedException("Object already exists: " + objectType + " " + id);
			}
			
			osmObject = (T)osmData.createOsmObject(id,objectType);
		}
		else {
			osmObject = (T)osmData.getOsmObject(id,objectType);
			
			if(osmObject == null) {
				throw new UnchangedException("Object does not exist: " + objectType + " " + id);
			}
		}
		
		//save the initial data.
		EditData<T> newInitialData = targetData.readInitialData(osmObject);
	
//OPTIONAL - We may want to avalidate existing initial data
//and throw an exception if it does not agree
		
		//update the object
		targetData.writeData(osmObject);
		
		//if this is a delete, remove the object
		if(t == InstrType.DELETE) {
			osmData.removeOsmObject(this.id, this.objectType);
		}
		
		return newInitialData;
	}
}
