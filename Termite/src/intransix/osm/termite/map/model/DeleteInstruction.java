package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;

/**
 *
 * @author sutter
 */
public class DeleteInstruction<T extends OsmObject> extends EditInstruction {
	private T osmObject;
	
	public DeleteInstruction(T objectToDelete) {
		//get a copy of the object to delete
		long id = objectToDelete.getId();
		if(objectToDelete instanceof OsmNode) {
			OsmNode node = new OsmNode(id);
			objectToDelete.copyInto(node);
			this.osmObject = (T)node;
		}
		else if(objectToDelete instanceof OsmWay) {
			OsmWay way = new OsmWay(id);
			objectToDelete.copyInto(way);
			this.osmObject = (T)way;
		}
		else if(objectToDelete instanceof OsmRelation) {
			OsmRelation relation = new OsmRelation(id);
			objectToDelete.copyInto(relation);
			this.osmObject = (T)relation;
		}
	}
	
	public void doInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeDelete(termiteData,osmObject);
	}
	
	public void undoInstruction(TermiteData termiteData) throws UnchangedException, Exception {
		executeCreate(termiteData,osmObject);
	}

}
