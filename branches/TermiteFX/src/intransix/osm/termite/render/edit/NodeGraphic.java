
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.workingdata.OsmNode;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.shape.Circle;

/**
 * This class plots a node for a local coordinate frame.
 * 
 * @author sutter
 */
public class NodeGraphic extends Circle implements ShapeGraphic {
	
	private double radiusPixels = 0;
	
	public NodeGraphic(OsmNode node, AffineTransform mercToLocal) {
		Point2D localPoint = new Point2D.Double();
		mercToLocal.transform(node.getPoint(),localPoint);
		
		this.setCenterX(localPoint.getX());
		this.setCenterY(localPoint.getY());
	}
	
	public NodeGraphic(Point2D mercPoint, AffineTransform mercToLocal) {
		Point2D localPoint = new Point2D.Double();
		mercToLocal.transform(mercPoint,localPoint);
		
		this.setCenterX(localPoint.getX());
		this.setCenterY(localPoint.getY());
	}
	
	@Override
	public void setStyle(Style style, double pixelsToLocalScale) {
		radiusPixels = style.getPointRadius();
		style.setAreaStyle(this);
		setPixelsToLocalScale(pixelsToLocalScale);
	}
	
	@Override
	public void setPixelsToLocalScale(double pixelsToLocalScale) {
		this.setRadius(radiusPixels * pixelsToLocalScale);
	}
}
