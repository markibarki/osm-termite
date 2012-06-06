package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class TermiteStructure extends TermiteObject {
	
	private Rectangle2D bounds;
	
	private OsmWay parent;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	private OsmWay anchor;
	
	private OsmRelation osmRelation = null;
	
	public Rectangle2D getBounds() {
		return bounds;
	}
	
	public OsmWay getParent() {
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
	
	void calculateBounds() {
		//calculate bounds
		double minX = MercatorCoordinates.MAX_SIZE;
		double minY = MercatorCoordinates.MAX_SIZE;
		double maxX = -MercatorCoordinates.MAX_SIZE;
		double maxY = -MercatorCoordinates.MAX_SIZE;
		for(TermiteLevel level:this.levels) {
			for(TermiteNode termiteNode:level.getTermiteNodes()) {
				
				OsmNode node = termiteNode.getOsmNode();

				double x = node.getX();
				double y = node.getY();
				if(x < minX) minX = x;
				if(y < minY) minY = y;
				if(x > maxX) maxX = x;
				if(y > maxY) maxY = y;
			}
		}
		bounds = new Rectangle2D.Double(minX,minY,maxX - minX, maxY - minY);
	}
	
	void load(OsmRelation osmRelation, TermiteData data) {
		this.osmRelation = osmRelation;
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			long memberId = osmMember.member.getId();
			//only allow multi ways
			if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_PARENT)) {
				if(osmMember.member instanceof OsmWay) {
					this.parent = (OsmWay)osmMember.member;
				}
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_LEVEL)) {
				TermiteLevel level = data.getLevel(memberId);
				if(OsmModel.doNodeLevelLabels) {
					//METHOD 2 - shell geometry
					if(osmMember.member instanceof OsmWay) {
						OsmWay osmShell = (OsmWay)osmMember.member;
						level.loadFromShell(osmShell,data);
					}
				}
				else {
					//METHOD 1 - level relation
					
				}
				
				if(level != null) {
					this.levels.add(level);
					level.setStructure(this);
				}
				
				this.addLevel(level);
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_ANCHOR)) {
				if(osmMember.member instanceof OsmWay) {
					anchor = (OsmWay)osmMember.member;
				}
			}
		}
	}
	
	@Override
	OsmObject getOsmObject() {
		return osmRelation;
	}
}
