package intransix.osm.termite.app.mapdata.instruction;

import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmMember;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.map.workingdata.OsmRelation;
import java.util.List;

/**
 * This class removes a member from a relation.
 * 
 * @author sutter
 */
public class UpdateRemoveMember extends EditData<OsmRelation> {

	//========================
	// Pproperties
	//========================

	private int index;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param index			The index of the member to remove. This refers to 
	 *						the index at the time the instruction is executed
	 *						and not when the instruction is created. If another
	 *						member is added or removed, this can change the index
	 *						at instruction execution time relative to the index when
	 *						at the time before the action is executed.
	 */
	public UpdateRemoveMember(int index) {
		this.index = index;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmRelation> readInitialData(OsmData osmData, OsmRelation relation) throws UnchangedException {
		
		//get the undo command
		List<OsmMember> members = relation.getMembers();
		if(index == -1) {
			//remove the last one
			index = relation.getMembers().size() - 1;
		}
		else if((index < 0)||(index >= members.size())) {
			throw new UnchangedException("Invalid index for relation: " + index);
		}
		OsmMember member = members.get(index);
		OsmObject osmObject = member.osmObject;
		long id;
		String objectType;
		if(osmObject == null) {
			//this shouldn't happen
			id = OsmData.INVALID_ID;
			//we have to call it something, I guess
			objectType = OsmModel.TYPE_NODE;
		}
		else {
			id = osmObject.getId();
			objectType = osmObject.getObjectType();
		}
		UpdateInsertMember undoUpdate = new UpdateInsertMember(id,objectType,member.role,index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmData osmData, OsmRelation relation, int editNumber) throws UnchangedException, Exception {
		//set the property
		List<OsmMember> members = relation.getMembers();
		if((index < 0)||(index >= members.size())) {
			throw new UnchangedException("Invalid index for relation: " + index);
		}
		OsmMember member = members.remove(index);
		//remove relation from this way if there are no other copies in the relation
		OsmObject osmObject = member.osmObject;
		if(osmObject != null) {
			boolean otherCopies = false;
			for(OsmMember m:members) {
				if(m.osmObject == osmObject) otherCopies = true;
			}
			if(!otherCopies) {
				osmObject.removeRelation(relation);
			}
		}
		
		relation.setDataVersion(editNumber);
		relation.setContainingObjectDataVersion(editNumber);
	}	
}
