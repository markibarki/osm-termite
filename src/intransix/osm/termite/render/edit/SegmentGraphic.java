/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

import java.awt.geom.Point2D;
import javafx.scene.shape.Line;

/**
 *
 * @author sutter
 */
public class SegmentGraphic extends Line implements ShapeGraphic {
	
	private double widthPixels;
	
	public SegmentGraphic(Point2D p1, Point2D p2) {
		super(p1.getX(),p2.getY(),p2.getX(),p2.getY());	
	}
	
	public void setStyle(Style style, double pixelsToMerc) {
		widthPixels = style.getStrokeWidth();
		style.setLineStyle(this);
		setPixelsToMerc(pixelsToMerc);
	}
	
	public void setPixelsToMerc(double pixelsToMerc) {
		this.setStrokeWidth(widthPixels * pixelsToMerc);
	}
	
}
