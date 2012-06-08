/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.osm;

import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateInsertMember implements EditData<OsmRelation> {
	
	private OsmMember member;
	private int index;
	
	public UpdateInsertMember(OsmMember member, int nodeIndex) {
		this.member = member;
		this.index = nodeIndex;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		UpdateRemoveMember undoUpdate = new UpdateRemoveMember(index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an RecoverableException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmRelation relation) throws UnchangedException, Exception {
		//set the property
		List<OsmMember> members = relation.getMembers();
		OsmMember memberCopy = member.createCopy();
		members.add(index,memberCopy);
	}
}
