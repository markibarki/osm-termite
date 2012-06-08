package intransix.osm.termite.map.model;

import intransix.osm.termite.map.osm.*;
import intransix.osm.termite.util.MercatorCoordinates;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;

/**
 * This class represents a structure object. 
 * 
 * @author sutter
 */
public class TermiteStructure extends TermiteObject {
	
	//==============================
	// Properties
	//==============================
	
	private Rectangle2D bounds;
	
	private OsmWay parent;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	private OsmWay anchor;
	
	private OsmRelation osmRelation = null;
	
	//==============================
	// Public Methods
	//==============================
	
	/** This method returns the most recently calculated bounds. */
	public Rectangle2D getBounds() {
		return bounds;
	}
	
	/** This method return the parent object for the structure, which is the footprint
	 * in the outdoor map.  */
	public OsmWay getParent() {
		return parent;
	}
	
	/** This method looks up the level in this structure with the given zlevel value. */
	public TermiteLevel lookupLevel(int zlevel) {
		for(TermiteLevel level:levels) {
			if(level.getZlevel() == zlevel) return level;
		}
		return null;
	}
	
	/** This method returns the list of levels in the structure. */
	public ArrayList<TermiteLevel> getLevels() {
		return levels;
	}
	
	//====================
	// Package Methods
	//====================
	
	/** This method calculates the bounds of the object. */
	void calculateBounds() {
		//calculate bounds
		double minX = MercatorCoordinates.MAX_SIZE;
		double minY = MercatorCoordinates.MAX_SIZE;
		double maxX = -MercatorCoordinates.MAX_SIZE;
		double maxY = -MercatorCoordinates.MAX_SIZE;
		for(TermiteLevel level:this.levels) {
			for(TermiteNode termiteNode:level.getNodes()) {
				
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
	
	/** This method loads the TermiteStructure from the OSM structure Relation. */
	void setOsmRelation(OsmRelation osmRelation) {
		this.osmRelation = osmRelation;
	}
	
	/** This initializes the outdoor level - bypassing the structure relation. */
	TermiteLevel initOutdoors(TermiteData termiteData) {
		TermiteLevel outdoorLevel = termiteData.getLevel(OsmObject.INVALID_ID,true);
		this.levels.clear();
		this.levels.add(outdoorLevel);
		return outdoorLevel;
	}
	
	void updateLocalData(TermiteData termiteData) {
		if(osmRelation == null) return;
		
		levels.clear();
		
		OsmData osmData = termiteData.getWorkingData();
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			long memberId = osmMember.memberId;
			//only allow multi ways
			if(OsmModel.ROLE_PARENT.equalsIgnoreCase(osmMember.role)) {
				if(OsmModel.TYPE_WAY.equalsIgnoreCase(osmMember.type)) {
					this.parent = osmData.getOsmWay(memberId);
				}
			}
			else if(OsmModel.ROLE_LEVEL.equalsIgnoreCase(osmMember.role)) {
				TermiteLevel level;
				if(OsmModel.doNodeLevelLabels) {
					//METHOD 2 - shell geometry
//no action? 
				}
				else {
					//METHOD 1 - level relation
					level = termiteData.getLevel(memberId, true);
					this.levels.add(level);
					
				}
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_ANCHOR)) {
				if(OsmModel.TYPE_WAY.equalsIgnoreCase(osmMember.type)) {
					this.anchor = osmData.getOsmWay(memberId);
				}
			}
		}
		
	}
	
	void updateRemoteData(TermiteData termiteData) {
		for(TermiteLevel level:levels) {
			level.setStructure(this);
		}
	}
	
	/** This method returns the obm object for the structure. */
	@Override
	OsmObject getOsmObject() {
		return osmRelation;
	}
}
