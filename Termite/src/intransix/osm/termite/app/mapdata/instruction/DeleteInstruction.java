package intransix.osm.termite.app.mapdata.instruction;

import intransix.osm.termite.map.dataset.OsmSrcData;
import intransix.osm.termite.map.dataset.OsmNodeSrc;
import intransix.osm.termite.map.dataset.OsmWaySrc;
import intransix.osm.termite.map.dataset.OsmRelationSrc;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmRelation;
import intransix.osm.termite.map.workingdata.OsmWay;

/**
 * This is a delete instruction.
 * 
 * @author sutter
 */
public class DeleteInstruction<T extends OsmObject> extends EditInstruction {
	
	//========================
	// Properties
	//========================
	
	private OsmSrcData srcData;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor */
	public DeleteInstruction(T objectToDelete) {
		//get a copy of the object to delete
		long id = objectToDelete.getId();
		if(objectToDelete instanceof OsmNode) {
			OsmNodeSrc src = new OsmNodeSrc(id);
			((OsmNode)objectToDelete).copyInto(src);
			this.srcData = src;
		}
		else if(objectToDelete instanceof OsmWay) {
			OsmWaySrc src = new OsmWaySrc(id);
			((OsmWay)objectToDelete).copyInto(src);
			this.srcData = src;
		}
		else if(objectToDelete instanceof OsmRelation) {
			OsmRelationSrc src = new OsmRelationSrc(id);
			((OsmRelation)objectToDelete).copyInto(src);
			this.srcData = src;
		}
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method executes the instruction. */
	@Override
	void doInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeDelete(osmData,srcData,editNumber);
	}
	
	/** This method undoes the instruction. */
	@Override
	void undoInstruction(OsmData osmData, int editNumber) throws UnchangedException, Exception {
		executeCreate(osmData,srcData,editNumber);
	}

}
