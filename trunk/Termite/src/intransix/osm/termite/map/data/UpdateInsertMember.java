package intransix.osm.termite.map.data;

import java.util.List;

/**
 * This edit instruction adds a member to a relation.
 * 
 * @author sutter
 */
public class UpdateInsertMember extends EditData<OsmRelation> {
	
	//========================
	// Properties
	//========================
	
	private final static int INVALID_INDEX = -1;
	
	private long id;
	private String objectType;
	private String role;
	private int index;
	
	//========================
	// Public Methods
	//========================
	
	/** Constructor
	 * 
	 * @param osmData		The data manager object
	 * @param id			The id of the object to insert into the relation.
	 * @param objectType	The type of object to insert into the relation.
	 * @param role			The role of the object in the relation
	 * @param index			The position in the relation to insert the object.
	 */
	public UpdateInsertMember(long id, String objectType, String role, int index) {
		this.id = id;
		this.objectType = objectType;
		this.role = role;
		this.index = index;
	}
	
	/** Constructor to insert the object at the end of the list
	 * 
	 * @param id			The id of the object to insert into the relation.
	 * @param objectType	The type of object to insert into the relation.
	 * @param role			The role of the object in the relation
	 */
	public UpdateInsertMember(long id, String objectType, String role) {
		this.id = id;
		this.objectType = objectType;
		this.role = role;
		this.index = INVALID_INDEX;
	}
	
	//========================
	// Package Methods
	//========================
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a UnchangedException, which means no data was changed. */
	@Override
	EditData<OsmRelation> readInitialData(OsmData osmData, OsmRelation relation) throws UnchangedException {
		UpdateRemoveMember undoUpdate = new UpdateRemoveMember(index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an UnchangedException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	void writeData(OsmData osmData, OsmRelation relation, int editNumber) throws UnchangedException, Exception {
		
		//set the property
		List<OsmMember> members = relation.getMembers();
		if(index == INVALID_INDEX) {
			index = members.size();
		}
		if((index < 0)||(index > members.size())) {
			throw new UnchangedException("Invalid index for relation: " + index);
		}
		OsmObject osmObject = osmData.getOsmObject(id,objectType,true);
		if(osmObject == null) {
			throw new UnchangedException("Invalid member for relation: " + id + "," + objectType);
		}
		OsmMember member = new OsmMember(osmObject,role);
		members.add(index,member);
		osmObject.addRelation(relation);
		
		relation.setDataVersion(osmData,editNumber);
		relation.setContainingObjectDataVersion(osmData,editNumber);
	}
}
