package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.OsmMember;
import intransix.osm.termite.map.osm.OsmRelation;
import java.util.List;

/**
 *
 * @author sutter
 */
public class UpdateMemberOrder implements EditData<OsmRelation> {
	
	private int index;
	private boolean moveUp;
	
	public UpdateMemberOrder(int nodeIndex, boolean moveUp) {
		this.index = nodeIndex;
		this.moveUp = moveUp;
	}
	
	/** This method creates a copy of the edit data that can restore the initial state. 
	 * This method can throw a RecoeveableException, which means no data was changed. */
	@Override
	public EditData<OsmRelation> readInitialData(OsmRelation relation) throws UnchangedException {
		int undoIndex;
		if(moveUp) {
			undoIndex = index - 1;
		}
		else {
			undoIndex = index + 1;
		}
		UpdateMemberOrder undoUpdate = new UpdateMemberOrder(undoIndex,!moveUp);
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
		int readdIndex;
		if(moveUp) {
			if(index > 0) readdIndex = index - 1;
			else return; //no action since it can't move
		}
		else {
			if(index > members.size()-2) readdIndex = index + 1;
			else return; //no action since it can't move
		}
		OsmMember member = members.remove(index);
		members.add(readdIndex,member);
		
		TermiteObject<OsmRelation> termiteObject = relation.getTermiteObject();
		if(termiteObject != null) {
			if(termiteObject instanceof TermiteMultiPoly) {
				//update the way list
				List<TermiteWay> ways = ((TermiteMultiPoly)termiteObject).getWays();
				TermiteWay way = ways.remove(index);
				ways.add(readdIndex,way);
			}			
			termiteObject.incrementTermiteVersion();
		}
	}	
}
