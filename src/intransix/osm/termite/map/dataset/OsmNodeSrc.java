package intransix.osm.termite.map.dataset;

import intransix.osm.termite.app.mapdata.download.MapDataRequest;
import intransix.osm.termite.map.workingdata.OsmData;
import intransix.osm.termite.map.workingdata.OsmModel;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.util.MercatorCoordinates;
import org.xml.sax.Attributes;
import java.awt.geom.Point2D;

/**
 * This class holds data in an OSM node. 
 * 
 * @author sutter
 */
public class OsmNodeSrc extends OsmSrcData {
	
	//======================
	// Properties
	//======================
	
	//node
	Point2D mxy = new Point2D.Double();
	
	//======================
	// Public Methods
	//======================
	
	public OsmNodeSrc() {
		super(OsmModel.TYPE_NODE,OsmData.INVALID_ID);
	}
	
	/** This method gets the X coordinate of the node, in local mercator units. */
	public Point2D getPoint() {
		return mxy;
	}
	
	public void setPosition(double x, double y) {
		mxy.setLocation(x, y);
	}
	
	//------------------------------
	// Commit Methods
	//-----------------------------
	
	public boolean isDifferent(OsmNode osmNode) {
		//compare points
//we might want to base this on a tolerence, not absolute match
		Point2D point = osmNode.getPoint();
		if((point.getX() != mxy.getX())||(point.getY() != mxy.getY())) {
			return true;
		}
		else return propertiesDifferent(osmNode);
	}
	
	//===================
	// Package Methods
	//===================
	
	/** Constructor. */
	public OsmNodeSrc(long id) {
		super(OsmModel.TYPE_NODE,id);
	}
	
}
