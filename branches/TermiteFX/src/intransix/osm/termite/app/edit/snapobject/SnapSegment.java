package intransix.osm.termite.app.edit.snapobject;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import intransix.osm.termite.app.edit.editobject.EditObject;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;

/**
 *
 * @author sutter
 */
public class SnapSegment extends SnapObject {
	
	//=======================
	// Properties
	//=======================
	
	//display line start
	public Point2D p1;
	//display line end
	public Point2D p2;
	
	//=======================
	// Public Methods
	//=======================
	
	/** Constructor */
	public SnapSegment() {
		//set type as unknown - se dont' know what kind of segment
		super(SnapType.UNKNOWN);
	}
	
//	
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
//		renderSegment(g2,mercatorToPixels,p1,p2,style);
//	}
	
	/** This method looks up an select object for this snap object.  . There is
	 * no select object for a segment.
	 * 
	 * @param editMap	The edit map of existing edit objects
	 * @return			The edit object
	 */
	@Override
	public EditObject getSelectObject() {
		//no select object for segment
		return null;
	}

}
