/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmWay;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import javafx.scene.shape.Polyline;

/**
 *
 * @author sutter
 */
public class WayGraphic extends Polyline implements ShapeGraphic {
	
	private double widthPixels;
	
	public WayGraphic(OsmWay way, AffineTransform mercToLocal) {
		Point2D localPoint = new Point2D.Double();
		for(OsmNode node:way.getNodes()) {
			mercToLocal.transform(node.getPoint(),localPoint);
			this.getPoints().add(localPoint.getX());
			this.getPoints().add(localPoint.getY());
		}
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
