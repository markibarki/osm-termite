/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmMember;
import intransix.osm.termite.map.osm.OsmRelation;
import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateInsertMember implements EditData<OsmRelation> {
	
	private TermiteData termiteData;
	private OsmMember member;
	private int index;
	
	public UpdateInsertMember(TermiteData termiteData, OsmMember member, int nodeIndex) {
		this.termiteData = termiteData;
		this.member = member;
		this.index = nodeIndex;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a UnchangedException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		UpdateRemoveMember undoUpdate = new UpdateRemoveMember(termiteData,index);
		return undoUpdate;
	}
		
	/** This method writes the data to the object. This method can throw an UnchangedException,
	 which means the state of the data was not modified, or a different exception, in which case it
	 will be assumed the data was changes and the state of the system can not be recovered. The
	 application will be forced to close if this happens. */
	@Override
	public void writeData(OsmRelation relation) throws UnchangedException, Exception {
		
		//set the property
		List<OsmMember> osmMembers = relation.getMembers();
		OsmMember memberCopy = member.createCopy();
		osmMembers.add(index,memberCopy);
		
		TermiteRelation termiteRelation = (TermiteRelation)relation.getTermiteObject();
		List<TermiteMember> termiteMembers = termiteRelation.getMembers();
		TermiteMember termiteMember = TermiteMember.createTermiteMember(termiteData,member);
		termiteMembers.add(index,termiteMember);
		
		termiteMember.termiteObject.addRelation(termiteRelation);
		
		termiteRelation.incrementDataVersion();
	}
}
