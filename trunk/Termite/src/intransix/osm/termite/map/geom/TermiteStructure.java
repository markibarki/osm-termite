package intransix.osm.termite.map.geom;

import intransix.osm.termite.map.MapObject;
import intransix.osm.termite.map.osm.OsmMember;
import intransix.osm.termite.map.osm.OsmModel;
import intransix.osm.termite.map.osm.OsmRelation;
import intransix.osm.termite.map.osm.OsmWay;
import intransix.osm.termite.util.MercatorCoordinates;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.awt.geom.Rectangle2D;

/**
 *
 * @author sutter
 */
public class TermiteStructure extends TermiteObject {
	
	private long id;
	private Rectangle2D bounds;
	
	private TermiteWay parent;
	private ArrayList<TermiteLevel> levels = new ArrayList<TermiteLevel>();
	private TermiteWay anchor;
	
	private OsmRelation osmRelation = null;
	
	public Rectangle2D getBounds() {
		return bounds;
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
	
	void calculateBounds() {
		//calculate bounds
		double minX = MercatorCoordinates.MAX_SIZE;
		double minY = MercatorCoordinates.MAX_SIZE;
		double maxX = 0;
		double maxY = 0;
		for(TermiteLevel level:this.levels) {
			for(TermiteFeature feature:level.getFeatures()) {
				for(TermiteWay way:feature.getWays()) {
					for(TermiteNode node:way.getNodes()) {
						Point2D point = node.getPoint();
						if(node == null) continue;

						double x = point.getX();
						double y = point.getY();
						if(x < minX) minX = x;
						if(y < minY) minY = y;
						if(x > maxX) maxX = x;
						if(y > maxY) maxY = y;
					}
				}
			}
		}
		bounds = new Rectangle2D.Double(minX,minY,maxX - minX, maxY - minY);
	}
	
	void load(OsmRelation osmRelation, TermiteData data) {
		this.osmRelation = osmRelation;
		copyProperties(osmRelation);
		
		//load members
		for(OsmMember osmMember:osmRelation.getMembers()) {
			long memberId = osmMember.member.getId();
			//only allow multi ways
			if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_PARENT)) {
				//footprint
				this.parent = data.getTermiteWay(memberId, true);
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_LEVEL)) {
				TermiteLevel level;
				if(OsmModel.doNodeLevelLabels) {
					//METHOD 2 - shell geometry
					if(osmMember.member instanceof OsmWay) {
						OsmWay osmShell = (OsmWay)osmMember.member; 
						TermiteWay termiteShellWay = data.getTermiteWay(memberId, true);
						TermiteFeature termiteShell = termiteShellWay.getFeature();
						if(termiteShell == null) termiteShell = data.createVirtualFeatureForWay(termiteShellWay);
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
				
				this.addLevel(level);
			}
			else if(osmMember.role.equalsIgnoreCase(OsmModel.ROLE_ANCHOR)) {
				anchor = data.getTermiteWay(osmMember.member.getId(), false);
			}
		}
	}
}
