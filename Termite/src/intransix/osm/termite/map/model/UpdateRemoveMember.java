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
public class UpdateRemoveMember implements EditData<OsmRelation> {

	private TermiteData termiteData;
	private int index;
	
	public UpdateRemoveMember(TermiteData termiteData, int index) {
		this.termiteData = termiteData;
		this.index = index;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		List<OsmMember> members = relation.getMembers();
		if(members.size() <= index) {
			throw new UnchangedException("Invalid node index for relation: " + relation.getId());
		}
		OsmMember member = members.get(index);
		OsmMember memberCopy = member.createCopy();
		UpdateInsertMember undoUpdate = new UpdateInsertMember(termiteData, memberCopy, index);
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
		if(members.size() <= index) {
			throw new UnchangedException("Invalid node index for relation: " + relation.getId());
		}
		members.remove(index);
		
		TermiteObject<OsmRelation> termiteObject = relation.getTermiteObject();
		if(termiteObject != null) {
			if(termiteObject instanceof TermiteMultiPoly) {
				List<TermiteWay> ways = ((TermiteMultiPoly)termiteObject).getWays();
				TermiteWay way = ways.remove(index);
				way.setMultiPoly(null);
			}			
			termiteObject.incrementTermiteVersion();
		}
	}	
}