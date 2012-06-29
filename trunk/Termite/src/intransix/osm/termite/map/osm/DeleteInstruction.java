package intransix.osm.termite.map.osm;

import intransix.osm.termite.map.model.TermiteData;
import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class DeleteInstruction<T extends OsmObject> extends EditInstruction {
	
	private OsmSrcData<T> srcData;
	
	public DeleteInstruction(T objectToDelete) {
		//get a copy of the object to delete
		long id = objectToDelete.getId();
		if(objectToDelete instanceof OsmNode) {
			OsmNodeSrc src = new OsmNodeSrc(id);
			src.copyFrom((OsmNode)objectToDelete);
			this.srcData = (OsmSrcData<T>)src;
		}
		else if(objectToDelete instanceof OsmWay) {
			OsmWaySrc src = new OsmWaySrc(id);
			src.copyFrom((OsmWay)objectToDelete);
			this.srcData = (OsmSrcData<T>)src;
		}
		else if(objectToDelete instanceof OsmRelation) {
			OsmRelationSrc src = new OsmRelationSrc(id);
			src.copyFrom((OsmRelation)objectToDelete);
			this.srcData = (OsmSrcData<T>)src;
		}
	}
	
	public void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeDelete(osmData,srcData,editNumber);
	}
	
	public void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeCreate(osmData,srcData,editNumber);
	}

}
