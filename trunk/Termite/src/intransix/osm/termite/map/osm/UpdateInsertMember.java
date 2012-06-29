package intransix.osm.termite.map.osm;

import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateInsertMember implements EditData<OsmRelation> {
	
	private OsmData osmData;
	private long id;
	private String objectType;
	private String role;
	private int index;
	
	public UpdateInsertMember(OsmData osmData, long id, String objectType, String role, int nodeIndex) {
		this.osmData = osmData;
		this.id = id;
		this.objectType = objectType;
		this.role = role;
		this.index = nodeIndex;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a UnchangedException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		UpdateRemoveMember undoUpdate = new UpdateRemoveMember(osmData,index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an UnchangedException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmRelation relation, int editNumber) throws UnchangedException, Exception {
		
		//set the property
		List<TermiteMember> members = relation.getMembers();
		if((index < 0)||(index > members.size())) {
			throw new UnchangedException("Invalid index for relation: " + index);
		}
		OsmObject osmObject = osmData.getOsmObject(id,objectType,true);
		if(osmObject == null) {
			throw new UnchangedException("Invalid member for relation: " + id + "," + objectType);
		}
		TermiteMember member = new TermiteMember(osmObject,role);
		members.add(index,member);
		osmObject.addRelation(relation);
		
		relation.setDataVersion(editNumber);
		relation.setContainingObjectDataVersion(editNumber);
	}
}
