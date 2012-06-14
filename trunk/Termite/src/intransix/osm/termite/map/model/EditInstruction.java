package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public abstract class EditInstruction<T extends OsmObject> {

	
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
		//we can probably cleean this up with generics and a new method on termite data
		//think about this
		if(objectType.equalsIgnoreCase("node")) {
			TermiteNode tn = termiteData.getNode(id, true);
			tn.init(termiteData,(OsmNode)osmObject);
		}
		else if(objectType.equalsIgnoreCase("way")) {
			TermiteWay tw = termiteData.getWay(id, true);
			tw.init(termiteData,(OsmWay)osmObject);
		}
		else if(objectType.equalsIgnoreCase("relation")) {
			String relationType = ((OsmRelation)osmObject).getProperty(OsmModel.TAG_TYPE);
			if(OsmModel.TYPE_MULTIPOLYGON.equalsIgnoreCase(relationType)) {
				TermiteMultiPoly tmp = termiteData.getMultiPoly(id, true);
				tmp.init(termiteData,(OsmRelation)osmObject);
			}
		}
	}
	
	
	/** This method executes the instruction, returning the inferred initial data. */
	public void executeDelete(TermiteData termiteData, T osmObject) 
			throws UnchangedException, Exception {
		
		OsmData osmData = termiteData.getWorkingData();
		
		//process the update
		OsmObject liveOsmObject = osmData.createOsmObject(osmObject.getId(), osmObject.getObjectType());
		TermiteObject termiteObject;
		if(liveOsmObject != null) {
			termiteObject = liveOsmObject.getTermiteObject();
		}
		else {
			//this shouldn't happen
			termiteObject = null;
		}
			
		//if this is a delete, remove the object
		osmData.removeOsmObject(osmObject.getId(), osmObject.getObjectType());

		if(termiteObject != null) {
			termiteData.deleteTermiteObject(termiteObject);
		}

	}
}
