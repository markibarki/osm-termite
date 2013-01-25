package intransix.osm.termite.app.mapdata.instruction;

import intransix.osm.termite.map.dataset.OsmSrcData;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmObject;

/**
 * This is a create instruction. It will create a OSMObject that is a copy of data
 * in an OsmDrcData object. 
 * 
 * @author sutter
 */
public class CreateInstruction<T extends OsmObject> extends EditInstruction {
	
	//========================
	// Properties
	//========================
	
	private OsmSrcData srcData;
	
	//========================
	// Constructon
	//========================
	
	/** Constructor. */
	public CreateInstruction(OsmSrcData srcData, MapDataManager mapDataManager) {
		this.srcData = srcData;
		long id = mapDataManager.getNextId();
		srcData.setId(id);
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method executes the instruction. */
	@Override
	void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeCreate(osmData,srcData,editNumber);
	}
	
	/** This method undoes the instruction. */
	@Override
	public void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeDelete(osmData,srcData,editNumber);
	}

}
