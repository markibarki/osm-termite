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
		
		//do this first since it might throw an exception - we don't want to update the data before then
		TermiteObject<OsmRelation> termiteObject = relation.getTermiteObject();
		if(termiteObject != null) {
			if(termiteObject instanceof TermiteMultiPoly) {
				
				//add the way to the multipolygon
				TermiteWay way = termiteData.getWay(member.memberId);
				if(way != null) {
					List<TermiteWay> ways = ((TermiteMultiPoly)termiteObject).getWays();
					ways.add(index,way);
					way.setMultiPoly((TermiteMultiPoly)termiteObject);
				}
				else {
					throw new UnchangedException("Way not found!");
				}
			}			
			termiteObject.incrementTermiteVersion();
		}
		
		//set the property
		List<OsmMember> members = relation.getMembers();
		OsmMember memberCopy = member.createCopy();
		members.add(index,memberCopy);
		//explicitly increment version since edit was external
		relation.incrementLocalVersion();
			
	}
}
