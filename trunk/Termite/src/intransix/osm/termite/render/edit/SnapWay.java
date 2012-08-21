package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmObject;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import intransix.osm.termite.map.data.OsmWay;
import intransix.osm.termite.map.data.OsmSegment;
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
	
	public static void loadHitWays(OsmSegment segment, Point2D mouseMerc, 
			double mercRadSq, List<SnapObject> snapObjects) {
		Point2D p1 = segment.getNode1().getPoint();
		Point2D p2 = segment.getNode2().getPoint();
		double err2 = Line2D.ptSegDistSq(p1.getX(),p1.getY(),p2.getX(),p2.getY(),
				mouseMerc.getX(),mouseMerc.getY());
		if(err2 < mercRadSq) {
			for(OsmWay way:segment.getOsmWays()) {
				snapObjects.add(new SnapWay(way,mouseMerc,err2));
			}
		}
	}
	
	/** This method renders the object.
	 * 
	 * @param g2				The graphics context
	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
	 * @param styleInfo			The style info for rendering
	 */
	@Override
	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
			StyleInfo styleInfo) {

		Style style = this.getHoverStyle(styleInfo);
		renderWay(g2,mercatorToPixels,way,style);
	}
	
	/** This method looks up an select object for this snap object.  . 
	 * 
	 * @return			The edit object
	 */
	@Override
	public Object getSelectObject() {
		return way;
	}
	
}
