package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmMember;
import intransix.osm.termite.map.osm.OsmRelation;
import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateRole implements EditData<OsmRelation> {
	
	private String role;
	private int index;
	
	public UpdateRole(String role, int index) {
		this.role = role;
		this.index = index;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		List<OsmMember> members = relation.getMembers();
		if((index >= members.size())||(index < 0)) {
			throw new UnchangedException("Invalid index for relation: " + relation.getId());
		}
		OsmMember member = members.get(index);
		String initialRole = member.role;
		UpdateRole undoUpdate = new UpdateRole(initialRole, index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmRelation relation) throws UnchangedException, Exception {
		List<OsmMember> members = relation.getMembers();
		if((index >= members.size())||(index < 0)) {
			throw new UnchangedException("Invalid index for relation: " + relation.getId());
		}
		OsmMember member = members.get(index);
		member.role = role;
		//explicitly increment version sinec edit was external
		relation.incrementLocalVersion();
		
		//no action here for multipolygon for now. I think we are ignoring the role.
	}	
}
