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
public class TermiteStructure extends TermiteObject<OsmWay> {
	
	//==============================
	// Properties
	//==============================
	
	private Rectangle2D bounds;
	
	private OsmWay parent;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	
	//==============================
	// Public Methods
	//==============================
	
	/** This method returns the most recently calculated bounds. */
	public Rectangle2D getBounds() {
		return bounds;
	}
	
	/** This method calculates the bounds of the object. */
	public void calculateBounds() {
		//calculate bounds
		double minX = MercatorCoordinates.MAX_SIZE;
		double minY = MercatorCoordinates.MAX_SIZE;
		double maxX = -MercatorCoordinates.MAX_SIZE;
		double maxY = -MercatorCoordinates.MAX_SIZE;
		for(TermiteLevel level:this.levels) {
			for(TermiteNode termiteNode:level.getNodes()) {
				
				OsmNode node = termiteNode.getOsmObject();

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
	
	void addLevel(TermiteLevel level) {
		this.levels.add(level);
	}
	
	void init(TermiteData termiteData, OsmWay parent) {
		this.parent = parent;
	}
	
	void objectDeleted(TermiteData termiteData) {
//do something here once I figure out how a structure is deleted		
	}
	
	/** This method returns the obm object for the structure. */
	@Override
	public OsmWay getOsmObject() {
		return parent;
	}
}
