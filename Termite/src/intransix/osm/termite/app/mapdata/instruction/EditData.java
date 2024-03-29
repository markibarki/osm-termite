package intransix.osm.termite.app.mapdata.instruction;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.dataset.OsmSrcData;

/**
 * <p>This object is used to edit data on OsmObjects. It is implemented as an abstract
 * class instead of an interface to the inherited methods to be package methods and
 * not public methods.</p>
 * 
 * <p> An edit data object should respect the data version rules described in the
 * notes in EditAction</p> 
 * 
 * @author sutter
 */
public abstract class EditData<T extends OsmObject> {
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	abstract EditData<T> readInitialData(OsmData osmData, T osmObject) throws UnchangedException;
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 * which means the state of the data was not modified, or a different exception, in which case it
	 * will be assumed the data was changes and the state of the system can not be recovered. The
	 * application will be forced to close if this happens.
	 * 
	 * @param osmData		The data manager
	 * @param osmObject		The object that is being edited
	 * @param editNumber	The version associated with this action doing this edit.
	 * @return				The value for the property
	 * */
	abstract void writeData(OsmData osmData, T osmObject, int editNumber) throws UnchangedException, Exception;
}
