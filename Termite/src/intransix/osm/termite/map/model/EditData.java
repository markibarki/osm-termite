package intransix.osm.termite.map.model;

import intransix.osm.termite.map.model.UnchangedException;
import intransix.osm.termite.map.osm.OsmObject;

/**
 * This object is used to edit data on OsmObjects.
 * 
 * @author sutter
 */
public interface EditData<T extends OsmObject> {
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	public EditData<T> readInitialData(T osmObject) throws UnchangedException;
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	public void writeData(T osmObject) throws UnchangedException, Exception;
}
