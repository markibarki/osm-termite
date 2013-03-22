/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.shape.Line;

/**
 *
 * @author sutter
 */
public class SegmentGraphic extends Line implements ShapeGraphic {
	
	private double widthPixels;
	
	public SegmentGraphic(Point2D p1, Point2D p2, AffineTransform mercToLocal) {

		Point2D localPoint = new Point2D.Double();
		
		mercToLocal.transform(p1,localPoint);
		this.setStartX(localPoint.getX());
		this.setStartY(localPoint.getY());
		
		mercToLocal.transform(p2,localPoint);
		this.setEndX(localPoint.getX());
		this.setEndY(localPoint.getY());
	}
	
	public void setStyle(Style style, double pixelsToLocalScale) {
		widthPixels = style.getStrokeWidth();
		style.setLineStyle(this);
		setPixelsToLocalScale(pixelsToLocalScale);
	}
	
	public void setPixelsToLocalScale(double pixelsToLocalScale) {
		this.setStrokeWidth(widthPixels * pixelsToLocalScale);
	}
	
}
