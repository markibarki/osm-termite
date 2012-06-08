/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.osm;

/**
 *
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
		String initialValue = osmObject.getProperty(initialKey);
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
		if(!initialKey.equals(finalKey)) {
			osmObject.removeProperty(initialKey);
		}
		//set the property
		osmObject.setProperty(finalKey, finalValue);
	}
}
