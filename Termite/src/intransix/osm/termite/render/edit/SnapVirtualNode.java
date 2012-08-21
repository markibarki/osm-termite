package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.data.OsmObject;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import intransix.osm.termite.map.data.OsmSegment;
import java.util.HashMap;

/**
 *
 * @author sutter
 */
public class SnapVirtualNode extends SnapObject {
	
	public OsmSegment segment;
	
	/** Constructor */
	public SnapVirtualNode(OsmSegment segment, double mercX, double mercY, double err2) {
		super(SnapType.VIRTUAL_NODE);
		this.segment = segment;
		this.snapPoint = new Point2D.Double(mercX,mercY);
		this.err2 = err2;
	}
	
	/** This method tests if the mouse hit the virtual node on this segment.
	 * 
	 * @param segment		The segment to test
	 * @param mouseMerc		The mouse point, in pmercator coordinates
	 * @param mercRadSq		The allowed radius for a hit, in mercator coordinates
	 * @return				A SnapVirtualNode, if there was a hit.
	 */
	public static SnapVirtualNode testVirtualNodeHit(OsmSegment segment, Point2D mouseMerc,
			double mercRadSq) {
		
		double xCenter = (segment.getNode1().getPoint().getX() + segment.getNode2().getPoint().getX())/2;
		double yCenter = (segment.getNode1().getPoint().getY() + segment.getNode2().getPoint().getY())/2;
		double err2 = mouseMerc.distanceSq(xCenter,yCenter);
		if(err2 < mercRadSq) {
			return new SnapVirtualNode(segment,xCenter,yCenter,err2);
		}
		else {
			return null;
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
		renderPoint(g2,mercatorToPixels,snapPoint,style);
	}
	
	/** This method looks up an select object for this snap object.  . 
	 * 
	 * @return			The edit object
	 */
	@Override
	public Object getSelectObject() {
		return new VirtualNode(segment);
	}
}
