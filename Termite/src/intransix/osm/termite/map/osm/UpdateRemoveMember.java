package intransix.osm.termite.map.osm;

import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateRemoveMember implements EditData<OsmRelation> {

	private OsmData osmData;
	private int index;
	
	public UpdateRemoveMember(OsmData osmData, int index) {
		this.osmData = osmData;
		this.index = index;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		
		//get the undo command
		List<TermiteMember> members = relation.getMembers();
		if((index < 0)||(index >= members.size())) {
			throw new UnchangedException("Invalid index for relation: " + index);
		}
		TermiteMember member = members.get(index);
		OsmObject osmObject = member.termiteObject;
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
		UpdateInsertMember undoUpdate = new UpdateInsertMember(osmData,id,objectType,member.role,index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmRelation relation, int editNumber) throws UnchangedException, Exception {
		//set the property
		List<TermiteMember> members = relation.getMembers();
		if((index < 0)||(index >= members.size())) {
			throw new UnchangedException("Invalid index for relation: " + index);
		}
		TermiteMember member = members.remove(index);
		//remove relation from this way if there are no other copies in the relation
		OsmObject osmObject = member.termiteObject;
		if(osmObject != null) {
			boolean otherCopies = false;
			for(TermiteMember m:members) {
				if(m.termiteObject == osmObject) otherCopies = true;
			}
			if(!otherCopies) {
				osmObject.removeRelation(relation);
			}
		}
		
		relation.setDataVersion(editNumber);
		relation.setContainingObjectDataVersion(editNumber);
	}	
}
