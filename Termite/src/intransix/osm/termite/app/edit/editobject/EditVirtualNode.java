package intransix.osm.termite.app.edit.editobject;

import intransix.osm.termite.map.data.OsmSegment;
import intransix.osm.termite.render.edit.StyleInfo;
import intransix.osm.termite.app.edit.VirtualNode;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public class EditVirtualNode extends EditObject {
	public EditNode en1;
	public EditNode en2;
	public OsmSegment osmSegment;
	
	//this is a virtual point
	public EditNode enVirtual;
	public EditSegment es1;
	public EditSegment es2;
	
	/** This method renders the object.
	 * 
	 * @param g2				The graphics context
	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
	 * @param styleInfo			The style info for rendering
	 */
	@Override
	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
			StyleInfo styleInfo) {
		
		enVirtual.render(g2, mercatorToPixels, styleInfo);
	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** Constructor */
	public EditVirtualNode(VirtualNode virtualNode) {
		this.osmSegment = virtualNode.segment;
		en1 = new EditNode(osmSegment.getNode1());
		en2 = new EditNode(osmSegment.getNode2());
		
		double xCenter = (osmSegment.getNode1().getPoint().getX() + osmSegment.getNode2().getPoint().getX())/2;
		double yCenter = (osmSegment.getNode1().getPoint().getY() + osmSegment.getNode2().getPoint().getY())/2;
		Point2D midPoint = new Point2D.Double(xCenter,yCenter);
		enVirtual = new EditNode(midPoint,null);

		es1 = new EditSegment(null,en1,enVirtual);
		es2 = new EditSegment(null,enVirtual,en2);
	}
}
