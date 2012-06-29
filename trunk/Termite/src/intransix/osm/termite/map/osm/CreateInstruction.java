package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteData;
import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class CreateInstruction<T extends OsmObject> extends EditInstruction {
	
	private OsmSrcData<T> srcData;
	
	public CreateInstruction(OsmSrcData<T> srcData, OsmData osmData) {
		this.srcData = srcData;
		long id = osmData.getNextId();
		srcData.setId(id);
	}
	
	public void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeCreate(osmData,srcData,editNumber);
	}
	
	public void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeDelete(osmData,srcData,editNumber);
	}

}
