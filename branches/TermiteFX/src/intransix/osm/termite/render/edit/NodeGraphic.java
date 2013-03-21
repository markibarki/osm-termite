/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package intransix.osm.termite.render.edit;

import intransix.osm.termite.map.workingdata.OsmNode;
import java.awt.geom.Point2D;
import javafx.scene.shape.Circle;

/**
 *
 * @author sutter
 */
public class NodeGraphic extends Circle implements ShapeGraphic {
	
	private double radiusPixels = 0;
	
	public NodeGraphic(OsmNode node) {
		super(node.getPoint().getX(),node.getPoint().getY(),0);
	}
	
	public NodeGraphic(Point2D point) {
		super(point.getX(),point.getY(),0);
	}
	
	public void setStyle(Style style, double pixelsToMerc) {
		radiusPixels = style.getPointRadius();
		style.setAreaStyle(this);
		setPixelsToMerc(pixelsToMerc);
	}
	
	public void setPixelsToMerc(double pixelsToMerc) {
		this.setStrokeWidth(radiusPixels * pixelsToMerc);
	}
}
