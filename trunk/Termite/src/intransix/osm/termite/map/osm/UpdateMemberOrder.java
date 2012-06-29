package intransix.osm.termite.map.osm;

import java.util.List;

/**
 * This class moves a member within a relation.
 * 
 * @author sutter
 */
public class UpdateMemberOrder extends EditData<OsmRelation> {
	
	//========================
	// Properties
	//========================
	
	private int initialIndex;
	private int finalIndex;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param initialIndex		The index of the member to move.
	 * @param finalIndex		The destination index
	 */
	public UpdateMemberOrder(int initialIndex, int finalIndex) {
		this.initialIndex = initialIndex;
		this.finalIndex = finalIndex;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		UpdateMemberOrder undoUpdate = new UpdateMemberOrder(finalIndex,initialIndex);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmRelation relation, int editNumber) throws UnchangedException, Exception {
		//set the property
		List<TermiteMember> members = relation.getMembers();
		if((initialIndex >= members.size())||(initialIndex < 0)) {
			throw new UnchangedException("Invalid index for relation: " + relation.getId());
		}
		if((finalIndex >= members.size())||(finalIndex < 0)) {
			throw new UnchangedException("Invalid index for relation: " + relation.getId());
		}
		TermiteMember member = members.get(initialIndex);
		members.add(finalIndex,member);
		
		relation.setDataVersion(editNumber);
		relation.setContainingObjectDataVersion(editNumber);
	}	
}
