/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.workingdata.OsmNode;
import intransix.osm.termite.map.workingdata.OsmWay;
import java.awt.geom.Point2D;
import javafx.scene.shape.Polyline;

/**
 *
 * @author sutter
 */
public class WayGraphic extends Polyline implements ShapeGraphic {
	
	private double widthPixels;
	
	public WayGraphic(OsmWay way) {
		Point2D point;
		for(OsmNode node:way.getNodes()) {
			point = node.getPoint();
			this.getPoints().add(point.getX());
			this.getPoints().add(point.getY());
		}
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
