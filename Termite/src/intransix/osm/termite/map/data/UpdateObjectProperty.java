package intransix.osm.termite.map.data;

/**
 * This EditData object is used to create, update and delete a property.
 * 
 * @author sutter
 */
public class UpdateObjectProperty<T extends OsmObject> extends EditData<T> {
	
	//========================
	// Properties
	//========================
	
	private String initialKey;
	private String finalKey;
	private String finalValue;
	private OsmData osmData;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param osmData		The data manager object
	 * @param initialKey	The initial value of the key. If this is null, a new property
	 *						will be created.
	 * @param finalKey		The final value of the key.  If this is null, the property
	 *						will be deleted. If the key value is not being changed, the initial
	 *						key and final key should be the same. 
	 * @param finalValue	The final value of the key. If this is null the key is deleted.
	 */
	public UpdateObjectProperty(OsmData osmData, String initialKey, String finalKey, String finalValue) {
		this.osmData = osmData;
		this.initialKey = initialKey;
		this.finalKey = finalKey;
		this.finalValue = finalValue;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<T> readInitialData(T osmObject) throws UnchangedException {
		String initialValue;
		if(initialKey != null) {
			initialValue = osmObject.getProperty(initialKey);
		}
		else {
			 initialValue = null;
		}
		UpdateObjectProperty<T> undoUpdate = new UpdateObjectProperty<T>(osmData,finalKey,initialKey,initialValue);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(T osmObject, int editNumber) throws UnchangedException, Exception {
		//if the key changes, delete the old property
		if(((initialKey != null)&&(!initialKey.equals(finalKey)))
			||(finalValue == null)) {
			osmObject.removeProperty(initialKey);
		}
		//set the property
		if(finalValue != null) {
			osmObject.setProperty(finalKey, finalValue);
		}
		
		//re-classify the object
		osmObject.propertiesUpdated(osmData);
		
		osmObject.setDataVersion(editNumber);
		osmObject.setContainingObjectDataVersion(editNumber);
	}
}
