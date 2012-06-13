package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmObject;

/**
 * This EditData object is used to create, update and delete a property. To create,
 * the initial key should be null. To delete, the final key should be null and the final
 * value is unused. To update a key value without changing the key, the initial and
 * final keys should be the same. To change the key without changing the value, the final
 * and initial keys should be different. The final value should still be entered. If the
 * final value is set to null, this will delete the key.
 * @author sutter
 */
public class UpdateObjectProperty<T extends OsmObject> implements EditData<T> {
	
	private String initialKey;
	private String finalKey;
	private String finalValue;
	
	public UpdateObjectProperty(String initialKey, String finalKey, String finalValue) {
		this.initialKey = initialKey;
		this.finalKey = finalKey;
		this.finalValue = finalValue;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<T> readInitialData(T osmObject) throws UnchangedException {
		String initialValue;
		if(initialKey != null) {
			initialValue = osmObject.getProperty(initialKey);
		}
		else {
			 initialValue = null;
		}
		UpdateObjectProperty<T> undoUpdate = new UpdateObjectProperty<T>(finalKey,initialKey,initialValue);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(T osmObject) throws UnchangedException, Exception {
		//if the key changes, delete the old property
		if(((initialKey != null)&&(!initialKey.equals(finalKey)))
			||(finalValue == null)) {
			osmObject.removeProperty(initialKey);
		}
		//set the property
		if(finalValue != null) {
			osmObject.setProperty(finalKey, finalValue);
		}
		
		TermiteObject termiteObject = osmObject.getTermiteObject();
		if(termiteObject != null) {
//			termiteObject.propertiesUpdated();
		}
	}
}
