package intransix.osm.termite.app.edit.editobject;

import intransix.osm.termite.map.workingdata.OsmSegment;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 *
 * @author sutter
 */
public class EditSegment extends EditObject {
	public EditNode en1;
	public EditNode en2;
	public OsmSegment osmSegment;
	
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
//		Style style = styleInfo.PENDING_STYLE;
//		renderSegment(g2,mercatorToPixels,en1.point,en2.point,style);
//	}
	
	//=======================
	// Package Methods
	//=======================
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	public EditSegment(OsmSegment osmSegment, EditNode en1, EditNode en2) {
		this.en1 = en1;
		this.en2 = en2;
		this.osmSegment = osmSegment;
	}
}
