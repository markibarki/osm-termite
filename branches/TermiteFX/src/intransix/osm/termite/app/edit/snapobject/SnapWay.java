package intransix.osm.termite.app.edit.snapobject;

import intransix.osm.termite.map.workingdata.OsmObject;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import intransix.osm.termite.map.workingdata.OsmWay;
import intransix.osm.termite.map.workingdata.OsmSegment;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author sutter
 */
public class SnapWay extends SnapObject {
	
	public OsmWay way;
	
	/** Constructor */
	public SnapWay(OsmWay way, Point2D snapPoint, double err2) {
		super(SnapType.WAY);
		this.way = way;
		this.snapPoint = snapPoint;
		this.err2 = err2;
	}
	
//	/** This method renders the object.
//	 * 
//	 * @param g2				The graphics context
//	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
//	 * @param styleInfo			The style info for rendering
//	 */
//	@Override
//	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
//			StyleInfo styleInfo) {
//
//		Style style = this.getHoverStyle(styleInfo);
//		renderWay(g2,mercatorToPixels,way,style);
//	}
	
	/** This method looks up an select object for this snap object.  . 
	 * 
	 * @return			The edit object
	 */
	@Override
	public Object getSelectObject() {
		return way;
	}
	
}
