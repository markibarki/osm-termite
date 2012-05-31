package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import intransix.osm.termite.map.osm.OsmMember;
import intransix.osm.termite.map.osm.OsmRelation;
import intransix.osm.termite.map.osm.OsmWay;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class TermiteStructure extends TermiteObject {
	
	public final static String ROLE_PARENT = "parent";
	public final static String ROLE_LEVEL = "level";
	public final static String ROLE_ANCHOR = "anchor";
	
	private long id;
	private Rectangle2D bounds;
	
	private TermiteWay parent;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	private TermiteWay anchor;
	
	private OsmRelation osmRelation = null;
	
	public Rectangle2D getBounds() {
		return bounds;
	}
	
	public void setBounds(Rectangle2D bounds) {
		this.bounds = bounds;
	}
	
	public TermiteWay getParent() {
		return parent;
	}
	
	public TermiteLevel lookupLevel(int zlevel) {
		for(TermiteLevel level:levels) {
			if(level.getZlevel() == zlevel) return level;
		}
		return null;
	}
	
	public void addLevel(TermiteLevel level) {
		this.levels.add(level);
		level.setStructure(this);
	}
	
	public ArrayList<TermiteLevel> getLevels() {
		return levels;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** Constructor. It should only be called by members of this package. */
	TermiteStructure(long id) {
		super(id);
	}
	
	void load(OsmRelation osmRelation, TermiteData data) {
		this.osmRelation = osmRelation;
		copyProperties(osmRelation);
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			long memberId = osmMember.member.getId();
			//only allow multi ways
			if(osmMember.role.equalsIgnoreCase(ROLE_PARENT)) {
				//footprint
				this.parent = data.getTermiteWay(memberId, true);
			}
			else if(osmMember.role.equalsIgnoreCase(ROLE_LEVEL)) {
				TermiteLevel level;
				if(data.getDoNodeLevelLabel()) {
					//METHOD 2 - shell geometry
					if(osmMember.member instanceof OsmWay) {
						OsmWay osmShell = (OsmWay)osmMember.member; 
						TermiteWay termiteShell = data.getTermiteWay(memberId, true);
						level = data.getTermiteLevel(memberId, true);
						level.loadLevelFromShell(osmShell, termiteShell);
					}
					else {
						level = null;
					}
				}
				else {
					//METHOD 1 - level relation
					level = data.getTermiteLevel(memberId, true);
				}
				
				if(level != null) {
					this.levels.add(level);
					level.setStructure(this);
				}
			}
			else if(osmMember.role.equalsIgnoreCase(ROLE_ANCHOR)) {
				anchor = data.getTermiteWay(osmMember.member.getId(), false);
			}
		}
	}
}
