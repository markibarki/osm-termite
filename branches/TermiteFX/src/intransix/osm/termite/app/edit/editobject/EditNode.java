package intransix.osm.termite.app.edit.editobject;

import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.app.mapdata.MapDataManager;
import intransix.osm.termite.map.feature.FeatureInfo;
import intransix.osm.termite.render.edit.Style;
import intransix.osm.termite.render.edit.StyleInfo;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 *
 * @author sutter
 */
public class EditNode extends EditObject {
	
	public Point2D point;
	public FeatureInfo featureInfo;
	public OsmNode node;
	
	/** This method renders the object.
	 * 
	 * @param g2				The graphics context
	 * @param mercatorToPixels	The transform from mercator coordinates to pixels
	 * @param styleInfo			The style info for rendering
	 */
	@Override
	public void render(Graphics2D g2, AffineTransform mercatorToPixels, 
			StyleInfo styleInfo) {
		Style style = styleInfo.PENDING_STYLE;
		renderPoint(g2,mercatorToPixels,point,style);
	}

	
	//=======================
	// Package Methods
	//=======================
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	public EditNode(OsmNode node) {
		this.node = node;
//@TODO clean this line up
		this.featureInfo = intransix.osm.termite.render.map.RenderLayer.getObjectFeatureInfo(node);
		this.point = new Point2D.Double(node.getPoint().getX(),node.getPoint().getY());
	}
	
	/** This method should not be called except from within the EditObjects. 
	 * Use the static method from EditObject to get an instance of this object. */
	public EditNode(Point2D point, FeatureInfo featureInfo) {
		this.node = null;
		this.featureInfo = featureInfo;
		this.point = new Point2D.Double(point.getX(),point.getY()); 
	}
}
