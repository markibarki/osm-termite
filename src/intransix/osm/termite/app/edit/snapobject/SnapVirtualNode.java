package intransix.osm.termite.app.edit.snapobject;

import intransix.osm.termite.map.workingdata.OsmObject;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import intransix.osm.termite.map.workingdata.OsmSegment;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import intransix.osm.termite.app.edit.data.VirtualNode;
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
