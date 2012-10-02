package intransix.osm.termite.app.edit.snapobject;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmObject;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import java.util.HashMap;

/**
 *
 * @author sutter
 */
public class SnapNode extends SnapObject {
	
	public OsmNode node;
	
	/** Constructor */
	public SnapNode(OsmNode node, double err2) {
		super(SnapType.NODE);
		this.node = node;
		this.err2 = err2;
		this.snapPoint = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
	}
	
	/** This method tests if the mouse point hits the node. 
	 * 
	 * @param node		The node to test
	 * @param mouseMerc	The mouse point to hit, in mercator coordinates
	 * @param mercRadSq	The radius allowed for a hit, in mercator coordinates
	 * @return 
	 */
	public static SnapNode testNode(OsmNode node, Point2D mouseMerc, double mercRadSq) {
		double err2 = mouseMerc.distanceSq(node.getPoint());
		if(err2 < mercRadSq) {
			return new SnapNode(node,err2);
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
		renderPoint(g2,mercatorToPixels,node.getPoint(),style);
	}
	
	/** This method looks up an select object for this snap object. 
	 * 
	 * @param editMap	The edit map of existing edit objects
	 * @return			The edit object
	 */
	@Override
	public Object getSelectObject() {
		return node;
	}
	
}
