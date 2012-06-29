package intransix.osm.termite.map.data;

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
	
	private OsmSrcData<T> srcData;
	
	//========================
	// Constructon
	//========================
	
	/** Constructor. */
	public CreateInstruction(OsmSrcData<T> srcData, OsmData osmData) {
		this.srcData = srcData;
		long id = osmData.getNextId();
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
